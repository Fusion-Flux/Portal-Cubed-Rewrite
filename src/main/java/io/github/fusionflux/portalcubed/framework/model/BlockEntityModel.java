package io.github.fusionflux.portalcubed.framework.model;

import java.util.function.Function;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class BlockEntityModel<T extends BlockEntity> extends Model {
	protected BlockEntityModel(ModelPart root) {
		this(root, RenderType::entityCutoutNoCull);
	}

	protected BlockEntityModel(ModelPart root, Function<ResourceLocation, RenderType> function) {
		super(root, function);
	}

	public void setupAnim(T entity, float tickDelta) {
		this.resetPose();
	}
}
