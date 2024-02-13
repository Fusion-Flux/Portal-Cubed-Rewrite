package io.github.fusionflux.portalcubed.content.prop;

import java.util.function.Consumer;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class HammerItem extends Item {
	public HammerItem(Properties settings) {
		super(settings);
	}

	public static void destroyProp(Player user, Level level, Prop prop) {
		prop.remove(RemovalReason.KILLED);
		if (level instanceof ServerLevel serverLevel) {
			if (prop instanceof ButtonActivatedProp buttonActivated)
				buttonActivated.setActivated(false);
			consumeEntityLootTable(serverLevel, prop, prop::spawnAtLocation);
		}
	}

	private static void consumeEntityLootTable(ServerLevel level, Entity entity, Consumer<ItemStack> lootConsumer) {
		var lootTableId = entity.getType().getDefaultLootTable();
		var lootTable = level.getServer().getLootData().getLootTable(lootTableId);
		var builder = new LootParams.Builder(level)
			.withParameter(LootContextParams.THIS_ENTITY, entity)
			.withParameter(LootContextParams.ORIGIN, entity.position())
			.withParameter(LootContextParams.DAMAGE_SOURCE, level.damageSources().genericKill());
		lootTable.getRandomItems(builder.create(LootContextParamSets.ENTITY), 0, lootConsumer);
	}
}
