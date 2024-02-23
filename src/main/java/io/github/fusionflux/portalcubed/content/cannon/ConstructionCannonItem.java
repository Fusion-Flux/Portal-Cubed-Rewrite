package io.github.fusionflux.portalcubed.content.cannon;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.content.cannon.data.DeviceData;
import io.github.fusionflux.portalcubed.content.cannon.data.DeviceInventory;
import io.github.fusionflux.portalcubed.content.cannon.data.PlacementMode;
import io.github.fusionflux.portalcubed.content.cannon.data.PlacementSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class ConstructionCannonItem extends Item {
	public ConstructionCannonItem(Properties settings) {
		super(settings);
	}

	@Override
	@NotNull
	public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		ItemStack held = user.getItemInHand(hand);
		if (user.isSecondaryUseActive()) {
			held.addTagElement(DeviceData.NBT_KEY, DeviceData.CODEC.encodeStart(NbtOps.INSTANCE, new DeviceData(
					new PlacementSettings(
							Optional.of(Items.GOLD_BLOCK),
							Optional.of(PortalCubed.id("test")),
							PlacementMode.WHOLE,
							Optional.empty()
					),
					new DeviceInventory(Map.of())
			)).getOrThrow(false, $ -> {}));
//			user.openMenu(new SimpleMenuProvider(ConstructionCannonMenu::new, ConstructionCannonMenu.TITLE));
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
		DeviceData data = this.getDeviceData(stack);
		if (data == null) // invalid state
			return CannonUseResult.INVALID;

		Rotation rotation = this.getRotation(ctx);

		PlacementSettings.Configured settings = data.settings().validate();
		if (settings == null) // not configured
			return CannonUseResult.NOT_CONFIGURED;

		ConstructSet constructSet = ConstructManager.INSTANCE.getConstructSet(settings.construct());
		if (constructSet == null) // fake construct
			return CannonUseResult.INVALID;

		Construct construct = constructSet.choose(ConstructPlacementContext.of(ctx));

		BlockPos clicked = new BlockPlaceContext(ctx).getClickedPos();
		BoundingBox bounds = construct.getBounds(rotation);

		if (!this.mayBuild(ctx, bounds))
			return CannonUseResult.NO_PERMS;

		if (ctx.getLevel() instanceof ServerLevel level) {
			construct.place(level, clicked, rotation);
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

	protected Rotation getRotation(UseOnContext ctx) {
		return switch (ctx.getHorizontalDirection()) {
			case NORTH -> Rotation.CLOCKWISE_180;
			default /* SOUTH */ -> Rotation.NONE;
			case EAST -> Rotation.CLOCKWISE_90;
			case WEST -> Rotation.COUNTERCLOCKWISE_90;
		};
	}

	@Nullable
	protected DeviceData getDeviceData(ItemStack stack) {
		CompoundTag nbt = stack.getTag();
		if (nbt != null && nbt.contains(DeviceData.NBT_KEY, Tag.TAG_COMPOUND)) {
			CompoundTag dataNbt = nbt.getCompound(DeviceData.NBT_KEY);
			return DeviceData.CODEC.parse(NbtOps.INSTANCE, dataNbt).result().orElse(null);
		}
		return null;
	}
}
