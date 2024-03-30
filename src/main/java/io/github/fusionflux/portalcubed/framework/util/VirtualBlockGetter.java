package io.github.fusionflux.portalcubed.framework.util;

import java.util.function.ToIntFunction;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

// Taken from Flywheel, licensed under MIT: https://github.com/Jozufozu/Flywheel/blob/96eb5ea47c44fc072a138efda1f6e646e2804d05/src/main/java/com/jozufozu/flywheel/lib/model/baked/VirtualBlockGetter.java
public abstract class VirtualBlockGetter implements BlockAndTintGetter {
	protected final VirtualLightEngine lightEngine;

	public VirtualBlockGetter(ToIntFunction<BlockPos> blockLightFunc, ToIntFunction<BlockPos> skyLightFunc) {
		lightEngine = new VirtualLightEngine(blockLightFunc, skyLightFunc, this);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return getBlockState(pos).getFluidState();
	}

	@Override
	public float getShade(Direction direction, boolean shaded) {
		return 1f;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return lightEngine;
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver resolver) {
		Biome plainsBiome = Minecraft.getInstance().getConnection().registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
		return resolver.getColor(plainsBiome, pos.getX(), pos.getZ());
	}
}
