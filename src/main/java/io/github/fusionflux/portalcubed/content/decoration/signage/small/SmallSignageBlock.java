package io.github.fusionflux.portalcubed.content.decoration.signage.small;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.decoration.signage.SignageBlock;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.github.fusionflux.portalcubed.mixin.utils.accessors.UseOnContextAccessor;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenSignageConfigPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
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

	public static void setQuadrant(Level world, BlockPos pos, Quadrant quadrant, boolean enabled) {
		BlockState state = world.getBlockState(pos);
		if (!(state.getBlock() instanceof SmallSignageBlock))
			return;

		BlockState newState = state.setValue(QUADRANT_PROPERTIES.get(quadrant), enabled);
		boolean noQuadrants = QUADRANT_PROPERTIES.values()
				.stream()
				.map(newState::getValue)
				.allMatch(Predicate.isEqual(false));
		if (noQuadrants) {
			world.destroyBlock(pos, true);
		} else {
			world.setBlock(pos, newState, Block.UPDATE_ALL);
		}
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

		Vector2d faceRelativeHitPos = switch (hitDirection) {
			case DOWN -> new Vector2d(1 - relativeHitPos.x, 1 - relativeHitPos.z);
			case UP -> new Vector2d(1 - relativeHitPos.x, relativeHitPos.z);
			case NORTH -> new Vector2d(1 - relativeHitPos.x, relativeHitPos.y);
			case SOUTH -> new Vector2d(relativeHitPos.x, relativeHitPos.y);
			case WEST -> new Vector2d(relativeHitPos.z, relativeHitPos.y);
			case EAST -> new Vector2d(1 - relativeHitPos.z, relativeHitPos.y);
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
				getHitQuadrant(state, ((UseOnContextAccessor) ctx).invokeGetHitResult())
						.map(quadrant -> state.setValue(QUADRANT_PROPERTIES.get(quadrant), true))
						.orElse(null)
		);
	}

	@Override
	@NotNull
	public InteractionResult onHammered(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (player instanceof ServerPlayer serverPlayer)
			PortalCubedPackets.sendToClient(serverPlayer, new OpenSignageConfigPacket.Small(hit));
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SmallSignageBlockEntity(pos, state);
	}

	public enum Quadrant implements StringRepresentable {
		TOP_LEFT(0, 1),
		TOP_RIGHT(1, 1),
		BOTTOM_LEFT(0, 0),
		BOTTOM_RIGHT(1, 0);

		public static final Quadrant[] VALUES = values();
		public static final Codec<Quadrant> CODEC = StringRepresentable.fromEnum(() -> VALUES);
		public static final StreamCodec<ByteBuf, Quadrant> STREAM_CODEC = PortalCubedStreamCodecs.ofEnum(Quadrant.class);

		public static final float SIZE = 8 / 16f;

		public final String name;
		public final double minX;
		public final double minY;
		public final double maxX;
		public final double maxY;

		Quadrant(double originX, double originY) {
			this.name = this.name().toLowerCase(Locale.ROOT);
			this.minX = originX * SIZE;
			this.minY = originY * SIZE;
			this.maxX = this.minX + SIZE;
			this.maxY = this.minY + SIZE;
		}

		public boolean contains(Vector2dc point) {
			return (point.x() >= this.minX) && (point.x() <= this.maxX) && (point.y() >= this.minY) && (point.y() <= this.maxY);
		}

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}
	}
}
