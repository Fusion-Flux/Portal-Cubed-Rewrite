package io.github.fusionflux.portalcubed.content.cannon.screen.tab;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.cannon.CannonSettings;
import io.github.fusionflux.portalcubed.content.cannon.screen.CannonSettingsHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import io.github.fusionflux.portalcubed.framework.gui.widget.TitleWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.ToggleButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SettingsTab {
	public static final int X_OFF = 9;
	public static final int Y_OFF = 47;
	public static final int SETTING_TOGGLE_SIZE = 11;

	@SuppressWarnings("resource")
	public static void init(CannonSettingsHolder settings, PanelLayout layout) {
		var tab = LinearLayout.vertical();
		for (var setting : Setting.values()) {
			var button = LinearLayout.horizontal();
			button.defaultCellSetting().alignVertically(.9f);
			button.spacing(3);
			button.addChild(new ToggleButton(
				SETTING_TOGGLE_SIZE, SETTING_TOGGLE_SIZE, setting.sprite,
				() -> setting.settingGetter.test(settings.get()), v -> settings.update(s -> setting.settingSetter.apply(s, v))
			)).setTooltip(Tooltip.create(setting.description));
			button.addChild(new TitleWidget(setting.title, Minecraft.getInstance().font));
			tab.addChild(button);
			tab.addChild(SpacerElement.height(21));
		}
		layout.addChild(X_OFF, Y_OFF, tab);
	}

	public enum Setting {
		PREVIEW(CannonSettings::preview, CannonSettings::withPreview),
		REPLACE_MODE(CannonSettings::replaceMode, CannonSettings::withReplaceMode);

		public final String name = this.name().toLowerCase(Locale.ROOT);
		public final Component title = ConstructionCannonScreen.translate("tab.settings." + this.name);
		public final Component description = ConstructionCannonScreen.translate("tab.settings." + this.name + ".description");
		public final ResourceLocation sprite = PortalCubed.id("construction_cannon/settings_tab/" + this.name + "_toggle");
		public final Predicate<CannonSettings> settingGetter;
		public final BiFunction<CannonSettings, Boolean, CannonSettings> settingSetter;

		Setting(Predicate<CannonSettings> settingGetter, BiFunction<CannonSettings, Boolean, CannonSettings> settingSetter) {
			this.settingGetter = settingGetter;
			this.settingSetter = settingSetter;
		}
	}
}
