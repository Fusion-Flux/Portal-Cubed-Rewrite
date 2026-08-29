package io.github.fusionflux.portalcubed.framework.registration.gamerule;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public final class CategoryHelper {
	public final GameRuleCategory category;
	private final Registrar registrar;

	public CategoryHelper(GameRuleCategory category, Registrar registrar) {
		this.category = category;
		this.registrar = registrar;
	}

	public GameRule<Boolean> createBool(String name, boolean defaultValue) {
		Identifier id = this.registrar.id(name);
		return GameRuleBuilder.forBoolean(defaultValue).category(this.category).buildAndRegister(id);
	}

	public GameRule<Integer> createInt(String name, int defaultValue, int min, int max) {
		Identifier id = this.registrar.id(name);
		return GameRuleBuilder.forInteger(defaultValue).range(min, max).category(this.category).buildAndRegister(id);
	}
}
