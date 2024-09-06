package io.github.fusionflux.portalcubed.content.decoration.signage.small;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.decoration.signage.SignageBlock;
import io.github.fusionflux.portalcubed.mixin.UseOnContextAccessor;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenSignageConfigPacket;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class SmallSignageBlock extends SignageBlock {
	public static final MapCodec<SmallSignageBlock> CODEC = simpleCodec(SmallSignageBlock::new);

	public static final Map<Quadrant, BooleanProperty> QUADRANT_PROPERTIES = Util.make(new EnumMap<>(Quadrant.class), map -> {
		for (Quadrant quadrant : Quadrant.values()) {
			map.put(quadrant, BooleanProperty.create(quadrant.name));
		}
	});

	public SmallSignageBlock(Properties properties) {
		super(properties);

		BlockState defaultState = this.stateDefinition.any()
				.setValue(FACE, AttachFace.WALL)
				.setValue(FACING, Direction.NORTH)
				.setValue(WATERLOGGED, false);
		for (BooleanProperty quadrant : QUADRANT_PROPERTIES.values()) {
			defaultState = defaultState.setValue(quadrant, false);
		}
		this.registerDefaultState(defaultState);
	}

	@Override
	@NotNull
	protected MapCodec<SmallSignageBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		QUADRANT_PROPERTIES.values().forEach(builder::add);
	}

	public static Optional<Quadrant> getHitQuadrant(BlockState state, BlockHitResult hit) {
		Direction hitDirection = hit.getDirection();
		if (hitDirection != getConnectedDirection(state))
			return Optional.empty();

		BlockPos hitBlockPos = hit.getBlockPos().relative(hitDirection);
		Vec3 relativeHitPos = hit.getLocation().subtract(hitBlockPos.getX(), hitBlockPos.getY(), hitBlockPos.getZ());
		if (state.getValue(FACE) != AttachFace.WALL) {
			Direction rotation = state.getValue(FACING);
			relativeHitPos = relativeHitPos
					.subtract(Quadrant.SIZE, 0, Quadrant.SIZE)
					.yRot(rotation.toYRot() * Mth.DEG_TO_RAD)
					.add(Quadrant.SIZE, 0, Quadrant.SIZE);
		}

		Vec2 faceRelativeHitPos = switch (hitDirection) {
			case DOWN -> new Vec2((float) (1 - relativeHitPos.x), (float) (1 - relativeHitPos.z));
			case UP -> new Vec2((float) (1 - relativeHitPos.x), (float) relativeHitPos.z);
			case NORTH -> new Vec2((float) (1 - relativeHitPos.x), (float) relativeHitPos.y);
			case SOUTH -> new Vec2((float) relativeHitPos.x, (float) relativeHitPos.y);
			case WEST -> new Vec2((float) relativeHitPos.z, (float) relativeHitPos.y);
			case EAST -> new Vec2((float) (1 - relativeHitPos.z), (float) relativeHitPos.y);
		};

		for (Quadrant quadrant : Quadrant.VALUES) {
			if (quadrant.contains(faceRelativeHitPos))
				return Optional.of(quadrant);
		}
		return Optional.empty();
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return Optionull.map(super.getStateForPlacement(ctx), state ->
				getHitQuadrant(state, ((UseOnContextAccessor) ctx).pc$getHitResult())
						.map(quadrant -> state.setValue(QUADRANT_PROPERTIES.get(quadrant), true))
						.orElse(null)
		);
	}

	@Override
	@NotNull
	public InteractionResult onHammered(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (player instanceof ServerPlayer serverPlayer)
			PortalCubedPackets.sendToClient(serverPlayer, new OpenSignageConfigPacket.Small(hitResult));
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SmallSignageBlockEntity(pos, state);
	}

	public enum Quadrant implements StringRepresentable {
		TOP_LEFT(new Vec2(0, 1)),
		TOP_RIGHT(new Vec2(1, 1)),
		BOTTOM_LEFT(new Vec2(0, 0)),
		BOTTOM_RIGHT(new Vec2(1, 0));

		public static final Quadrant[] VALUES = values();
		public static final float SIZE = 8 / 16f;

		public final String name;
		public final Vec2 min;
		public final Vec2 max;

		Quadrant(Vec2 origin) {
			this.name = this.name().toLowerCase(Locale.ROOT);
			this.min = origin.scale(SIZE);
			this.max = this.min.add(SIZE);
		}

		public boolean contains(Vec2 point) {
			return (point.x >= min.x) && (point.x <= max.x) && (point.y >= min.y) && (point.y <= max.y);
		}

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}
	}
}
