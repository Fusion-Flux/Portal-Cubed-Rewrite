package io.github.fusionflux.portalcubed.content;

import io.github.fusionflux.portalcubed.PortalCubed;
import net.fabricmc.fabric.api.gamerule.v1.CustomGameRuleCategory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;

public class PortalCubedGameRules {
	public static final CustomGameRuleCategory CATEGORY = new CustomGameRuleCategory(
		PortalCubed.id(PortalCubed.ID),
		Component.translatable("gamerule.category.portalcubed").withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)
	);

	public static final GameRules.Key<GameRules.BooleanValue> PROP_SNATCHING = GameRuleRegistry.register(
		"propSnatching", CATEGORY, GameRuleFactory.createBooleanRule(true)
	);

	public static final GameRules.Key<GameRules.BooleanValue> TOXIC_GOO_SOURCE_CONVERSION = GameRuleRegistry.register(
		"toxicGooSourceConversion", CATEGORY, GameRuleFactory.createBooleanRule(true)
	);

	public static void init() {
	}
}
