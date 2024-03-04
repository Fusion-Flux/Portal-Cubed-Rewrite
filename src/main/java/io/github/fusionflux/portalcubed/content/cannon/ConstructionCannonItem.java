package io.github.fusionflux.portalcubed.content.cannon;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.content.cannon.data.CannonSettings;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenCannonConfigPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConstructionCannonItem extends Item {
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
		System.out.println(result);

		return InteractionResult.SUCCESS;
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

		if (ctx.getLevel() instanceof ServerLevel level) {
			construct.place(level, clicked);
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
}
