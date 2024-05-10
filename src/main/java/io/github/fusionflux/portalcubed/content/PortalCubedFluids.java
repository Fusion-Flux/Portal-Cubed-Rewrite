package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.goo.GooFluid;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FlowingFluid;

public class PortalCubedFluids {
	public static final FlowingFluid FLOWING_GOO = Registry.register(BuiltInRegistries.FLUID, PortalCubed.id("flowing_toxic_goo"), new GooFluid.Flowing());
	public static final FlowingFluid STILL_GOO = Registry.register(BuiltInRegistries.FLUID, PortalCubed.id("toxic_goo"), new GooFluid.Source());
	public static final Item GOO_BUCKET = Registry.register(BuiltInRegistries.ITEM, PortalCubed.id("toxic_goo_bucket"),new BucketItem(STILL_GOO, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

	public static void init() {
	}

}
