package io.github.fusionflux.portalcubed.content.cannon;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14C;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.framework.construct.ConstructModelPool;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ConstructRenderer {
	public static ConstructModelPool MODEL_POOL = new ConstructModelPool();

	private static void renderPreview(WorldRenderContext context) {
		if (MODEL_POOL == null) return;
		if (!(context.consumers() instanceof final MultiBufferSource.BufferSource bufferSource))
			return;
		var minecraft = Minecraft.getInstance();
		var player = minecraft.player;
		if (player == null) return;

		PoseStack matrices = context.matrixStack();
		Vec3 camPos = context.camera().getPosition();
		matrices.pushPose();
		matrices.translate(-camPos.x, -camPos.y, -camPos.z);

		var hand = getHandHoldingCannon(player);
		if (hand == null) return;
		var itemInHand = player.getItemInHand(hand);
		var heldCannon = ConstructionCannonItem.getCannonSettings(itemInHand);
		if (!heldCannon.preview()) return;
		if ((heldCannon != null && heldCannon.construct().isPresent()) && minecraft.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK) {
			var placeContext = new BlockPlaceContext(minecraft.level, player, hand, itemInHand, hit);
			var construct = ConstructManager.INSTANCE.getConstructSet(heldCannon.construct().get())
				.choose(ConstructPlacementContext.of(placeContext));
			var pos = placeContext.getClickedPos();

			matrices.pushPose();
			matrices.translate(pos.getX(), pos.getY(), pos.getZ());

			boolean obstructed = construct.isObstructed(minecraft.level, pos);
			var model = MODEL_POOL.getOrBuildModel(construct);
			model.draw(matrices, () -> {
				if (obstructed)
					RenderSystem.setShaderColor(1f, .5f, .5f, 1f);
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
				GL14C.glBlendColor(0, 0, 0, .55f + (Mth.cos(Util.getMillis() / 500f) * .1f));
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
		if (MODEL_POOL != null) {
			MODEL_POOL.close();
			MODEL_POOL = null;
		}
		MODEL_POOL = new ConstructModelPool();
	}

	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ConstructRenderer::renderPreview);
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(ctx -> {
			if (ctx.server() instanceof IntegratedServer)
				reload();
		});
	}
}
