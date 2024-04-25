package io.github.fusionflux.portalcubed.content.misc;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class LemonTruckPlacer extends TrunkPlacer {
	public static final Codec<LemonTruckPlacer> CODEC = RecordCodecBuilder.create(
			instance ->
					// 32 is the max base height from trunk placer, 56 is the max base height plus max rand height, 4 is the number of horizontal directions, 16 is chunk size
					instance.group(
						Codec.intRange(1, 32).fieldOf("min_center_height").forGetter(placer -> placer.minCenterHeight),
						Codec.intRange(1, 56).fieldOf("max_center_height").forGetter(placer -> placer.maxCenterHeight),
						IntProvider.codec(1, 4).fieldOf("branch_count").forGetter(placer -> placer.branchCount),
						IntProvider.codec(1, 16).fieldOf("branch_distance").forGetter(placer -> placer.branchDistance)
					)
					.apply(instance, LemonTruckPlacer::new)
	);
	public static final TrunkPlacerType<LemonTruckPlacer> TYPE = Registry.register(BuiltInRegistries.TRUNK_PLACER_TYPE, PortalCubed.id("lemon_trunk_placer"), new TrunkPlacerType<>(CODEC));

	private final int minCenterHeight;
	private final int maxCenterHeight;
	private final IntProvider branchCount;
	private final IntProvider branchDistance;

	public LemonTruckPlacer(int minCenterHeight, int maxCenterHeight, IntProvider branchCount, IntProvider branchDistance) {
		super(minCenterHeight, Math.max(0, maxCenterHeight - minCenterHeight), 0);
		this.minCenterHeight = minCenterHeight;
		this.maxCenterHeight = maxCenterHeight;
		this.branchCount = branchCount;
		this.branchDistance = branchDistance;
	}

	@NotNull
	@Override
	protected TrunkPlacerType<?> type() {
		return TYPE;
	}

	@NotNull
	@Override
	public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader world, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, int height, BlockPos startPos, TreeConfiguration config) {
		setDirtAt(world, replacer, random, startPos.below(), config);

		// generate trunk
		for (int i = 0; i < height; i++) {
			this.placeLog(world, replacer, random, startPos.above(i), config);
		}
		BlockPos aboveTrunk = startPos.above(height);

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
				this.placeLog(world, replacer, random, branchPos, config, (j + 1) % 2 == 0 ? state -> state.trySetValue(RotatedPillarBlock.AXIS, branchDir.getAxis()) : Function.identity());
				branchPos.move(branchDir).move(Direction.UP);
			}
		}

		return foliageAttachments;
	}

	public static void init() {
	}
}
