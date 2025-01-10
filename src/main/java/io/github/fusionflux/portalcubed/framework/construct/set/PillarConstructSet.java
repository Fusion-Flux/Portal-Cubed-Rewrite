package io.github.fusionflux.portalcubed.framework.construct.set;

import java.util.Locale;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.framework.construct.ConfiguredConstruct;
import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructPlacementContext;
import io.github.fusionflux.portalcubed.framework.gui.util.AdvancedTooltip.Builder;
import io.github.fusionflux.portalcubed.framework.util.EvenMoreCodecs;
import io.github.fusionflux.portalcubed.framework.util.PortalCubedStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Rotation;

/**
 * A construct set that places one of two constructs based on the chosen axis.
 */
public class PillarConstructSet extends ConstructSet {
	public static final MapCodec<PillarConstructSet> CODEC = EvenMoreCodecs.validate(
			RecordCodecBuilder.mapCodec(instance -> instance.group(
					TagKey.hashedCodec(Registries.ITEM).fieldOf("material").forGetter(c -> c.material),
					ExtraCodecs.POSITIVE_INT.optionalFieldOf("cost").forGetter(c -> Optional.of(c.cost)),
					Chooser.CODEC.fieldOf("chooser").forGetter(c -> c.chooser),
					PreviewSelection.CODEC.optionalFieldOf("preview", PreviewSelection.VERTICAL).forGetter(c -> c.previewSelection),
					Construct.CODEC.fieldOf("horizontal").forGetter(c -> c.horizontal),
					Construct.CODEC.fieldOf("vertical").forGetter(c -> c.vertical)
			).apply(instance, PillarConstructSet::new)),
			PillarConstructSet::validate
	);

	public static final StreamCodec<ByteBuf, PillarConstructSet> STREAM_CODEC = StreamCodec.composite(
			TagKey.streamCodec(Registries.ITEM), set -> set.material,
			ByteBufCodecs.VAR_INT, set -> set.cost,
			Chooser.STREAM_CODEC, set -> set.chooser,
			PreviewSelection.STREAM_CODEC, set -> set.previewSelection,
			Construct.STREAM_CODEC, set -> set.horizontal,
			Construct.STREAM_CODEC, set -> set.vertical,
			PillarConstructSet::new
	);

	public final Chooser chooser;
	private final PreviewSelection previewSelection;
	private final Construct horizontal;
	private final Construct vertical;
	private final boolean explicitCost;

	public PillarConstructSet(TagKey<Item> material, Optional<Integer> cost, Chooser chooser, PreviewSelection previewSelection, Construct horizontal, Construct vertical) {
		super(Type.PILLAR, material, ConstructSet.getCost(cost, horizontal), previewSelection.choose(horizontal, vertical));
		this.chooser = chooser;
		this.previewSelection = previewSelection;
		this.horizontal = horizontal;
		this.vertical = vertical;
		this.explicitCost = cost.isPresent();
	}

	private PillarConstructSet(TagKey<Item> material, int cost, Chooser chooser, PreviewSelection previewSelection, Construct horizontal, Construct vertical) {
		this(material, Optional.of(cost), chooser, previewSelection, horizontal, vertical);
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
		public static final StreamCodec<ByteBuf, Chooser> STREAM_CODEC = PortalCubedStreamCodecs.ofEnum(Chooser.class);

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

	public enum PreviewSelection implements StringRepresentable {
		HORIZONTAL, VERTICAL;

		public static final Codec<PreviewSelection> CODEC = StringRepresentable.fromEnum(PreviewSelection::values);
		public static final StreamCodec<ByteBuf, PreviewSelection> STREAM_CODEC = PortalCubedStreamCodecs.ofEnum(PreviewSelection.class);

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
