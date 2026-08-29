package io.github.fusionflux.portalcubed.framework.registration.gamerule;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public final class GameRuleHelper {
	private final Registrar registrar;

	public GameRuleHelper(Registrar registrar) {
		this.registrar = registrar;
	}

	public CategoryHelper createCategory(String name) {
		Identifier id = this.registrar.id(name);
		GameRuleCategory category = GameRuleCategory.register(id);
		return new CategoryHelper(category, this.registrar);
	}
}
