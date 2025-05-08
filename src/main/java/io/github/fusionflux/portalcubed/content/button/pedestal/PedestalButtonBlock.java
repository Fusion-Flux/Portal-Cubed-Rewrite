package io.github.fusionflux.portalcubed.content.button.pedestal;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.block.HammerableBlock;
import io.github.fusionflux.portalcubed.framework.block.PortalCubedStateProperties;
import io.github.fusionflux.portalcubed.framework.extension.BigShapeBlock;
import io.github.fusionflux.portalcubed.framework.shape.voxel.VoxelShaper;
import io.github.fusionflux.portalcubed.framework.shape.voxel.VoxelShaper.DefaultRotationValues;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenPedestalButtonConfigPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PedestalButtonBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock, EntityBlock, HammerableBlock, BigShapeBlock {
	public static final double ONE_PIXEL = 1 / 16d;

	public static final MapCodec<PedestalButtonBlock> CODEC = simpleCodec(PedestalButtonBlock::new);

	public static final EnumProperty<Direction> FACE = PortalCubedStateProperties.FACE;
	public static final EnumProperty<Offset> OFFSET = EnumProperty.create("offset", Offset.class);
	public static final BooleanProperty BASE = BooleanProperty.create("base");
	public static final BooleanProperty ACTIVE = PortalCubedStateProperties.ACTIVE;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public static final PedestalShapes NORMAL_SHAPES = new PedestalShapes(
			Shapes.or(
					// the shaft
					shaft(20),
					// the little base
					box(4, 0, 4, 12, 1, 12)
			),
			new ThreeDepthBaseShapes(
					box(0, 0, 7, 16, 1, 16),
					box(0, 0, 3.5, 16, 1, 12.5),
					box(0, 0, 0, 16, 1, 9)
			)
	);

	public static final PedestalShapes OLD_AP_SHAPES = new PedestalShapes(
			Shapes.or(
					// the main part
					shaft(17.05),
					// the button is thinner
					box(6, 17, 6, 10, 19.05, 10)
			),
			new RadialBaseShapes(
					box(2.5, 0, 2.5, 13.5, 1, 13.5),
					box(3.5, 0, 3.5, 12.5, 1, 12.5)
			)
	);

	private final ImmutableMap<BlockState, VoxelShape> shapesCache;
	private final SoundEvent pressSound;
	private final SoundEvent releaseSound;

	public PedestalButtonBlock(Properties properties) {
		this(
			properties,
			NORMAL_SHAPES,
			PortalCubedSounds.PEDESTAL_BUTTON_PRESS, PortalCubedSounds.PEDESTAL_BUTTON_RELEASE
		);
	}

	public PedestalButtonBlock(Properties properties, PedestalShapes shapes, SoundEvent pressSound, SoundEvent releaseSound) {
		super(properties);
		this.pressSound = pressSound;
		this.releaseSound = releaseSound;

		this.shapesCache = this.getShapeForEachState(state -> {
			Direction face = state.getValue(FACE);
			Direction facing = state.getValue(FACING);
			boolean base = state.getValue(BASE);
			Offset offset = state.getValue(OFFSET).rotate(face, facing);
			Vec3 shift = offset.get(base);

			// start with the shaft
			VoxelShape shape = shapes.shaft;
			if (base) {
				// move up a pixel to make room for the base
				shape = shape.move(0, ONE_PIXEL, 0);
			}

			// shift the shaft
			shape = shape.move(shift.x, shift.y, shift.z);

			if (base) {
				VoxelShape baseShape = shapes.base.get(offset);
				shape = Shapes.or(shape, baseShape);
			}

			// rotat e
			// step 1: towards facing
			shape = VoxelShaper.rotate(shape, Direction.SOUTH, facing, DefaultRotationValues.INSTANCE);
			// step 2: towards face
			shape = VoxelShaper.rotate(shape, Direction.UP, face, DefaultRotationValues.INSTANCE);

			return shape.optimize();
		});

		this.registerDefaultState(this.stateDefinition.any()
			.setValue(FACE, Direction.UP)
			.setValue(FACING, Direction.SOUTH)
			.setValue(OFFSET, Offset.NONE)
			.setValue(BASE, false)
			.setValue(ACTIVE, false)
			.setValue(WATERLOGGED, false)
		);
	}

	public static PedestalButtonBlock oldAp(Properties properties) {
		return new PedestalButtonBlock(
			properties,
			OLD_AP_SHAPES,
			PortalCubedSounds.OLD_AP_PEDESTAL_BUTTON_PRESS, PortalCubedSounds.OLD_AP_PEDESTAL_BUTTON_RELEASE
		);
	}

	@Override
	@NotNull
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACE, FACING, OFFSET, BASE, ACTIVE, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		Direction clickedFace = ctx.getClickedFace();
		boolean horizontal = clickedFace.getAxis().isHorizontal();
		Direction direction = ctx.getHorizontalDirection().getAxisDirection() == Direction.AxisDirection.NEGATIVE ? Direction.NORTH : Direction.SOUTH;
		FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
		for (Direction looking : ctx.getNearestLookingDirections()) {
			if (looking.getAxis() != clickedFace.getAxis()) {
				direction = switch (looking) {
					case NORTH -> horizontal ? Direction.WEST : looking;
					case SOUTH -> horizontal ? Direction.EAST : looking;
					case DOWN -> Direction.NORTH;
					case UP -> Direction.SOUTH;
					default -> looking;
				};
				break;
			}
		}
		return this.defaultBlockState()
			.setValue(FACE, clickedFace)
			.setValue(FACING, direction.getOpposite())
			.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if (!moved && !state.is(newState.getBlock())) {
			if (state.getValue(ACTIVE)) {
				this.updateNeighbours(state, world, pos);
			}
			super.onRemove(state, world, pos, newState, false);
		}
	}

	@Override
	public int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return state.getValue(ACTIVE) ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
		return state.getValue(ACTIVE) && state.getValue(FACE) == direction ? 15 : 0;
	}

	@Override
	public boolean isSignalSource(BlockState state) {
		return true;
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (state.getValue(ACTIVE)) {
			world.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
			this.updateNeighbours(state, world, pos);
			world.gameEvent(null, GameEvent.BLOCK_DEACTIVATE, pos);
			this.playSound(null, world, pos, false);
		}
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (state.getValue(WATERLOGGED))
			scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
	}

	@Override
	@NotNull
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Objects.requireNonNull(this.shapesCache.get(state));
	}

	@Override
	@NotNull
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	@NotNull
	public InteractionResult onHammered(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (player instanceof ServerPlayer serverPlayer)
			PortalCubedPackets.sendToClient(serverPlayer, new OpenPedestalButtonConfigPacket(pos));
		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
		HammerableBlock.appendTooltip(tooltip);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (state.getValue(ACTIVE)) {
			return InteractionResult.CONSUME;
		} else {
			this.press(player, state, world, pos);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected void onExplosionHit(BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer) {
		if (explosion.canTriggerBlocks() && !state.getValue(ACTIVE)) {
			this.press(null, state, level, pos);
		}

		super.onExplosionHit(state, level, pos, explosion, dropConsumer);
	}

	private void press(@Nullable Player player, BlockState state, Level world, BlockPos pos) {
		world.setBlock(pos, state.setValue(ACTIVE, true), Block.UPDATE_ALL);
		this.updateNeighbours(state, world, pos);
		world.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
		if (world.getBlockEntity(pos) instanceof PedestalButtonBlockEntity pedestalButton)
			world.scheduleTick(pos, this, pedestalButton.getPressTime());
		this.playSound(player, world, pos, true);
	}

	private void updateNeighbours(BlockState state, Level world, BlockPos pos) {
		world.updateNeighborsAt(pos, this);
		world.updateNeighborsAt(pos.relative(state.getValue(FACE).getOpposite()), this);
	}

	private void playSound(@Nullable Player player, LevelAccessor world, BlockPos pos, boolean pressed) {
		world.playSound(player, pos, pressed ? this.pressSound : this.releaseSound, SoundSource.BLOCKS);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PedestalButtonBlockEntity(pos, state);
	}

	private static VoxelShape shaft(double height) {
		return box(5.5, 0, 5.5, 10.5, height, 10.5);
	}

	// these are relative to [facing=south,face=up] viewed from above, horizontally facing north
	public enum Offset implements StringRepresentable {
		NONE(0, 0),
		UP(0, -1),
		UP_LEFT(-1, -1),
		UP_RIGHT(1, -1),
		DOWN(0, 1),
		DOWN_LEFT(-1, 1),
		DOWN_RIGHT(1, 1),
		LEFT(-1, 0),
		RIGHT(1, 0);

		public static final float SHIFT_DIST = 4;

		public final String name;
		public final int stepX;
		public final int stepY;

		private final Vec3 offsetWithBase;
		private final Vec3 offsetWithoutBase;

		Offset(int stepX, int stepY) {
			this.name = this.name().toLowerCase(Locale.ROOT);
			this.stepX = stepX;
			this.stepY = stepY;

			this.offsetWithBase = this.computeOffset(true);
			this.offsetWithoutBase = this.computeOffset(false);
		}

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}

		private Vec3 get(boolean hasBase) {
			return hasBase ? this.offsetWithBase : this.offsetWithoutBase;
		}

		public Vec3 forBase() {
			return this.get(true);
		}

		public Offset ccw90() {
			return switch (this) {
				case NONE -> NONE;
				case UP -> LEFT;
				case LEFT -> DOWN;
				case DOWN -> RIGHT;
				case RIGHT -> UP;
				case UP_LEFT -> DOWN_LEFT;
				case DOWN_LEFT -> DOWN_RIGHT;
				case DOWN_RIGHT -> UP_RIGHT;
				case UP_RIGHT -> UP_LEFT;
			};
		}

		public Offset xMirrored() {
			return switch (this) {
				case UP_LEFT -> UP_RIGHT;
				case LEFT -> RIGHT;
				case DOWN_LEFT -> DOWN_RIGHT;
				case UP_RIGHT -> UP_LEFT;
				case RIGHT -> LEFT;
				case DOWN_RIGHT -> DOWN_LEFT;
				default -> this;
			};
		}

		public Offset opposite() {
			return this.ccw90().ccw90();
		}

		public Offset cw90() {
			return this.opposite().ccw90();
		}

		public Offset rotate(Direction face, Direction facing) {
			if (this == NONE || face == Direction.UP) {
				return this;
			} else if (face == Direction.DOWN) {
				// don't ask, I can't answer.
				if (facing.getAxis() == Direction.Axis.Z) {
					return this.xMirrored().opposite();
				} else {
					return this.xMirrored();
				}
			}

			// this is one of the 8 outer offsets, and face is horizontal
			// offsets should be relative to [facing=south]

			return switch (facing) {
				case SOUTH -> this;
				case NORTH -> this.opposite();
				case WEST -> this.ccw90();
				case EAST -> this.cw90();
				default -> throw new IllegalArgumentException("Non-horizontal direction: " + facing);
			};
		}

		private Vec3 computeOffset(boolean hasBase) {
			double scale = ONE_PIXEL * (hasBase ? SHIFT_DIST - 0.5 : SHIFT_DIST);
			return new Vec3(this.stepX * scale, 0, this.stepY * scale);
		}
	}

	public record PedestalShapes(VoxelShape shaft, BaseShapes base) {
	}

	@FunctionalInterface
	public interface BaseShapes {
		VoxelShape get(Offset offset);
	}

	public record ThreeDepthBaseShapes(VoxelShape far, VoxelShape mid, VoxelShape near) implements BaseShapes {
		@Override
		public VoxelShape get(Offset offset) {
			return switch (offset.stepY) {
				case 1 -> this.far;
				case 0 -> this.mid;
				case -1 -> this.near;
				default -> throw new IllegalStateException("Weird offset: " + offset);
			};
		}
	}

	public record RadialBaseShapes(VoxelShape center, VoxelShape edge) implements BaseShapes {
		@Override
		public VoxelShape get(Offset offset) {
			if (offset == Offset.NONE)
				return this.center;

			return this.edge.move(offset.forBase());
		}
	}
}
