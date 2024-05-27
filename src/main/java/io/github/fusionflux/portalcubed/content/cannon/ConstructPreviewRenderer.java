package io.github.fusionflux.portalcubed.content.cannon;

import io.github.fusionflux.portalcubed.content.cannon.CannonSettings.Configured;
import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructModelPool.ModelInfo;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14C;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.framework.construct.ConstructModelPool;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ConstructPreviewRenderer {
	private static ConstructModelPool modelPool = new ConstructModelPool();

	private static void renderPreview(WorldRenderContext context) {
		if (modelPool == null) return;
		if (!(context.consumers() instanceof final MultiBufferSource.BufferSource bufferSource))
			return;

		var minecraft = Minecraft.getInstance();
		var player = minecraft.player;
		if (player == null)
			return;

		if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK)
			return;

		InteractionHand hand = getHandHoldingCannon(player);
		if (hand == null)
			return;

		ItemStack stack = player.getItemInHand(hand);
		CannonSettings settings = ConstructionCannonItem.getCannonSettings(stack);
		if (settings == null || !settings.preview())
			return;

		Configured configured = settings.validate();
		if (configured == null)
			return;

		PoseStack matrices = context.matrixStack();
		Vec3 camPos = context.camera().getPosition();

		UseOnContext placeContext = new UseOnContext(context.world(), player, hand, stack, hit);
		ConfiguredConstruct construct = configured.construct().choose(ConstructPlacementContext.of(placeContext));
		boolean replaceMode = settings.replaceMode();
		BlockPos pos = ConstructionCannonItem.getPlacementPos(placeContext, replaceMode);

		matrices.pushPose();
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);
		matrices.translate(pos.getX() + construct.offset.getX(), pos.getY() + construct.offset.getY(), pos.getZ() + construct.offset.getZ());

		boolean obstructed = construct.isObstructed(context.world(), pos, replaceMode);
		ModelInfo model = modelPool.getOrBuildModel(construct);
		model.draw(matrices, () -> {
			if (obstructed)
				RenderSystem.setShaderColor(1f, .5f, .5f, 1f);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
			GL14C.glBlendColor(0, 0, 0, settings.previewOpacity() + (Mth.cos(Util.getMillis() / 500f) * .1f));
			RenderSystem.depthFunc(GL11.GL_ALWAYS);
		});
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		model.bufferBlockEntities(matrices, bufferSource);
		bufferSource.endBatch();

		matrices.popPose();
	}

	@Nullable
	private static InteractionHand getHandHoldingCannon(Player player) {
		var mainHand = player.getMainHandItem();
		var offhand = player.getOffhandItem();

		if (mainHand.is(PortalCubedItems.CONSTRUCTION_CANNON)) {
			return InteractionHand.MAIN_HAND;
		} else if (offhand.is(PortalCubedItems.CONSTRUCTION_CANNON)) {
			return InteractionHand.OFF_HAND;
		}

		return null;
	}

	public static void reload() {
		if (modelPool != null) {
			modelPool.close();
			modelPool = null;
		}
		modelPool = new ConstructModelPool();
	}

	public static ConstructModelPool getModelPool() {
		return modelPool;
	}

	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ConstructPreviewRenderer::renderPreview);
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(ctx -> {
			if (ctx.server() instanceof IntegratedServer)
				reload();
		});
	}
}
