package io.github.fusionflux.portalcubed.framework.construct;

// TODO: This entire thing probably needs to be nuked - Max
public final class ConstructModelPool implements AutoCloseable {
//	private static final ModelEmitter EMITTER = new ModelEmitter();
//	private static final Supplier<DynamicTexture> LIGHT_TEXTURE = Suppliers.memoize(() -> {
//		DynamicTexture texture = new DynamicTexture(16, 16, false);
//		Objects.requireNonNull(texture.getPixels()).fillRect(0, 0, 16, 16, 0xFFFFFFFF);
//		texture.upload();
//		return texture;
//	});

//	private final Object2ReferenceOpenHashMap<ConfiguredConstruct, ModelInfo> models = new Object2ReferenceOpenHashMap<>();

//	public static ModelInfo buildModel(ConfiguredConstruct construct) {
//		VirtualConstructEnvironment environment = new VirtualConstructEnvironment(construct);
//
//		BlockRenderDispatcher renderDispatcher = Minecraft.getInstance().getBlockRenderer();
//		ModelBlockRenderer blockRenderer = renderDispatcher.getModelRenderer();
//		RandomSource random = RandomSource.create();
//
//		ModelBlockRenderer.enableCaching();
//		PoseStack matrices = new PoseStack();
//		construct.blocks.forEach((pos, info) -> {
//			BlockState state = info.state();
//			if (state.getRenderShape() == RenderShape.MODEL) {
//				EMITTER.prepare(ItemBlockRenderTypes.getChunkRenderType(state), renderDispatcher.getBlockModel(state));
//				matrices.pushPose();
//				matrices.translate(pos.getX(), pos.getY(), pos.getZ());
//				blockRenderer.tesselateBlock(environment, EMITTER.model, state, pos, matrices, EMITTER, true, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY);
//				matrices.popPose();
//			}
//		});
//		ModelBlockRenderer.clearCache();

//		List<ModelInfo.Buffer> buffers = new ArrayList<>();
//		EMITTER.end(buffers::add);
//		return new ModelInfo(buffers);
//		return null;
//	}

//	public ModelInfo getOrBuildModel(ConfiguredConstruct construct) {
//		return this.models.computeIfAbsent(construct, $ -> buildModel(construct));
//	}
//
	@Override
	public void close() {
//		this.models.values().forEach(ModelInfo::close);
//		this.models.clear();
	}
//
//	public record ModelInfo(List<Buffer> buffers) implements AutoCloseable {
//		public void draw(PoseStack matrices, Runnable extraRenderState) {
//			Matrix4f matrix = matrices.last().pose();
//			this.buffers.forEach(buffer -> buffer.draw(matrix, () -> {
//				extraRenderState.run();
//				RenderSystem.setShaderTexture(2, LIGHT_TEXTURE.get().getId());
//			}));
//		}
//
//		@Override
//		public void close() {
//			this.buffers.forEach(Buffer::close);
//			this.buffers.clear();
//		}
//
//		public record Buffer(RenderType renderType, VertexBuffer vertexBuffer) implements AutoCloseable {
//			public void draw(Matrix4f matrix, Runnable extraRenderState) {
//				this.renderType.setupRenderState();
//				extraRenderState.run();
//				this.vertexBuffer.bind();
//				this.vertexBuffer.drawWithShader(matrix, RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
//				VertexBuffer.unbind();
//				this.renderType.clearRenderState();
//			}
//
//			@Override
//			public void close() {
//				this.vertexBuffer.close();
//			}
//		}
//	}
//
//	private static final class ModelEmitter extends DelegatingVertexConsumer {
//		private final Reference2ReferenceMap<RenderType, ByteBufferBuilder> buffers = Util.make(new Reference2ReferenceOpenHashMap<>(), map -> {
//			for (RenderType renderType : RenderType.chunkBufferLayers()) {
//				map.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
//			}
//		});
//		private final Reference2ReferenceMap<RenderType, BufferBuilder> builders = new Reference2ReferenceOpenHashMap<>();
//		private final DelegateModel model = new DelegateModel();
//
//		private RenderType defaultRenderType;
//
//		private void prepare(RenderType defaultRenderType, BakedModel model) {
//			this.model.setDelegate(model);
//			this.defaultRenderType = defaultRenderType;
//		}
//
//		private void end(Consumer<ModelInfo.Buffer> resultConsumer) {
//			for (Map.Entry<RenderType, BufferBuilder> entry : this.builders.reference2ReferenceEntrySet()) {
//				BufferBuilder builder = entry.getValue();
//				MeshData meshData = builder.build();
//				if (meshData != null) {
//					VertexBuffer vertexBuffer = new VertexBuffer(BufferUsage.STATIC_WRITE);
//					vertexBuffer.bind();
//					vertexBuffer.upload(meshData);
//					VertexBuffer.unbind();
//					resultConsumer.accept(new ModelInfo.Buffer(entry.getKey(), vertexBuffer));
//				}
//			}
//
//			this.builders.clear();
//			this.model.setDelegate(null);
//			this.defaultRenderType = null;
//			this.delegate = null;
//		}
//
//		private void prepareForMaterial(RenderMaterial material) {
//			BlendMode blendMode = material.blendMode();
//			RenderType renderType = blendMode == BlendMode.DEFAULT ? ModelEmitter.this.defaultRenderType : blendMode.blockRenderLayer;
//			this.delegate = ModelEmitter.this.builders.computeIfAbsent(renderType, $ -> new BufferBuilder(this.buffers.get(renderType), renderType.mode(), renderType.format()));
//		}
//
//		private final class DelegateModel extends TransformingBakedModel {
//			private DelegateModel() {
//				super((quad -> {
//					ModelEmitter.this.prepareForMaterial(quad.material());
//					return true;
//				}));
//			}
//
//			private void setDelegate(BakedModel delegate) {
//				this.delegate = delegate;
//			}
//		}
//	}
}
