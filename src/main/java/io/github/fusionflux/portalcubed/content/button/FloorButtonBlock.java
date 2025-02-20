package io.github.fusionflux.portalcubed.content.button;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.PortalCubedCriteriaTriggers;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.prop.entity.ButtonActivatedProp;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedEntityTags;
import io.github.fusionflux.portalcubed.framework.block.PortalCubedStateProperties;
import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;
import io.github.fusionflux.portalcubed.framework.util.VoxelShaper;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.PufferfishAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class
FloorButtonBlock extends AbstractMultiBlock {
	public static final MapCodec<FloorButtonBlock> CODEC = simpleCodec(FloorButtonBlock::new);

	public static final SizeProperties SIZE_PROPERTIES = SizeProperties.create(2, 2, 1);
	public static final BooleanProperty ACTIVE = PortalCubedStateProperties.ACTIVE;
	public static final int PRESSED_TIME = 5;
	public static final double DISINTEGRATION_EJECTION_FORCE = 0.05;

	private static final VoxelShaper[][] SHAPES = new VoxelShaper[][]{
		new VoxelShaper[]{
			VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(4, 1, 4, 16, 3, 16)), Direction.UP),
			VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(0, 1, 4, 12, 3, 16)), Direction.UP)
		},
		new VoxelShaper[]{
			VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(4, 1, 0, 16, 3, 12)), Direction.UP),
			VoxelShaper.forDirectional(Shapes.or(box(0, 0, 0, 16, 1, 16), box(0, 1, 0, 12, 3, 12)), Direction.UP)
		}
	};
	private static final VoxelShape BUTTON_SHAPE = box(7.5, 7.5, 3, 16, 16, 4);
	private static final VoxelShaper[][] OLD_AP_SHAPES = new VoxelShaper[][]{
		new VoxelShaper[]{
			VoxelShaper.forDirectional(box(2, 0, 2, 16, 2, 16), Direction.UP),
			VoxelShaper.forDirectional(box(0, 0, 2, 14, 2, 16), Direction.UP)
		},
		new VoxelShaper[]{
			VoxelShaper.forDirectional(box(2, 0, 0, 16, 2, 14), Direction.UP),
			VoxelShaper.forDirectional(box(0, 0, 0, 14, 2, 14), Direction.UP)
		}
	};
	private static final VoxelShape OLD_AP_BUTTON_SHAPE = box(4, 4, 2, 16, 16, 3);

	public final VoxelShaper[][] shapes;
	public final EnumMap<Direction, AABB> buttonBounds = new EnumMap<>(Direction.class);
	public final Predicate<? super Entity> entityPredicate;
	public final SoundEvent pressSound;
	public final SoundEvent releaseSound;

	public FloorButtonBlock(
		Properties properties,
		VoxelShaper[][] shapes,
		VoxelShape buttonShape,
		Predicate<? super Entity> entityPredicate,
		SoundEvent pressSound,
		SoundEvent releaseSound
	) {
		super(properties);
		this.shapes = shapes;
		this.buttonBounds.put(Direction.SOUTH, new AABB(
			buttonShape.min(Direction.Axis.X) * 2,
			buttonShape.min(Direction.Axis.Y) * 2,
			buttonShape.min(Direction.Axis.Z),
			buttonShape.max(Direction.Axis.X) * 2,
			buttonShape.max(Direction.Axis.Y) * 2,
			buttonShape.max(Direction.Axis.Z)
		).move(-buttonShape.min(Direction.Axis.X), -buttonShape.min(Direction.Axis.Y), 0));
		this.entityPredicate = EntitySelector.NO_SPECTATORS.and(entity -> !entity.isIgnoringBlockTriggers()).and(entityPredicate);
		this.pressSound = pressSound;
		this.releaseSound = releaseSound;
		this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVE, false));
	}

	public FloorButtonBlock(Properties properties, VoxelShaper[][] shapes, VoxelShape buttonShape, SoundEvent pressSound, SoundEvent releaseSound) {
		this(properties, shapes, buttonShape, entity -> entity instanceof LivingEntity || entity.getType().is(PortalCubedEntityTags.PRESSES_FLOOR_BUTTONS), pressSound, releaseSound);
	}

	public FloorButtonBlock(Properties properties, SoundEvent pressSound, SoundEvent releaseSound) {
		this(properties, SHAPES, BUTTON_SHAPE, pressSound, releaseSound);
	}

	public FloorButtonBlock(Properties properties) {
		this(properties, PortalCubedSounds.FLOOR_BUTTON_PRESS, PortalCubedSounds.FLOOR_BUTTON_RELEASE);
	}

	public static FloorButtonBlock oldAp(Properties properties) {
		return new FloorButtonBlock(properties, OLD_AP_SHAPES, OLD_AP_BUTTON_SHAPE, PortalCubedSounds.OLD_AP_FLOOR_BUTTON_PRESS, PortalCubedSounds.OLD_AP_FLOOR_BUTTON_RELEASE);
	}

	public static FloorButtonBlock p1(Properties properties) {
		return new FloorButtonBlock(properties, PortalCubedSounds.PORTAL_1_FLOOR_BUTTON_PRESS, PortalCubedSounds.PORTAL_1_FLOOR_BUTTON_RELEASE);
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	public AABB getButtonBounds(Direction face) {
		return buttonBounds.computeIfAbsent(face, $ -> {
			AABB baseButtonBounds = buttonBounds.get(Direction.SOUTH);

			Vec3 min = new Vec3(baseButtonBounds.minX, baseButtonBounds.minY, baseButtonBounds.minZ);
			Vec3 max = new Vec3(baseButtonBounds.maxX, baseButtonBounds.maxY, baseButtonBounds.maxZ);
			if (face.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
				min = VoxelShaper.rotate(min.subtract(1, 0, .5), 180, Direction.Axis.Y).add(1, 0, .5);
				max = VoxelShaper.rotate(max.subtract(1, 0, .5), 180, Direction.Axis.Y).add(1, 0, .5);
			}

			return switch (face.getAxis()) {
				case X -> new AABB(min.z, min.y, min.x, max.z, max.y, max.x);
				case Y -> new AABB(min.x, min.z, min.y, max.x, max.z, max.y);
				case Z -> new AABB(min.x, min.y, min.z, max.x, max.y, max.z);
			};
		});
	}

	protected void toggle(BlockState state, Level level, BlockPos pos, @Nullable Entity entity, boolean currentState) {
		for (BlockPos quadrantPos : quadrants(pos, state)) {
			BlockState quadrantState = level.getBlockState(quadrantPos);
			if (!quadrantState.is(this)) return;
			level.setBlock(quadrantPos, quadrantState.setValue(ACTIVE, !currentState), UPDATE_ALL);
			updateNeighbours(quadrantState, level, quadrantPos);
		}

		SoundEvent toggleSound;
		if (currentState) {
			level.gameEvent(entity, GameEvent.BLOCK_DEACTIVATE, pos);
			toggleSound = releaseSound;
		} else {
			level.scheduleTick(pos, this, PRESSED_TIME);
			level.gameEvent(entity, GameEvent.BLOCK_ACTIVATE, pos);
			toggleSound = pressSound;
		}
		playSoundAtCenter(toggleSound, 0, 0, -.5, 1f, 1f, pos, state, level);
	}

	protected void updateNeighbours(BlockState state, Level world, BlockPos pos) {
		world.updateNeighborsAt(pos, this);
		world.updateNeighborsAt(pos.relative(state.getValue(FACE).getOpposite()), this);
	}

	@Override
	public SizeProperties sizeProperties() {
		return SIZE_PROPERTIES;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(ACTIVE);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		int y = getY(state);
		int x = getX(state);
		Direction face = state.getValue(FACE);
		VoxelShaper shape = switch (face) {
			case NORTH, EAST ->       shapes[y == 1 ? 0 : 1][x == 1 ? 0 : 1];
			case DOWN, WEST, SOUTH -> shapes[y == 1 ? 0 : 1][x];
			default ->                shapes[y][x];
		};
		return shape.get(face);
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state) {
		return Shapes.empty();
	}

	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
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
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!level.getEntitiesOfClass(Entity.class, getButtonBounds(state.getValue(FACE)).move(pos), entityPredicate).isEmpty()) {
			level.scheduleTick(pos, this, PRESSED_TIME);
		} else if (state.getValue(ACTIVE)) {
			toggle(state, level, pos, null, true);
		}
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (level instanceof ServerLevel serverLevel) {
			boolean entityInsideBounds = isEntityPressing(state, pos, entity);
			if (entityInsideBounds)
				entityPressing(state, serverLevel, getOriginPos(pos, state), entity);
		}
	}

	public boolean isEntityPressing(BlockState state, BlockPos pos, Entity entity) {
		return entityPredicate.test(entity) && getButtonBounds(state.getValue(FACE)).move(getOriginPos(pos, state)).intersects(entity.getBoundingBox());
	}

	protected void entityPressing(BlockState state, ServerLevel level, BlockPos pos, Entity entity) {
		if (!state.getValue(ACTIVE))
			toggle(state, level, pos, entity, false);

		// trigger advancements
		AABB area = new AABB(pos).inflate(16);
		List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, area);
		for (ServerPlayer player : players) {
			PortalCubedCriteriaTriggers.ENTITY_ON_BUTTON.trigger(player, pos, entity);
		}

		// special effects
		if (entity instanceof ButtonActivatedProp buttonActivated) {
			buttonActivated.setActivated(true);
		} if (entity instanceof Armadillo armadillo && armadillo.canStayRolledUp()) {
			armadillo.getBrain().setMemoryWithExpiry(MemoryModuleType.DANGER_DETECTED_RECENTLY, true, 80);
			armadillo.rollUp();
		} else if (entity instanceof Pufferfish pufferfish) {
			if (pufferfish.getPuffState() != Pufferfish.STATE_FULL) {
				pufferfish.makeSound(SoundEvents.PUFFER_FISH_BLOW_UP);
			}

			pufferfish.setPuffState(Pufferfish.STATE_FULL);
			((PufferfishAccessor) pufferfish).setDeflateTimer(0);
		}
	}
}
