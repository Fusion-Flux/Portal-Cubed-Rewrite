package io.github.fusionflux.portalcubed.content.cannon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedBlockTags;
import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.extension.CustomHoldPoseItem;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenCannonConfigPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.OtherPlayerShootCannonPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.ShootCannonPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class ConstructionCannonItem extends Item implements CustomHoldPoseItem {
	public static final ResourceLocation REACH_BOOST = PortalCubed.id("construction_cannon_reach_boost");

	public ConstructionCannonItem(Properties settings) {
		super(settings);
	}

	@Override
	@NotNull
	public InteractionResult use(Level world, Player user, InteractionHand hand) {
		if (user.isSecondaryUseActive()) {
			tryOpenConfig(user, hand);
			user.awardStat(Stats.ITEM_USED.get(this));
			return InteractionResult.SUCCESS;
		}

		// do nothing
		return InteractionResult.PASS;
	}

	@Override
	@NotNull
	public InteractionResult useOn(UseOnContext context) {
		if (context.isSecondaryUseActive())
			return InteractionResult.PASS; // fall back to use

		if (!(context.getPlayer() instanceof ServerPlayer player))
			return InteractionResult.PASS;

		CannonUseResult result = this.tryPlace(context, player);

		// feedback
		result.sound().ifPresent(sound -> playSound(player, sound, 1, 1));
		result.feedback(player.getRandom()).ifPresent(
				feedback -> player.displayClientMessage(feedback, true)
		);

		PortalCubedPackets.sendToClient(player, new ShootCannonPacket(context.getHand(), result));

		if (result == CannonUseResult.MISCONFIGURED) {
			tryOpenConfig(player, context.getHand());
			return InteractionResult.CONSUME;
		} else if (result == CannonUseResult.PLACED) {
			// kaboom
			float pitch = player.getRandom().nextIntBetweenInclusive(120, 270) / 100f;
			playSound(player, SoundEvents.GENERIC_EXPLODE.value(), 0.4f, pitch);
			OtherPlayerShootCannonPacket packet = new OtherPlayerShootCannonPacket(player);
			PlayerLookup.tracking(player).forEach(
					tracking -> PortalCubedPackets.sendToClient(tracking, packet)
			);

			ItemStack stack = context.getItemInHand();
			UseCooldown cooldown = stack.get(DataComponents.USE_COOLDOWN);
			if (cooldown != null) {
				cooldown.apply(stack, player);
			}

			return InteractionResult.CONSUME;
		}

		return InteractionResult.FAIL;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public ArmPose getHoldPose(ItemStack stack) {
		return ArmPose.CROSSBOW_HOLD;
	}

	protected CannonUseResult tryPlace(UseOnContext ctx, ServerPlayer player) {
		ItemStack stack = ctx.getItemInHand();
		CannonSettings settings = getCannonSettings(stack);
		if (settings == null) // invalid state
			return CannonUseResult.MISCONFIGURED;

		CannonSettings.Configured configured = settings.validate();
		if (configured == null) // not configured
			return CannonUseResult.MISCONFIGURED;

		ConstructSet constructSet = configured.construct();
		ConfiguredConstruct construct = constructSet.choose(ConstructPlacementContext.of(ctx));

		boolean replaceMode = settings.replaceMode();
		BlockPos clicked = getPlacementPos(ctx, replaceMode);

		BoundingBox bounds = construct.getAbsoluteBounds(clicked);
		if (!this.mayBuild(player, bounds))
			return CannonUseResult.NO_PERMS;

		ServerLevel level = player.serverLevel();
		if (construct.isObstructed(level, clicked, replaceMode))
			return CannonUseResult.OBSTRUCTED;

		if (!this.consumeMaterials(player, constructSet.material, constructSet.cost))
			return CannonUseResult.MISSING_MATERIALS;

		construct.place(level, clicked, player, stack);
		return CannonUseResult.PLACED;
	}

	protected boolean mayBuild(ServerPlayer player, BoundingBox box) {
		return BlockPos.betweenClosedStream(box).allMatch(
				pos -> player.mayInteract(player.serverLevel(), pos)
		);
	}

	protected boolean consumeMaterials(Player player, TagKey<Item> tag, int count) {
		// creative players always have enough.
		if (player.isCreative())
			return true;

		try (Transaction t = Transaction.openOuter()) {
			PlayerInventoryStorage storage = PlayerInventoryStorage.of(player);
			for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
				ItemVariant variant = view.getResource();
				if (variant.getRegistryEntry().is(tag)) {
					// matches
					long extract = Math.min(count, view.getAmount());
					long extracted = view.extract(variant, extract, t);
					count -= (int) extracted;

					if (count <= 0) {
						// got enough
						t.commit();
						return true;
					}
				}
			}
		}
		// did not find enough resources.
		return false;
	}

	public static BlockPos getPlacementPos(UseOnContext ctx, boolean replaceMode) {
		BlockPos clicked = ctx.getClickedPos();
		BlockState state = ctx.getLevel().getBlockState(clicked);
		if (cantBeReplaced(state, replaceMode)) {
			clicked = clicked.relative(ctx.getClickedFace());
		}
		return clicked;
	}

	@Nullable
	public static CannonSettings getCannonSettings(ItemStack stack) {
		return stack.get(PortalCubedDataComponents.CANNON_SETTINGS);
	}

	public static void setCannonSettings(ItemStack stack, CannonSettings settings) {
		stack.set(PortalCubedDataComponents.CANNON_SETTINGS, settings);
	}

	public static MutableComponent translate(String key) {
		return Component.translatable("item.portalcubed.construction_cannon." + key);
	}

	public static boolean cantBeReplaced(BlockState state, boolean replaceMode) {
		if (state.canBeReplaced())
			return false;

		return !replaceMode || !state.is(PortalCubedBlockTags.CANNON_REPLACEABLE);
	}

	private static void tryOpenConfig(Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer serverPlayer) {
			PortalCubedPackets.sendToClient(serverPlayer, new OpenCannonConfigPacket(hand));
		}
	}

	private static void playSound(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
		if (!player.isSilent()) {
			player.playSound(sound, volume, pitch); // plays to other players
			player.playNotifySound(sound, player.getSoundSource(), volume, pitch); // plays to self
		}
	}
}
