package io.github.fusionflux.portalcubed.content.portal.graphics.render.stencil;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongRBTreeSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public record PortalStencilShape(int width, int height, List<Segment> segments) implements Iterable<PortalStencilShape.Segment> {
	public static PortalStencilShape generate(int width, int height, BiFunction<Integer, Integer, Boolean> occupancyGetter) {
		LongSet open = new LongRBTreeSet();
		Long2ObjectMap<EnumSet<PortalStencilSide>> edgeGrid = new Long2ObjectArrayMap<>();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (occupancyGetter.apply(x, y)) {
					EnumSet<PortalStencilSide> sides = EnumSet.noneOf(PortalStencilSide.class);

					for (PortalStencilSide side : PortalStencilSide.values()) {
						int edgeX = x + switch (side) {
							case LEFT -> -1;
							case RIGHT -> 1;
							default -> 0;
						};
						int edgeY = y + switch (side) {
							case BOTTOM -> 1;
							case TOP -> -1;
							default -> 0;
						};

						if ((edgeX >= 0 && edgeX < width) && (edgeY >= 0 && edgeY < height)) {
							if (!occupancyGetter.apply(edgeX, edgeY)) {
								sides.add(side);
							}
						} else {
							sides.add(side);
						}
					}

					if (!sides.isEmpty()) {
						long packed = packPixelPosition(x, y);
						open.add(packed);
						edgeGrid.put(packed, sides);
					}
				}
			}
		}

		LongSet visited = new LongOpenHashSet();
		ImmutableList.Builder<Segment> list = ImmutableList.builder();

		for (long packed : open) {
			if (!visited.add(packed)) {
				continue;
			}

			int x = unpackPixelX(packed);
			int y = unpackPixelY(packed);
			int length = 0;
			EnumSet<PortalStencilSide> sides = edgeGrid.get(packed);

			//noinspection DuplicatedCode
			for (int x2 = x + 1; x2 < width; x2++) {
				long head = packPixelPosition(x2, y);
				if (open.contains(head) && visited.add(head)) {
					sides.addAll(edgeGrid.get(head));
					length++;
				} else {
					break;
				}
			}

			if (length > 0) {
				list.add(new Segment.Horizontal(x, x + length, y, sides));
				continue;
			}

			//noinspection DuplicatedCode
			for (int y2 = y + 1; y2 < height; y2++) {
				long head = packPixelPosition(x, y2);
				if (open.contains(head) && visited.add(head)) {
					sides.addAll(edgeGrid.get(head));
					length++;
				} else {
					break;
				}
			}

			if (length > 0) {
				list.add(new Segment.Vertical(x, y, y + length, sides));
				continue;
			}

			list.add(new Segment.Floating(x, y, sides));
		}

		return new PortalStencilShape(width, height, list.build());
	}

	private static long packPixelPosition(int x, int y) {
		return (((long) x) << 32) | (y & 0xFFFFFFFFL);
	}

	private static int unpackPixelX(long packed) {
		return (int) (packed >> 32);
	}

	private static int unpackPixelY(long packed) {
		return (int) packed;
	}

	/**
	 * Returns an iterator over elements of type {@code T}.
	 *
	 * @return an Iterator.
	 */
	@Override
	@NotNull
	public Iterator<Segment> iterator() {
		return this.segments.iterator();
	}

	public sealed interface Segment permits Segment.Floating, Segment.Horizontal, Segment.Vertical {
		int x();
		int y();

		Collection<PortalStencilSide> sides();


		record Floating(int x, int y, EnumSet<PortalStencilSide> sides) implements Segment { }

		record Horizontal(int x, int x2, int y, EnumSet<PortalStencilSide> sides) implements Segment { }

		record Vertical(int x, int y, int y2, EnumSet<PortalStencilSide> sides) implements Segment { }
	}
}
