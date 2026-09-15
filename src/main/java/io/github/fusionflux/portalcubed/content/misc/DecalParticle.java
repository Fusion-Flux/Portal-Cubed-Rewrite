package io.github.fusionflux.portalcubed.content.misc;

import org.jspecify.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.portal.PortalData;
import io.github.fusionflux.portalcubed.framework.particle.DecalPos;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry.PendingParticleProvider;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DecalParticle extends SingleQuadParticle {
	public static final double SURFACE_OFFSET = 0.01f;
	public static final int LIFETIME = 1200;

	private final BlockPos supportingPos;
	private final Direction facing;
	private final Layer layer;

	@Nullable
	private BlockState lastSupportingState;

	protected DecalParticle(ClientLevel level, Vec3 pos, Direction facing, float roll, Layer layer, TextureAtlasSprite sprite) {
		super(level, pos.x, pos.y, pos.z, sprite);
		this.setLifetime(LIFETIME);

		this.supportingPos = getSupportingPos(pos, facing);
		this.facing = facing;
		this.layer = layer;
		this.roll = roll;
		this.oRoll = roll;

		this.recomputeSize();
	}

	private void recomputeSize() {
		SpriteContents contents = this.sprite.contents();
		int width = contents.width();
		int height = contents.height();
		this.setSize(width / 16f, height / 16f);
//		Gizmos.cuboid(this.getBoundingBox(), GizmoStyle.stroke(Colors.RED)).persistForMillis(5000);
//		Gizmos.point(new Vec3(this.x, this.y, this.z), Colors.GREEN, 10f).persistForMillis(5000);

		this.quadSize = Math.max(this.bbWidth, this.bbHeight) / 2;
	}

	@Override
	public void tick() {
		super.tick();

		BlockState supportingState = this.level.getBlockState(this.supportingPos);
		if (supportingState.isAir()) {
			this.remove();
			return;
		}

		if (this.lastSupportingState != null && this.lastSupportingState != supportingState) {
			VoxelShape baseShape = supportingState.getCollisionShape(this.level, this.supportingPos);
			Vec3 rayStart = new Vec3(this.x, this.y, this.z);
			Vec3 rayEnd = rayStart.subtract(this.facing.getUnitVec3().scale(DecalPos.ONE_PIXEL));
			BlockHitResult hit = baseShape.clip(rayStart, rayEnd, this.supportingPos);
			if (hit == null || hit.isInside()) {
				this.remove();
			}
		}

		this.lastSupportingState = supportingState;
	}

	@Override
	public FacingCameraMode getFacingCameraMode() {
		return (target, _, _) -> target.set(PortalData.normalToRotation(this.facing, 0)).rotateX(Mth.DEG_TO_RAD * -90);
	}

	@Override
	protected void setSprite(TextureAtlasSprite sprite) {
		super.setSprite(sprite);
		this.recomputeSize();
	}

	@Override
	public Layer getLayer() {
		return this.layer;
	}

	private static BlockPos getSupportingPos(Vec3 pos, Direction facing) {
		Vec3 offset = facing.getUnitVec3().scale(-DecalPos.ONE_PIXEL);
		return BlockPos.containing(pos.add(offset));
	}

	private static Direction decodeFacing(double xAux, double yAux, double zAux) {
		return Direction.getApproximateNearest(xAux, yAux, zAux);
	}

	private static float getRoll(boolean rotateRandomly, RandomSource random) {
		return rotateRandomly ? Mth.HALF_PI * random.nextIntBetweenInclusive(0, 3) : 0;
	}

	/// Slightly offsets the given position to avoid z-fighting, and also ensure the particle renders on top of its supporting block.
	private static Vec3 nudge(double x, double y, double z, Direction facing, RandomSource random) {
		Vec3 surfaceOffset = facing.getUnitVec3().scale(SURFACE_OFFSET);
		double nudge = random.nextDouble() * 0.0001;
		return surfaceOffset.add(x + nudge, y + nudge, z + nudge);
	}

	public static PendingParticleProvider<SimpleParticleType> bulletHoleProvider(SingleQuadParticle.Layer layer, boolean randomRotation) {
		return sprites -> (_, level, x, y, z, xAux, yAux, zAux, random) -> {
			Direction facing = decodeFacing(xAux, yAux, zAux);
			Vec3 pos = nudge(x, y, z, facing, random);
			float roll = getRoll(randomRotation, random);
			TextureAtlasSprite sprite = sprites.get(random);
			return new DecalParticle(level, pos, facing, roll, layer, sprite);
		};
	}

	public record ScorchProvider(FabricSpriteSet spriteSet) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux, RandomSource random) {
			Direction facing = decodeFacing(xAux, yAux, zAux);
			Vec3 pos = nudge(x, y, z, facing, random);
			float roll = getRoll(true, random);
			TextureAtlasSprite sprite = this.spriteSet.get(random);
			return new DecalParticle(level, pos, facing, roll, Layer.TRANSLUCENT, sprite);
		}
	}
}
