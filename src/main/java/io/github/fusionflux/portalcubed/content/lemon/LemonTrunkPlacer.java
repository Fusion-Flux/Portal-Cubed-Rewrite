package io.github.fusionflux.portalcubed.content.lemon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

public class LemonTrunkPlacer extends TrunkPlacer {
	public static final MapCodec<LemonTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
			instance ->
					// 32 is the max base height from trunk placer, 4 is the number of horizontal directions, 16 is chunk size
					instance.group(
						IntProviders.validateCodec(1, 32, UniformInt.MAP_CODEC.codec()).fieldOf("center_height").forGetter(placer -> placer.centerHeight),
						IntProviders.codec(1, 4).fieldOf("branch_count").forGetter(placer -> placer.branchCount),
						IntProviders.codec(1, 16).fieldOf("branch_distance").forGetter(placer -> placer.branchDistance)
					)
					.apply(instance, LemonTrunkPlacer::new)
	);
	public static final TrunkPlacerType<LemonTrunkPlacer> TYPE = Registry.register(BuiltInRegistries.TRUNK_PLACER_TYPE, PortalCubed.id("lemon_trunk_placer"), new TrunkPlacerType<>(CODEC));

	private final UniformInt centerHeight;
	private final IntProvider branchCount;
	private final IntProvider branchDistance;

	public LemonTrunkPlacer(UniformInt centerHeight, IntProvider branchCount, IntProvider branchDistance) {
		super(centerHeight.minInclusive(), centerHeight.maxInclusive() - centerHeight.minInclusive(), 0);
		this.centerHeight = centerHeight;
		this.branchCount = branchCount;
		this.branchDistance = branchDistance;
	}

	@NotNull
	@Override
	protected TrunkPlacerType<?> type() {
		return TYPE;
	}

	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, int treeHeight, BlockPos origin, TreeFeature tree) {
		placeBelowTrunkBlock(level, trunkSetter, random, origin.below(), tree);

		// generate trunk
		for (int i = 0; i < treeHeight; i++) {
			this.placeLog(level, trunkSetter, random, origin.above(i), tree);
		}
		BlockPos aboveTrunk = origin.above(treeHeight);

		// generate branches
		List<FoliagePlacer.FoliageAttachment> foliageAttachments = new ArrayList<>();
		int branchCount = this.branchCount.sample(random);
		BlockPos.MutableBlockPos branchPos = new BlockPos.MutableBlockPos();
		List<Direction> availableBranchDirections = new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
		for (int i = 0; i < branchCount; i++) {
			// There should always be at least one direction available but just to make sure lets default to north
			Direction branchDir = Util.getRandomSafe(availableBranchDirections, random).orElse(Direction.NORTH);
			availableBranchDirections.remove(branchDir);

			branchPos.set(aboveTrunk).move(branchDir);
			if (random.nextBoolean()) {
				Direction adjacentDir = random.nextBoolean() ? branchDir.getCounterClockWise() : branchDir.getClockWise();
				if (availableBranchDirections.remove(adjacentDir))
					branchPos.move(adjacentDir);
			}

			int branchDist = this.branchDistance.sample(random);
			for (int j = 0; j < branchDist; j++) {
				if (j == branchDist - 1)
					foliageAttachments.add(new FoliagePlacer.FoliageAttachment(branchPos.above(), 1, false));
				this.placeLog(level, trunkSetter, random, branchPos, tree, (j + 1) % 2 == 0 ? state -> state.trySetValue(RotatedPillarBlock.AXIS, branchDir.getAxis()) : Function.identity());
				branchPos.move(branchDir).move(Direction.UP);
			}
		}

		return foliageAttachments;
	}

	public static void init() {
	}
}
