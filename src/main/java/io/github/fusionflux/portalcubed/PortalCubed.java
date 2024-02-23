package io.github.fusionflux.portalcubed;

import java.util.List;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.PortalCubedItems;
import io.github.fusionflux.portalcubed.content.PortalCubedMenus;
import io.github.fusionflux.portalcubed.content.PortalCubedSerializers;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.PortalCubedTabs;
import io.github.fusionflux.portalcubed.framework.construct.Construct;
import io.github.fusionflux.portalcubed.framework.construct.ConstructManager;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.framework.construct.set.ConstructSet;
import io.github.fusionflux.portalcubed.framework.construct.set.MonoConstructSet;
import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.RandomSource;

import net.minecraft.world.item.Items;

import net.minecraft.world.level.block.Blocks;

import net.minecraft.world.level.block.RotatedPillarBlock;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
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
		Util.getRandomSafe(List.copyOf(metadata.contributors()), RandomSource.create()).ifPresent(contributor -> {
			LOGGER.info("I loved the part when " + contributor.name() + " said \"It's Portaln' time\" and portal'd all over the place");
		});

		PortalCubedBlocks.init();
		PortalCubedItems.init();
		PortalCubedTabs.init();
		PortalCubedEntities.init();
		PropType.init();
		PortalCubedSerializers.init();
		PortalCubedMenus.init();

		PortalCubedSounds.init();

		ConstructManager.init();

		ConstructSet test = new MonoConstructSet(
				Items.GOLD_BLOCK,
				new Construct.Builder()
						.put(BlockPos.ZERO, new Construct.BlockInfo(
								Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z)
						))
						.build()
		);

		JsonElement json = ConstructSet.CODEC.encodeStart(JsonOps.INSTANCE, test)
				.getOrThrow(false, System.out::println);
		String string = new GsonBuilder().setPrettyPrinting().create().toJson(json);
		System.out.println(string);
	}

	public static ResourceLocation id(String path) {
		return new ResourceLocation(ID, path);
	}
}
