package io.github.fusionflux.portalcubed;

import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedSerializers;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.PortalCubedTabs;
import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.content.misc.MOTL;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.construct.set.MonoConstructSet;
import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;

import net.minecraft.world.level.block.Blocks;

import net.minecraft.world.level.block.RotatedPillarBlock;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalCubed implements ModInitializer {
	public static final String ID = "portalcubed";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static final Registrar REGISTRAR = new Registrar(ID);

	@Override
	public void onInitialize(ModContainer mod) {
		ModMetadata metadata = mod.metadata();
		LOGGER.info("Portal Cubed (" + metadata.version() + ") initializing...");
		LOGGER.info(MOTL.get());

		PortalCubedBlocks.init();
		PortalCubedItems.init();
		PortalCubedTabs.init();
		PortalCubedEntities.init();
		PortalCubedSerializers.init();

		PortalCubedSounds.init();

		ConstructManager.init();

		ConstructSet test = new MonoConstructSet(
				ItemTags.ANVIL,
				new Construct.Builder()
						.put(BlockPos.ZERO, new Construct.BlockInfo(
								Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z)
						))
						.put(BlockPos.ZERO.above(), Blocks.STONE)
						.build()
		);

		JsonElement json = ConstructSet.CODEC.encodeStart(JsonOps.INSTANCE, test)
				.getOrThrow(false, System.out::println);
		String string = new GsonBuilder().setPrettyPrinting().create().toJson(json);
		System.out.println(string);

		LOGGER.info("Portal Cubed (" + metadata.version() + ") initialized!");
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
