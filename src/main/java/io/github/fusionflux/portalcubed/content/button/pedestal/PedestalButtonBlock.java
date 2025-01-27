package io.github.fusionflux.portalcubed.content.button.pedestal;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.block.HammerableBlock;
import io.github.fusionflux.portalcubed.framework.block.PortalCubedStateProperties;
import io.github.fusionflux.portalcubed.framework.extension.BigShapeBlock;
import io.github.fusionflux.portalcubed.framework.util.VoxelShaper;
import io.github.fusionflux.portalcubed.framework.util.VoxelShaper.DefaultRotationValues;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenPedestalButtonConfigPacket;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
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
	public static final MapCodec<PedestalButtonBlock> CODEC = simpleCodec(PedestalButtonBlock::new);

	public static final EnumProperty<Direction> FACE = PortalCubedStateProperties.FACE;
	public static final EnumProperty<Offset> OFFSET = EnumProperty.create("offset", Offset.class);
	public static final BooleanProperty BASE = BooleanProperty.create("base");
	public static final BooleanProperty ACTIVE = PortalCubedStateProperties.ACTIVE;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final VoxelShaper BASE_SHAPE = VoxelShaper.forHorizontal(box(5.5, 1, 5.5, 10.5, 20, 10.5), Direction.UP);
	private static final VoxelShaper OLD_AP_BASE_SHAPE = VoxelShaper.forHorizontal(Shapes.or(box(5.5, 0, 5.5, 10.5, 17.05, 10.5), box(6, 17, 6, 10, 19.05, 10)), Direction.UP);

	private final Map<BlockState, VoxelShape> shapes;
	private final SoundEvent pressSound;
	private final SoundEvent releaseSound;

	public PedestalButtonBlock(Properties properties) {
		this(
			properties,
			BASE_SHAPE,
			PortalCubedSounds.PEDESTAL_BUTTON_PRESS, PortalCubedSounds.PEDESTAL_BUTTON_RELEASE
		);
	}

	public PedestalButtonBlock(Properties properties, VoxelShaper shape, SoundEvent pressSound, SoundEvent releaseSound) {
		super(properties);
		this.pressSound = pressSound;
		this.releaseSound = releaseSound;
		this.shapes = new Reference2ReferenceOpenHashMap<>();
		for (BlockState state : this.stateDefinition.getPossibleStates()) {
			Direction face = state.getValue(FACE);
			Direction facing = state.getValue(FACING);
			boolean base = state.getValue(BASE);
			Vec3 shift = state.getValue(OFFSET).get(face, facing, base);
			VoxelShape rotated = VoxelShaper.rotate(shape.get(facing).move(0, base ? 1 / 16d : 0, 0), Direction.UP, face, new DefaultRotationValues());
			this.shapes.put(state, rotated.move(shift.x() / 16d, shift.y() / 16d, shift.z() / 16d));
		}
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
			OLD_AP_BASE_SHAPE,
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
		return defaultBlockState()
			.setValue(FACE, clickedFace)
			.setValue(FACING, direction)
			.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
		if (!moved && !state.is(newState.getBlock())) {
			if (state.getValue(ACTIVE))
				updateNeighbours(state, world, pos);
			super.onRemove(state, world, pos, newState, moved);
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
		return this.shapes.get(state);
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
		public final boolean centered;

		Offset(int stepX, int stepY) {
			this.name = name().toLowerCase(Locale.ROOT);
			this.stepX = stepX;
			this.stepY = stepY;
			this.centered = stepX == 0;
		}

		@Override
		@NotNull
		public String getSerializedName() {
			return name;
		}

		public Vec3 get(Direction face, Direction facing, boolean pad) {
			boolean flip = face.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
			return (switch (face.getAxis()) {
				case X -> new Vec3(0, -stepY, flip ? stepX : -stepX);
				case Y -> switch (facing) {
					case SOUTH -> new Vec3(-stepX, 0, -stepY);
					case WEST -> new Vec3(stepY, 0, -stepX);
					case EAST -> new Vec3(-stepY, 0, stepX);
					default -> new Vec3(stepX, 0, stepY);
				};
				case Z -> new Vec3(flip ? -stepX : stepX, -stepY, 0);
			}).scale(pad ? SHIFT_DIST - .5 : SHIFT_DIST);
		}
	}
}
