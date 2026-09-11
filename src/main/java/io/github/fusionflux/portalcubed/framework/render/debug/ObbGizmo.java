package io.github.fusionflux.portalcubed.framework.render.debug;

import java.util.Iterator;

import org.joml.Vector3dc;

import io.github.fusionflux.portalcubed.framework.extension.Vec3Ext;
import io.github.fusionflux.portalcubed.framework.shape.OBB;
import net.minecraft.gizmos.CuboidGizmo;
import net.minecraft.gizmos.Gizmo;
import net.minecraft.gizmos.GizmoPrimitives;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;

/// @see CuboidGizmo
public final class ObbGizmo implements Gizmo {
	private final Vec3[] vertices;
	private final GizmoStyle style;
	private final boolean coloredCornerStroke;

	public ObbGizmo(OBB box, GizmoStyle style, boolean coloredCornerStroke) {
		this.vertices = new Vec3[8];
		this.style = style;
		this.coloredCornerStroke = coloredCornerStroke;

		Iterator<Vector3dc> iterator = box.vertices().iterator();
		for (int i = 0; i < 8; i++) {
			this.vertices[i] = Vec3Ext.of(iterator.next());
		}
	}

	@Override
	public void emit(GizmoPrimitives primitives, float alphaMultiplier) {
		Vec3 v0 = this.vertices[0];
		Vec3 v1 = this.vertices[1];
		Vec3 v2 = this.vertices[2];
		Vec3 v3 = this.vertices[3];
		Vec3 v4 = this.vertices[4];
		Vec3 v5 = this.vertices[5];
		Vec3 v6 = this.vertices[6];
		Vec3 v7 = this.vertices[7];

		// converted from CuboidGizmo using the table in AABB mixin

		if (this.style.hasFill()) {
			int color = this.style.multipliedFill(alphaMultiplier);
			primitives.addQuad(v4, v6, v7, v5, color);
			primitives.addQuad(v0, v1, v3, v2, color);
			primitives.addQuad(v0, v2, v6, v4, color);
			primitives.addQuad(v1, v5, v7, v3, color);
			primitives.addQuad(v2, v3, v7, v6, color);
			primitives.addQuad(v0, v4, v5, v1, color);
		}

		if (this.style.hasStroke()) {
			int color = this.style.multipliedStroke(alphaMultiplier);
			float width = this.style.strokeWidth();
			primitives.addLine(v0, v4, this.coloredCornerStroke ? ARGB.multiply(color, -34953) : color, width);
			primitives.addLine(v0, v2, this.coloredCornerStroke ? ARGB.multiply(color, -8913033) : color, width);
			primitives.addLine(v0, v1, this.coloredCornerStroke ? ARGB.multiply(color, -8947713) : color, width);
			primitives.addLine(v4, v6, color, width);
			primitives.addLine(v6, v2, color, width);
			primitives.addLine(v2, v3, color, width);
			primitives.addLine(v3, v1, color, width);
			primitives.addLine(v1, v5, color, width);
			primitives.addLine(v5, v4, color, width);
			primitives.addLine(v3, v7, color, width);
			primitives.addLine(v5, v7, color, width);
			primitives.addLine(v6, v7, color, width);
		}
	}

	public static GizmoProperties add(OBB box, GizmoStyle style, boolean coloredCornerStroke) {
		return Gizmos.addGizmo(new ObbGizmo(box, style, coloredCornerStroke));
	}

	public static GizmoProperties add(OBB box, GizmoStyle style) {
		return add(box, style, false);
	}

	public static GizmoProperties add(OBB box, int argb) {
		return add(box, GizmoStyle.stroke(argb));
	}
}
