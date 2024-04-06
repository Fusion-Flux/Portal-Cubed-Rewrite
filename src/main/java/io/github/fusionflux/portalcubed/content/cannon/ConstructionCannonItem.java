package io.github.fusionflux.portalcubed.content.cannon;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.extension.CustomHoldPoseItem;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.framework.item.TagTranslation;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenCannonConfigPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.OtherPlayerShootCannonPacket;
import io.github.fusionflux.portalcubed.packet.clientbound.ShootCannonPacket;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.levelgen.structure.BoundingBox;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PlayerLookup;

import java.util.List;
import java.util.Objects;

public class ConstructionCannonItem extends Item implements @ClientOnly CustomHoldPoseItem {
	public static final Component MATERIAL_TOOLTIP = translate("material").withStyle(ChatFormatting.GRAY);
	public static final Component CONSTRUCT_TOOLTIP = translate("construct_set").withStyle(ChatFormatting.GRAY);

	public ConstructionCannonItem(Properties settings) {
		super(settings);
	}

	@Override
	@NotNull
	public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		ItemStack held = user.getItemInHand(hand);
		if (user.isSecondaryUseActive()) {
			tryOpenConfig(user, hand);
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

		Player player = context.getPlayer();
		// player is required for material consumption
		if (player == null)
			return InteractionResult.PASS;

		CannonUseResult result = this.tryPlace(context);

		// feedback
		result.sound().ifPresent(player::playSound);
		result.feedback(player.getRandom()).ifPresent(
				feedback -> player.displayClientMessage(feedback, true)
		);
		if (player instanceof ServerPlayer serverPlayer) {
			PortalCubedPackets.sendToClient(serverPlayer, new ShootCannonPacket(context.getHand(), result));
		}

		if (result == CannonUseResult.MISCONFIGURED) {
			tryOpenConfig(player, context.getHand());
			return InteractionResult.CONSUME;
		} else if (result == CannonUseResult.PLACED) {
			// kaboom
			player.playSound(SoundEvents.GENERIC_EXPLODE, 0.4f, player.getRandom().nextIntBetweenInclusive(120, 270) / 100f);
			if (player instanceof ServerPlayer) {
				OtherPlayerShootCannonPacket packet = new OtherPlayerShootCannonPacket(player);
				PlayerLookup.tracking(player).forEach(
						tracking -> PortalCubedPackets.sendToClient(tracking, packet)
				);
			}
			return InteractionResult.CONSUME;
		}

		return InteractionResult.FAIL;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
		CannonSettings settings = getCannonSettings(stack);
		if (settings == null)
			return;

		if (settings.material().isPresent()) {
			tooltip.add(MATERIAL_TOOLTIP);
			Component name = TagTranslation.translate(settings.material().get());
			tooltip.add(CommonComponents.space().append(name).withStyle(ChatFormatting.BLUE));
		}
		if (settings.construct().isPresent()) {
			tooltip.add(CONSTRUCT_TOOLTIP);
			Component name = ConstructSet.getName(settings.construct().get());
			tooltip.add(CommonComponents.space().append(name).withStyle(ChatFormatting.BLUE));
		}
	}

	@ClientOnly
	@Override
	public ArmPose getHoldPose(ItemStack stack) {
		return ArmPose.CROSSBOW_HOLD;
	}

	protected CannonUseResult tryPlace(UseOnContext ctx) {
		ItemStack stack = ctx.getItemInHand();
		CannonSettings settings = getCannonSettings(stack);
		if (settings == null) // invalid state
			return CannonUseResult.MISCONFIGURED;

		CannonSettings.Configured configured = settings.validate();
		if (configured == null) // not configured
			return CannonUseResult.MISCONFIGURED;

		ConstructSet constructSet = configured.construct();
		ConfiguredConstruct construct = constructSet.choose(ConstructPlacementContext.of(ctx));

		BlockPos clicked = new BlockPlaceContext(ctx).getClickedPos();

		BoundingBox bounds = construct.getAbsoluteBounds(clicked);
		if (!this.mayBuild(ctx, bounds))
			return CannonUseResult.NO_PERMS;

		if (construct.isObstructed(ctx.getLevel(), clicked))
			return CannonUseResult.OBSTRUCTED;

		Player player = Objects.requireNonNull(ctx.getPlayer()); // null is checked on use
		if (!this.consumeMaterials(player, constructSet.material, constructSet.cost))
			return CannonUseResult.MISSING_MATERIALS;

		if (ctx.getLevel() instanceof ServerLevel level) {
			construct.place(level, clicked, player, stack);
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

	@SuppressWarnings("deprecation")
	protected boolean consumeMaterials(Player player, TagKey<Item> tag, int count) {
		// creative players always have enough.
		if (player.isCreative())
			return true;

		try (Transaction t = Transaction.openOuter()) {
			PlayerInventoryStorage storage = PlayerInventoryStorage.of(player);
			for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
				ItemVariant variant = view.getResource();
				//noinspection deprecation - builtInRegistryHolder
				if (variant.getItem().builtInRegistryHolder().is(tag)) {
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

	public static MutableComponent translate(String key) {
		return Component.translatable("item.portalcubed.construction_cannon." + key);
	}

	private static void tryOpenConfig(Player player, InteractionHand hand) {
		if (player instanceof ServerPlayer serverPlayer) {
			PortalCubedPackets.sendToClient(serverPlayer, new OpenCannonConfigPacket(hand));
		}
	}
}
