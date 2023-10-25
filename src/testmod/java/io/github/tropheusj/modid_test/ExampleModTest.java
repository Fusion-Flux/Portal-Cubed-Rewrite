package io.github.tropheusj.modid_test;

import net.minecraft.resources.ResourceLocation;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleModTest implements ModInitializer {
	public static final String ID = "modid_test";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize(ModContainer mod) {
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
