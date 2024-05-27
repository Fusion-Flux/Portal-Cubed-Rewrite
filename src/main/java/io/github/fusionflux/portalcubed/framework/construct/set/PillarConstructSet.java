package io.github.fusionflux.portalcubed.framework.construct.set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

import net.minecraft.world.level.block.Rotation;

import io.github.fusionflux.portalcubed.framework.gui.util.AdvancedTooltip.Builder;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

/**
 * A construct set that places one of two constructs based on the chosen axis.
 */
public class PillarConstructSet extends ConstructSet {
	public static Codec<PillarConstructSet> CODEC = ExtraCodecs.validate(
			RecordCodecBuilder.create(instance -> instance.group(
					TagKey.hashedCodec(Registries.ITEM).fieldOf("material").forGetter(c -> c.material),
					ExtraCodecs.POSITIVE_INT.optionalFieldOf("cost").forGetter(c -> Optional.of(c.cost)),
					Chooser.CODEC.fieldOf("chooser").forGetter(c -> c.chooser),
					Preview.CODEC.optionalFieldOf("preview", Preview.VERTICAL).forGetter(c -> c.preview),
					Construct.CODEC.fieldOf("horizontal").forGetter(c -> c.horizontal),
					Construct.CODEC.fieldOf("vertical").forGetter(c -> c.vertical)
			).apply(instance, PillarConstructSet::new)),
			PillarConstructSet::validate
	);

	public final Chooser chooser;
	private final Preview preview;
	private final Construct horizontal;
	private final Construct vertical;
	private final boolean explicitCost;

	public PillarConstructSet(TagKey<Item> material, Optional<Integer> cost, Chooser chooser, Preview preview, Construct horizontal, Construct vertical) {
		super(Type.PILLAR, material, ConstructSet.getCost(cost, horizontal), preview.choose(horizontal, vertical));
		this.chooser = chooser;
		this.preview = preview;
		this.horizontal = horizontal;
		this.vertical = vertical;
		this.explicitCost = cost.isPresent();
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

	@Override
	public void appendTooltip(Builder builder) {
		builder.add(this.chooser.tooltip);
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

	private static DataResult<PillarConstructSet> validate(PillarConstructSet constructSet) {
		if (constructSet.explicitCost)
			return DataResult.success(constructSet);

		int horizontalSize = constructSet.horizontal.getBlocks(Rotation.NONE).size();
		int verticalSize = constructSet.vertical.getBlocks(Rotation.NONE).size();
		if (horizontalSize != verticalSize) {
			// mismatch
			return DataResult.error(
					() -> "Horizontal and vertical constructs contain different numbers of blocks, so the cost must be specified explicitly."
			);
		}

		return DataResult.success(constructSet);
	}

	public enum Chooser implements StringRepresentable {
		CLICKED_FACE, PLACER_FACING;

		public static final Codec<Chooser> CODEC = StringRepresentable.fromEnum(Chooser::values);

		public final String name = this.name().toLowerCase(Locale.ROOT);
		public final Component tooltip = Component.translatable(
				"pillar_construct_set.chooser." + this.name + ".tooltip"
		).withStyle(ChatFormatting.GOLD);

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}
	}

	public enum Preview implements StringRepresentable {
		HORIZONTAL, VERTICAL;

		public static final Codec<Preview> CODEC = StringRepresentable.fromEnum(Preview::values);

		public final String name = this.name().toLowerCase(Locale.ROOT);

		public Construct choose(Construct horizontal, Construct vertical) {
			return this == HORIZONTAL ? horizontal : vertical;
		}

		@Override
		@NotNull
		public String getSerializedName() {
			return this.name;
		}
	}
}
