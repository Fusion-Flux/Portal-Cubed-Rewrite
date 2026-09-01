package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.type.WoodTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;

/// One-off registrations.
public final class PortalCubedMisc {
	public static final TicketType PORTAL_CHUNK_TICKET_TYPE = register(
			BuiltInRegistries.TICKET_TYPE, "portal", new TicketType(0, TicketType.FLAG_LOADING | TicketType.FLAG_PERSIST)
	);

	public static final ResourceKey<TrimMaterial> MAGNESIUM_TRIM_MATERIAL = ResourceKey.create(Registries.TRIM_MATERIAL, PortalCubed.id("magnesium"));

	// lemon block set stuff
	private static final Identifier lemonTypeId = PortalCubed.id("lemon");
	public static final BlockSetType LEMON_BLOCK_SET_TYPE = BlockSetTypeBuilder.copyOf(BlockSetType.OAK).register(lemonTypeId);
	public static final WoodType LEMON_WOOD_TYPE = WoodTypeBuilder.copyOf(WoodType.OAK).register(lemonTypeId, LEMON_BLOCK_SET_TYPE);

	@SuppressWarnings("SameParameterValue")
	private static <T> T register(Registry<T> registry, String name, T value) {
		return Registry.register(registry, PortalCubed.id(name), value);
	}
}
