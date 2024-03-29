package io.github.fusionflux.portalcubed.content.cannon;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.content.cannon.data.CannonSettings;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenCannonConfigPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ConstructionCannonItem extends Item {
	public static final int PARTICLES = 10;

	public ConstructionCannonItem(Properties settings) {
		super(settings);
	}

	@Override
	@NotNull
	public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		ItemStack held = user.getItemInHand(hand);
		if (user.isSecondaryUseActive()) {
			if (user instanceof ServerPlayer serverPlayer) {
				PortalCubedPackets.sendToClient(serverPlayer, new OpenCannonConfigPacket(hand));
			}
			return InteractionResultHolder.success(held);
		}

		// do nothing
		return InteractionResultHolder.pass(held);
	}

	@Override
	@NotNull
	public InteractionResult useOn(UseOnContext context) {
		if (context.isSecondaryUseActive())
			return InteractionResult.PASS; // fall back to use

		CannonUseResult result = this.tryPlace(context);
		if (result == CannonUseResult.PLACED) {
			if (context.getPlayer() instanceof ServerPlayer player) {
				ServerLevel level = player.serverLevel();
				// kaboom
				level.playSound(
						null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.4f,
						level.getRandom().nextIntBetweenInclusive(120, 270) / 100f
				);
				Vec3 source = getParticleSource(player);
				level.sendParticles(
						new DustParticleOptions(Vec3.fromRGB24(0xFFFFFF).toVector3f(), 1),
						source.x, source.y, source.z,
						PARTICLES,
						0.1, 0.1, 0.1,
						0.1
				);
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.FAIL;
	}

	protected CannonUseResult tryPlace(UseOnContext ctx) {
		ItemStack stack = ctx.getItemInHand();
		CannonSettings settings = getCannonSettings(stack);
		if (settings == null) // invalid state
			return CannonUseResult.INVALID;

		CannonSettings.Configured configured = settings.validate();
		if (configured == null) // not configured
			return CannonUseResult.NOT_CONFIGURED;

		ConstructSet constructSet = ConstructManager.INSTANCE.getConstructSet(configured.construct());
		if (constructSet == null) // fake construct
			return CannonUseResult.INVALID;

		ConfiguredConstruct construct = constructSet.choose(ConstructPlacementContext.of(ctx));

		BlockPos clicked = new BlockPlaceContext(ctx).getClickedPos();

		BoundingBox bounds = construct.getAbsoluteBounds(clicked);
		if (!this.mayBuild(ctx, bounds))
			return CannonUseResult.NO_PERMS;

		if (construct.isObstructed(ctx.getLevel(), clicked))
			return CannonUseResult.OBSTRUCTED;

		if (ctx.getLevel() instanceof ServerLevel level) {
			construct.place(level, clicked, ctx.getPlayer(), stack);
		}

		return CannonUseResult.PLACED;
	}

	protected boolean mayBuild(UseOnContext ctx, BoundingBox box) {
		Player player = ctx.getPlayer();
		if (player == null)
			return true;

		Level level = ctx.getLevel();
		return BlockPos.betweenClosedStream(box).allMatch(
				pos -> player.mayInteract(level, pos)
		);
	}

	@Nullable
	public static CannonSettings getCannonSettings(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		if (nbt != null && nbt.contains(CannonSettings.NBT_KEY, Tag.TAG_COMPOUND)) {
			CompoundTag dataNbt = nbt.getCompound(CannonSettings.NBT_KEY);
			return CannonSettings.CODEC.parse(NbtOps.INSTANCE, dataNbt).result().orElse(null);
		}
		return null;
	}

	public static void setCannonSettings(ItemStack stack, CannonSettings settings) {
		CannonSettings.CODEC.encodeStart(NbtOps.INSTANCE, settings).result()
				.ifPresent(nbt -> stack.addTagElement(CannonSettings.NBT_KEY, nbt));
	}

	private static Vec3 getParticleSource(Player player) {
		// based on Camera
		float pitch = player.getXRot();
		float yaw = player.getYRot();
		Quaternionf rotation = new Quaternionf().rotationXYZ(
				-yaw * (float) (Math.PI / 180.0),
				pitch * (float) (Math.PI / 180.0),
				0.0F
		);
		Vector3f offset = new Vector3f(0, 1, 0);
		offset.rotate(rotation);
		return player.getEyePosition().add(
				offset.x, offset.y, offset.z
		);
	}
}
