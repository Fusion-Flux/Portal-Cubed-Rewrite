package io.github.fusionflux.portalcubed.content.button.pedestal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.PortalCubedStateProperties;
import io.github.fusionflux.portalcubed.content.prop.HammerItem;
import io.github.fusionflux.portalcubed.framework.util.VoxelShaper;
import io.github.fusionflux.portalcubed.framework.util.VoxelShaper.DefaultRotationValues;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenPedestalButtonConfigPacket;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PedestalButtonBlock extends HorizontalDirectionalBlock implements EntityBlock {
	public static final EnumProperty<Direction> FACE = EnumProperty.create("face", Direction.class);
	public static final EnumProperty<Offset> OFFSET = EnumProperty.create("offset", Offset.class);
	public static final BooleanProperty ACTIVE = PortalCubedStateProperties.ACTIVE;

	private final Map<BlockState, VoxelShape> shapes;
	private final SoundEvent pressSound;
	private final SoundEvent releaseSound;

	public PedestalButtonBlock(Properties properties) {
		this(properties, VoxelShaper.forHorizontal(box(5.5, 1, 5.5, 10.5, 20, 10.5), Direction.UP), PortalCubedSounds.PEDESTAL_BUTTON_PRESS, PortalCubedSounds.PEDESTAL_BUTTON_RELEASE);
	}

	public PedestalButtonBlock(Properties properties, VoxelShaper shape, SoundEvent pressSound, SoundEvent releaseSound) {
		super(properties);
		this.pressSound = pressSound;
		this.releaseSound = releaseSound;
		this.shapes = new HashMap<>();
		for (var state : this.stateDefinition.getPossibleStates()) {
			var face = state.getValue(FACE);
			var facing = state.getValue(FACING);
			var shift = state.getValue(OFFSET).relative(face, facing);
			var rotated = VoxelShaper.rotate(shape.get(facing), Direction.UP, face, new DefaultRotationValues());
			shapes.put(state, rotated.move(shift.getX() / 16d, shift.getY() / 16d, shift.getZ() / 16d));
		}
		this.registerDefaultState(this.stateDefinition.any().setValue(FACE, Direction.UP).setValue(FACING, Direction.SOUTH).setValue(OFFSET, Offset.NONE).setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACE, FACING, OFFSET, ACTIVE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		var clickedFace = ctx.getClickedFace();
		boolean horizontal = clickedFace.getAxis().isHorizontal();
		var direction = ctx.getHorizontalDirection().getAxisDirection() == Direction.AxisDirection.NEGATIVE ? Direction.NORTH : Direction.SOUTH;
		for (var looking : ctx.getNearestLookingDirections()) {
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
		return defaultBlockState().setValue(FACE, clickedFace).setValue(FACING, direction);
	}

	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (state.getValue(ACTIVE)) {
			world.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
			world.gameEvent(null, GameEvent.BLOCK_DEACTIVATE, pos);
			playSound(null, world, pos, false);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return shapes.get(state);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (player.getAbilities().mayBuild && HammerItem.usingHammer(player)) {
			if (player instanceof ServerPlayer hammerUser)
				PortalCubedPackets.sendToClient(hammerUser, new OpenPedestalButtonConfigPacket(pos));
		} else {
			if (state.getValue(ACTIVE)) {
				return InteractionResult.CONSUME;
			} else {
				press(player, state, world, pos);
			}
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	private void press(@Nullable Player player, BlockState state, Level world, BlockPos pos) {
		world.setBlock(pos, state.setValue(ACTIVE, true), Block.UPDATE_ALL);
		world.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
		if (world.getBlockEntity(pos) instanceof PedestalButtonBlockEntity pedestalButton)
			world.scheduleTick(pos, this, pedestalButton.getPressTime());
		playSound(player, world, pos, true);
	}

	private void playSound(@Nullable Player player, LevelAccessor world, BlockPos pos, boolean pressed) {
		world.playSound(player, pos, pressed ? pressSound : releaseSound, SoundSource.BLOCKS);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PedestalButtonBlockEntity(pos, state);
	}

	@ClientOnly
	public static void pick(double reach, float tickDelta) {
		var client = Minecraft.getInstance();
		if (!(client.cameraEntity instanceof LocalPlayer player))
			return;
		var level = client.level;

		var start = player.getEyePosition(tickDelta);
		reach = (client.hitResult != null && client.hitResult.getType() != HitResult.Type.MISS) ? client.hitResult.getLocation().distanceTo(start) : reach;
		var end = start.add(player.getViewVector(tickDelta).scale(reach));

		var clipContext = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
		var result = BlockGetter.traverseBlocks(start, end, clipContext, ($, pos) -> {
			BlockHitResult currentHit = null;
			for (var cur : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
				var state = level.getBlockState(cur);
				if (state.getBlock() instanceof PedestalButtonBlock) {
					var hit = state.getShape(level, cur).clip(start, end, cur);
					if (hit == null || hit.getType() == HitResult.Type.MISS) continue;
					if (currentHit != null && Vec3.atCenterOf(cur).distanceToSqr(start) >= Vec3.atCenterOf(currentHit.getBlockPos()).distanceTo(start)) continue;
					currentHit = new BlockHitResult(Vec3.atCenterOf(cur), hit.getDirection(), cur.immutable(), hit.isInside());
				}
			}
			return currentHit;
		}, $ -> {
			var dir = start.subtract(end);
			return BlockHitResult.miss(end, Direction.getNearest(dir.x, dir.y, dir.z), BlockPos.containing(end));
		});

		if (result != null && result.getType() != HitResult.Type.MISS)
			client.hitResult = result;
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

		public static final int SHIFT_DIST = 4;

		public final String name;
		public final int stepX;
		public final int stepY;

		Offset(int stepX, int stepY) {
			this.name = name().toLowerCase(Locale.ROOT);
			this.stepX = stepX;
			this.stepY = stepY;
		}

		@Override
		public String getSerializedName() {
			return name;
		}

		public Vec3i relative(Direction face, Direction facing) {
			var shift = switch (facing) {
				case SOUTH -> IntIntPair.of(-stepX, -stepY);
				case WEST -> IntIntPair.of(stepY, -stepX);
				case EAST -> IntIntPair.of(-stepY, stepX);
				default -> IntIntPair.of(stepX, stepY);
			};
			int sign = face.getAxisDirection() == Direction.AxisDirection.NEGATIVE ? -1 : 1;
			return (switch (face.getAxis()) {
				case X -> new Vec3i(0, shift.rightInt(), shift.leftInt()).multiply(sign);
				case Y -> new Vec3i(shift.leftInt(), 0, shift.rightInt());
				case Z -> new Vec3i(-shift.leftInt(), -shift.rightInt(), 0).multiply(sign);
			}).multiply(SHIFT_DIST);
		}
	}
}
