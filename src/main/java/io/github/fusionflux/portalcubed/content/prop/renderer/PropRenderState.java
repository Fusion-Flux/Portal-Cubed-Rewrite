package io.github.fusionflux.portalcubed.content.prop.renderer;

import io.github.fusionflux.portalcubed.content.prop.PropType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class PropRenderState extends EntityRenderState {
	public PropType type;
	public int variant;

	public float yRot;
}
