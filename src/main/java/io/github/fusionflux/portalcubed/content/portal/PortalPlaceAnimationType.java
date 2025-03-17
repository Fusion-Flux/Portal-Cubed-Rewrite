package io.github.fusionflux.portalcubed.content.portal;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringRepresentable;

public enum PortalPlaceAnimationType implements StringRepresentable {
	POP_IN {
		@Override
		@Environment(EnvType.CLIENT)
		public void applyPose(float progress, PoseStack matrices) {
		}
	},
	EXPAND_ALL_CENTER {
		@Override
		@Environment(EnvType.CLIENT)
		public void applyPose(float progress, PoseStack matrices) {
			matrices.scale(progress, 1, progress);
		}
	},
	EXPAND_VERTICAL_TOP {
		@Override
		@Environment(EnvType.CLIENT)
		public void applyPose(float progress, PoseStack matrices) {
			matrices.translate(0, 0, 1);
			matrices.scale(1, 1, progress);
			matrices.translate(0, 0, -1);
		}
	},
	EXPAND_VERTICAL_CENTER {
		@Override
		@Environment(EnvType.CLIENT)
		public void applyPose(float progress, PoseStack matrices) {
			matrices.scale(1, 1, progress);
		}
	},
	EXPAND_VERTICAL_BOTTOM {
		@Override
		@Environment(EnvType.CLIENT)
		public void applyPose(float progress, PoseStack matrices) {
			matrices.translate(0, 0, -1);
			matrices.scale(1, 1, progress);
			matrices.translate(0, 0, 1);
		}
	},
	EXPAND_HORIZONTAL_LEFT {
		@Override
		@Environment(EnvType.CLIENT)
		public void applyPose(float progress, PoseStack matrices) {
			matrices.translate(0.5, 0, 0);
			matrices.scale(progress, 1, 1);
			matrices.translate(-0.5, 0, 0);
		}
	},
	EXPAND_HORIZONTAL_CENTER {
		@Override
		@Environment(EnvType.CLIENT)
		public void applyPose(float progress, PoseStack matrices) {
			matrices.scale(progress, 1, 1);
		}
	},
	EXPAND_HORIZONTAL_RIGHT {
		@Override
		@Environment(EnvType.CLIENT)
		public void applyPose(float progress, PoseStack matrices) {
			matrices.translate(-0.5, 0, 0);
			matrices.scale(progress, 1, 1);
			matrices.translate(0.5, 0, 0);
		}
	};

	public static final Codec<PortalPlaceAnimationType> CODEC = StringRepresentable.fromEnum(PortalPlaceAnimationType::values);

	public final String name = this.name().toLowerCase(Locale.ROOT);

	@Environment(EnvType.CLIENT)
	public abstract void applyPose(float progress, PoseStack matrices);

	@Override
	@NotNull
	public String getSerializedName() {
		return this.name;
	}
}
