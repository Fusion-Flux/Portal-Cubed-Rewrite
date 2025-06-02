package io.github.fusionflux.portalcubed.content.cannon;

import org.joml.Math;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;

public class ConstructionCannonAnimator {
	private static final float RECOIL_POWER = 25f;
	private static final float MISSING_MATERIALS_RECOIL_POWER = 17f;
	private static final float RECOIL_DECAY = 11.9f;

	private static final float WIGGLE_STOP = (float) Math.PI * 16;
	private static final float WIGGLE_DECAY = 17f;
	private static final float WIGGLE_SPEED = 6f;
	private static final float WIGGLE_ROTATION_OFFSET = 0.1875f;

	private static float recoilOld;
	private static float recoil;
	private static float wiggleOld;
	private static float wiggle;

	public static void tick(Minecraft client) {
		if (client.isPaused())
			return;

		recoilOld = recoil;
		recoil = Math.max(0, recoil - RECOIL_DECAY);

		wiggleOld = wiggle;
		wiggle = Math.max(0, wiggle - WIGGLE_DECAY);
	}

	public static void onShoot(CannonUseResult useResult) {
		recoilOld = 0f;
		if (useResult.shouldRecoil()) {
			recoil = useResult == CannonUseResult.MISSING_MATERIALS ? MISSING_MATERIALS_RECOIL_POWER : RECOIL_POWER;
		} else {
			recoil = recoilOld;
		}

		wiggleOld = 0f;
		if (useResult.shouldWiggle()) {
			wiggle = WIGGLE_STOP;
		} else {
			wiggle = wiggleOld;
		}
	}

	public static void animate(PoseStack matrices, float tickDelta, InteractionHand hand) {
		matrices.mulPose(Axis.XP.rotationDegrees(Mth.lerp(tickDelta, recoilOld, recoil)));

		float wiggleRotationOffset = hand == InteractionHand.MAIN_HAND ? WIGGLE_ROTATION_OFFSET : -WIGGLE_ROTATION_OFFSET;
		matrices.translate(wiggleRotationOffset, 0, 0);
		matrices.mulPose(Axis.ZP.rotationDegrees(Math.sin(Mth.lerp(tickDelta, wiggleOld, wiggle) / WIGGLE_SPEED) * WIGGLE_SPEED));
		matrices.translate(-wiggleRotationOffset, 0, 0);
	}
}
