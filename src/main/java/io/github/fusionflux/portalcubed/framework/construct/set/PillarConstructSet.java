package io.github.fusionflux.portalcubed.framework.construct.set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

import net.minecraft.world.level.block.Rotation;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * A construct set that places one of two constructs based on the chosen axis.
 */
public class PillarConstructSet extends ConstructSet {
	public static Codec<PillarConstructSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TagKey.hashedCodec(Registries.ITEM).fieldOf("material").forGetter(c -> c.material),
			Chooser.CODEC.fieldOf("chooser").forGetter(c -> c.chooser),
			Construct.CODEC.fieldOf("horizontal").forGetter(c -> c.horizontal),
			Construct.CODEC.fieldOf("vertical").forGetter(c -> c.vertical)
	).apply(instance, PillarConstructSet::new));

	public final Chooser chooser;
	private final Construct horizontal;
	private final Construct vertical;

	public PillarConstructSet(TagKey<Item> material, Chooser chooser, Construct horizontal, Construct vertical) {
		super(Type.PILLAR, material, vertical);
		this.chooser = chooser;
		this.horizontal = horizontal;
		this.vertical = vertical;
	}

	@Override
	public ConfiguredConstruct choose(ConstructPlacementContext ctx) {
		Direction facing = ctx.placerFacing();
		Direction hFacing = ctx.placerHorizontalFacing();

		if (this.chooser == Chooser.CLICKED_FACE) {
			// pretend the placer is facing the clicked block
			facing = ctx.clickedFace().getOpposite();
			if (facing.getAxis().isHorizontal()) {
				hFacing = facing;
			}
		}

		Construct construct = facing.getAxis().isVertical() ? this.vertical : this.horizontal;
		// rotate based on horizontal direction
		Rotation rotation = getRotation(hFacing);

		Vec3i offset = Vec3i.ZERO;
		if (facing == Direction.UP) {
			// when placing on the ceiling, shift down to not intersect
			int height = construct.getBounds(rotation).getYSpan();
			offset = offset.below(height - 1);
		}

		return new ConfiguredConstruct(construct, rotation, offset);
	}

	public static Rotation getRotation(Direction direction) {
		return switch (direction) {
			case EAST -> Rotation.NONE;
			case SOUTH -> Rotation.CLOCKWISE_90;
			case WEST -> Rotation.CLOCKWISE_180;
			case NORTH -> Rotation.COUNTERCLOCKWISE_90;
			default -> throw new IllegalStateException("Horizontal direction was not horizontal");
		};
	}

	public enum Chooser implements StringRepresentable {
		CLICKED_FACE, PLACER_FACING;

		public static final Codec<Chooser> CODEC = StringRepresentable.fromEnum(Chooser::values);

		public final String name = this.name().toLowerCase(Locale.ROOT);

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}
	}
}
