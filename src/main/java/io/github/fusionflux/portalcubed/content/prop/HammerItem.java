package io.github.fusionflux.portalcubed.content.prop;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class HammerItem extends Item {
	public HammerItem(Properties settings) {
		super(settings);
	}

	public static void destroyProp(Player user, Level world, Prop prop) {
		prop.remove(RemovalReason.KILLED);
		var propItem = prop.getPickResult();
		if (prop instanceof P2CubeProp cube)
			cube.setActivated(false);
		if (!prop.type.randomVariantOnPlace && prop.getVariant() != 0)
			propItem.getOrCreateTag().putInt("CustomModelData", prop.getVariant());
		if (prop.hasCustomName())
			propItem.getOrCreateTagElement("display").putString("Name", Component.Serializer.toJson(prop.getCustomName()));
		world.addFreshEntity(new ItemEntity(world, prop.getX(), prop.getY(), prop.getZ(), propItem));
	}
}
