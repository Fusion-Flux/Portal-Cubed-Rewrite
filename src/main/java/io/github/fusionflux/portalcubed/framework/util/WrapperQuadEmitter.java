package io.github.fusionflux.portalcubed.framework.util;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;

public class WrapperQuadEmitter implements QuadEmitter {
	protected QuadEmitter wrapped;

	@Override
	public QuadEmitter pos(int vertexIndex, float x, float y, float z) {
		this.wrapped.pos(vertexIndex, x, y, z);
		return this;
	}

	@Override
	public QuadEmitter color(int vertexIndex, int color) {
		this.wrapped.color(vertexIndex, color);
		return this;
	}

	@Override
	public QuadEmitter uv(int vertexIndex, float u, float v) {
		this.wrapped.uv(vertexIndex, u, v);
		return this;
	}

	@Override
	public QuadEmitter lightmap(int vertexIndex, int lightmap) {
		this.wrapped.lightmap(vertexIndex, lightmap);
		return this;
	}

	@Override
	public QuadEmitter normal(int vertexIndex, float x, float y, float z) {
		this.wrapped.normal(vertexIndex, x, y, z);
		return this;
	}

	@Override
	public QuadEmitter nominalFace(@Nullable Direction face) {
		this.wrapped.nominalFace(face);
		return this;
	}

	@Override
	public QuadEmitter cullFace(@Nullable Direction face) {
		this.wrapped.cullFace(face);
		return this;
	}

	@Override
	public QuadEmitter atlas(QuadAtlas quadAtlas) {
		this.wrapped.atlas(quadAtlas);
		return this;
	}

	@Override
	public QuadEmitter chunkLayer(ChunkSectionLayer layer) {
		this.wrapped.chunkLayer(layer);
		return this;
	}

	@Override
	public QuadEmitter itemRenderType(RenderType renderType) {
		this.wrapped.itemRenderType(renderType);
		return this;
	}

	@Override
	public QuadEmitter itemGlintRenderType(RenderType renderType) {
		this.wrapped.itemGlintRenderType(renderType);
		return this;
	}

	@Override
	public QuadEmitter itemGlintSpecialRenderType(RenderType renderType) {
		this.wrapped.itemGlintSpecialRenderType(renderType);
		return this;
	}

	@Override
	public QuadEmitter emissive(boolean emissive) {
		this.wrapped.emissive(emissive);
		return this;
	}

	@Override
	public QuadEmitter shadeDirectionOverride(@Nullable Direction shadeDirection) {
		this.wrapped.shadeDirectionOverride(shadeDirection);
		return this;
	}

	@Override
	public QuadEmitter ambientOcclusion(TriState ao) {
		this.wrapped.ambientOcclusion(ao);
		return this;
	}

	@Override
	public QuadEmitter foilType(ItemStackRenderState.@Nullable FoilType foilType) {
		this.wrapped.foilType(foilType);
		return this;
	}

	@Override
	public QuadEmitter shadeMode(ShadeMode mode) {
		this.wrapped.shadeMode(mode);
		return this;
	}

	@Override
	public QuadEmitter animated(boolean animated) {
		this.wrapped.animated(animated);
		return this;
	}

	@Override
	public QuadEmitter tintIndex(int tintIndex) {
		this.wrapped.tintIndex(tintIndex);
		return this;
	}

	@Override
	public QuadEmitter tag(int tag) {
		this.wrapped.tag(tag);
		return this;
	}

	@Override
	public QuadEmitter copyFrom(QuadView quad) {
		this.wrapped.copyFrom(quad);
		return this;
	}

	@Override
	public QuadEmitter fromBakedQuad(BakedQuad quad) {
		this.wrapped.fromBakedQuad(quad);
		return this;
	}

	@Override
	public QuadEmitter clear() {
		this.wrapped.clear();
		return this;
	}

	@Override
	public void pushTransform(QuadTransform transform) {
		this.wrapped.pushTransform(transform);
	}

	@Override
	public void popTransform() {
		this.wrapped.popTransform();
	}

	@Override
	public QuadEmitter emit() {
		this.wrapped.emit();
		return this;
	}

	@Override
	public float x(int vertexIndex) {
		return this.wrapped.x(vertexIndex);
	}

	@Override
	public float y(int vertexIndex) {
		return this.wrapped.y(vertexIndex);
	}

	@Override
	public float z(int vertexIndex) {
		return this.wrapped.z(vertexIndex);
	}

	@Override
	public float posByIndex(int vertexIndex, int coordinateIndex) {
		return this.wrapped.posByIndex(vertexIndex, coordinateIndex);
	}

	@Override
	public Vector3f copyPos(int vertexIndex, @Nullable Vector3f target) {
		return this.wrapped.copyPos(vertexIndex, target);
	}

	@Override
	public int color(int vertexIndex) {
		return this.wrapped.color(vertexIndex);
	}

	@Override
	public float u(int vertexIndex) {
		return this.wrapped.u(vertexIndex);
	}

	@Override
	public float v(int vertexIndex) {
		return this.wrapped.v(vertexIndex);
	}

	@Override
	public Vector2f copyUv(int vertexIndex, @Nullable Vector2f target) {
		return this.wrapped.copyUv(vertexIndex, target);
	}

	@Override
	public int lightmap(int vertexIndex) {
		return this.wrapped.lightmap(vertexIndex);
	}

	@Override
	public boolean hasNormal(int vertexIndex) {
		return this.wrapped.hasNormal(vertexIndex);
	}

	@Override
	public float normalX(int vertexIndex) {
		return this.wrapped.normalX(vertexIndex);
	}

	@Override
	public float normalY(int vertexIndex) {
		return this.wrapped.normalY(vertexIndex);
	}

	@Override
	public float normalZ(int vertexIndex) {
		return this.wrapped.normalZ(vertexIndex);
	}

	@Override
	public @Nullable Vector3f copyNormal(int vertexIndex, @Nullable Vector3f target) {
		return this.wrapped.copyNormal(vertexIndex, target);
	}

	@Override
	public Vector3fc faceNormal() {
		return this.wrapped.faceNormal();
	}

	@Override
	public Direction lightFace() {
		return this.wrapped.lightFace();
	}

	@Override
	public @Nullable Direction nominalFace() {
		return this.wrapped.nominalFace();
	}

	@Override
	public @Nullable Direction cullFace() {
		return this.wrapped.cullFace();
	}

	@Override
	public QuadAtlas atlas() {
		return this.wrapped.atlas();
	}

	@Override
	public ChunkSectionLayer chunkLayer() {
		return this.wrapped.chunkLayer();
	}

	@Override
	public RenderType itemRenderType() {
		return this.wrapped.itemRenderType();
	}

	@Override
	public RenderType itemGlintRenderType() {
		return this.wrapped.itemGlintRenderType();
	}

	@Override
	public RenderType itemGlintSpecialRenderType() {
		return this.wrapped.itemGlintSpecialRenderType();
	}

	@Override
	public boolean emissive() {
		return this.wrapped.emissive();
	}

	@Override
	public @Nullable Direction shadeDirectionOverride() {
		return this.wrapped.shadeDirectionOverride();
	}

	@Override
	public TriState ambientOcclusion() {
		return this.wrapped.ambientOcclusion();
	}

	@Override
	public ItemStackRenderState.@Nullable FoilType foilType() {
		return this.wrapped.foilType();
	}

	@Override
	public ShadeMode shadeMode() {
		return this.wrapped.shadeMode();
	}

	@Override
	public boolean animated() {
		return this.wrapped.animated();
	}

	@Override
	public int tintIndex() {
		return this.wrapped.tintIndex();
	}

	@Override
	public int tag() {
		return this.wrapped.tag();
	}

	@Override
	public void buffer(int overlayCoords, VertexConsumer vertexConsumer) {
		this.wrapped.buffer(overlayCoords, vertexConsumer);
	}

	@Override
	public void buffer(int overlayCoords, PoseStack.Pose pose, VertexConsumer vertexConsumer) {
		this.wrapped.buffer(overlayCoords, pose, vertexConsumer);
	}
}
