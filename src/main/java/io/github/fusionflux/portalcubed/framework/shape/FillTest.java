package io.github.fusionflux.portalcubed.framework.shape;

import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface FillTest {
	FillTest FALSE = pos -> false;

	boolean isFilled(Vector3f pos);

	static FillTest create(VoxelShape shape, Quaternionf rotation) {
		List<AABB> boxes = shape.toAabbs();
		return switch (boxes.size()) {
			case 0 -> FALSE;
			case 1 -> new Single(boxes.get(0), rotation);
			default -> new Multi(boxes, rotation);
		};
	}

	// simple common case, optimize the array away
	class Single implements FillTest {
		private final OBB box;

		public Single(AABB box, Quaternionf rotation) {
			this.box = new OBB(box, rotation);
		}

		@Override
		public boolean isFilled(Vector3f pos) {
			return this.box.contains(pos);
		}
	}

	class Multi implements FillTest {
		private final OBB[] boxes;

		public Multi(List<AABB> boxes, Quaternionf rotation) {
			this.boxes = new OBB[boxes.size()];
			for (int i = 0; i < this.boxes.length; i++) {
				this.boxes[i] = new OBB(boxes.get(i), rotation);
			}
		}

		@Override
		public boolean isFilled(Vector3f pos) {
			for (OBB box : boxes) {
				if (box.contains(pos)) {
					return true;
				}
			}
			return false;
		}
	}
}
