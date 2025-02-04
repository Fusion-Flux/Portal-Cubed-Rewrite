package io.github.fusionflux.portalcubed.framework.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadView;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class DelegatingQuadEmitter implements QuadEmitter {
	protected QuadEmitter delegate;

	@Override
	public QuadEmitter pos(int vertexIndex, float x, float y, float z) {
		this.delegate.pos(vertexIndex, x, y, z);
		return this;
	}

	@Override
	public QuadEmitter color(int vertexIndex, int color) {
		this.delegate.color(vertexIndex, color);
		return this;
	}

	@Override
	public QuadEmitter uv(int vertexIndex, float u, float v) {
		this.delegate.uv(vertexIndex, u, v);
		return this;
	}

	@Override
	public QuadEmitter spriteBake(TextureAtlasSprite sprite, int bakeFlags) {
		this.delegate.spriteBake(sprite, bakeFlags);
		return this;
	}

	@Override
	public QuadEmitter lightmap(int vertexIndex, int lightmap) {
		this.delegate.lightmap(vertexIndex, lightmap);
		return this;
	}

	@Override
	public QuadEmitter normal(int vertexIndex, float x, float y, float z) {
		this.delegate.normal(vertexIndex, x, y, z);
		return this;
	}

	@Override
	public QuadEmitter cullFace(@Nullable Direction face) {
		this.delegate.cullFace(face);
		return this;
	}

	@Override
	public QuadEmitter nominalFace(@Nullable Direction face) {
		this.delegate.nominalFace(face);
		return this;
	}

	@Override
	public QuadEmitter material(RenderMaterial material) {
		this.delegate.material(material);
		return this;
	}

	@Override
	public QuadEmitter tintIndex(int tintIndex) {
		this.delegate.tintIndex(tintIndex);
		return this;
	}

	@Override
	public QuadEmitter tag(int tag) {
		this.delegate.tag(tag);
		return this;
	}

	@Override
	public QuadEmitter copyFrom(QuadView quad) {
		this.delegate.copyFrom(quad);
		return this;
	}

	@Override
	public QuadEmitter fromVanilla(int[] quadData, int startIndex) {
		this.delegate.fromVanilla(quadData, startIndex);
		return this;
	}

	@Override
	public QuadEmitter fromVanilla(BakedQuad quad, RenderMaterial material, @Nullable Direction cullFace) {
		this.delegate.fromVanilla(quad, material, cullFace);
		return this;
	}

	@Override
	public void pushTransform(QuadTransform transform) {
		this.delegate.pushTransform(transform);
	}

	@Override
	public void popTransform() {
		this.delegate.popTransform();
	}

	@Override
	public QuadEmitter emit() {
		this.delegate.emit();
		return this;
	}

	@Override
	public float x(int vertexIndex) {
		return this.delegate.x(vertexIndex);
	}

	@Override
	public float y(int vertexIndex) {
		return this.delegate.y(vertexIndex);
	}

	@Override
	public float z(int vertexIndex) {
		return this.delegate.z(vertexIndex);
	}

	@Override
	public float posByIndex(int vertexIndex, int coordinateIndex) {
		return this.delegate.posByIndex(vertexIndex, coordinateIndex);
	}

	@Override
	public Vector3f copyPos(int vertexIndex, @Nullable Vector3f target) {
		return this.delegate.copyPos(vertexIndex, target);
	}

	@Override
	public int color(int vertexIndex) {
		return this.delegate.color(vertexIndex);
	}

	@Override
	public float u(int vertexIndex) {
		return this.delegate.u(vertexIndex);
	}

	@Override
	public float v(int vertexIndex) {
		return this.delegate.v(vertexIndex);
	}

	@Override
	public Vector2f copyUv(int vertexIndex, @Nullable Vector2f target) {
		return this.delegate.copyUv(vertexIndex, target);
	}

	@Override
	public int lightmap(int vertexIndex) {
		return this.delegate.lightmap(vertexIndex);
	}

	@Override
	public boolean hasNormal(int vertexIndex) {
		return this.delegate.hasNormal(vertexIndex);
	}

	@Override
	public float normalX(int vertexIndex) {
		return this.delegate.normalX(vertexIndex);
	}

	@Override
	public float normalY(int vertexIndex) {
		return this.delegate.normalY(vertexIndex);
	}

	@Override
	public float normalZ(int vertexIndex) {
		return this.delegate.normalZ(vertexIndex);
	}

	@Override
	public @Nullable Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target) {
		return this.delegate.copyNormal(vertexIndex, target);
	}

	@Override
	public @Nullable Direction cullFace() {
		return this.delegate.cullFace();
	}

	@Override
	public @NotNull Direction lightFace() {
		return this.delegate.lightFace();
	}

	@Override
	public @Nullable Direction nominalFace() {
		return this.delegate.nominalFace();
	}

	@Override
	public Vector3fc faceNormal() {
		return this.delegate.faceNormal();
	}

	@Override
	public RenderMaterial material() {
		return this.delegate.material();
	}

	@Override
	public int tintIndex() {
		return this.delegate.tintIndex();
	}

	@Override
	public int tag() {
		return this.delegate.tag();
	}

	@Override
	public void toVanilla(int[] target, int targetIndex) {
		this.delegate.toVanilla(target, targetIndex);
	}
}
