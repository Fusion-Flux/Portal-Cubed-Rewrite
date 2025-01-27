package io.github.fusionflux.portalcubed.content.cannon.screen.tab;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.cannon.CannonSettings;
import io.github.fusionflux.portalcubed.content.cannon.screen.CannonSettingsHolder;
import io.github.fusionflux.portalcubed.content.cannon.screen.ConstructionCannonScreen;
import io.github.fusionflux.portalcubed.framework.gui.layout.PanelLayout;
import io.github.fusionflux.portalcubed.framework.gui.widget.SliderWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.TitleWidget;
import io.github.fusionflux.portalcubed.framework.gui.widget.ToggleButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SettingsTab {
	public static final int X_OFF = 9;
	public static final int Y_OFF = 47;
	public static final int SETTING_TOGGLE_SIZE = 13;
	public static final Component PREVIEW_OPACITY_SLIDER_TITLE = ConstructionCannonScreen.translate("tab.settings.preview_opacity");
	public static final String PREVIEW_OPACITY_SLIDER_DESCRIPTION = "tab.settings.preview_opacity.description";
	public static final ResourceLocation PREVIEW_OPACITY_SLIDER_SPRITE = PortalCubed.id("construction_cannon/settings_tab/preview_opacity_slider");
	public static final int PREVIEW_OPACITY_SLIDER_WIDTH = 158;

	public static void init(CannonSettingsHolder settings, PanelLayout layout) {
		Font font = Minecraft.getInstance().font;

		SliderWidget previewOpacitySlider = new SliderWidget(
				PREVIEW_OPACITY_SLIDER_SPRITE, PREVIEW_OPACITY_SLIDER_WIDTH,
				settings.get().previewOpacity(), slider -> {
					settings.update(b -> b.setPreviewOpacity(slider.handlePos()));
					Tooltip tooltip = createPreviewOpacityTooltip(settings);
					slider.setTooltip(tooltip);
				}
		);
		previewOpacitySlider.setTooltip(createPreviewOpacityTooltip(settings));

		LinearLayout tab = LinearLayout.vertical();
		{
			LinearLayout toggles = tab.addChild(LinearLayout.vertical());
			toggles.spacing(9);
			for (ToggleSetting setting : ToggleSetting.values()) {
				LinearLayout button = LinearLayout.horizontal();
				button.defaultCellSetting().alignVertically(.7f);
				button.spacing(3);
				button.addChild(
					new ToggleButton(
						SETTING_TOGGLE_SIZE, SETTING_TOGGLE_SIZE, setting.sprite,
						() -> setting.settingGetter.test(settings.get()), v -> {
							if (setting == ToggleSetting.PREVIEW)
								previewOpacitySlider.active = v;
							settings.update(s -> setting.settingSetter.apply(s, v));
						}
					)
				).setTooltip(Tooltip.create(setting.description));
				button.addChild(new TitleWidget(setting.title, font));
				toggles.addChild(button);
			}
		}

		tab.addChild(SpacerElement.height(7));

		{
			LinearLayout slider = tab.addChild(LinearLayout.vertical());
			slider.addChild(new TitleWidget(PREVIEW_OPACITY_SLIDER_TITLE, font));
			slider.addChild(previewOpacitySlider)
					.active = settings.get().preview();
		}
		layout.addChild(X_OFF, Y_OFF, tab);
	}

	private static Tooltip createPreviewOpacityTooltip(CannonSettingsHolder settings) {
		return Tooltip.create(ConstructionCannonScreen.translate(PREVIEW_OPACITY_SLIDER_DESCRIPTION, Mth.floor((double) settings.get().previewOpacity() * 100)));
	}

	public enum ToggleSetting {
		REPLACE_MODE(CannonSettings::replaceMode, CannonSettings.Builder::setReplaceMode),
		PREVIEW(CannonSettings::preview, CannonSettings.Builder::setPreview);

		public final String name = this.name().toLowerCase(Locale.ROOT);
		public final Component title = ConstructionCannonScreen.translate("tab.settings." + this.name);
		public final Component description = ConstructionCannonScreen.translate("tab.settings." + this.name + ".description");
		public final ResourceLocation sprite = PortalCubed.id("construction_cannon/settings_tab/" + this.name + "_toggle");
		public final Predicate<CannonSettings> settingGetter;
		public final BiFunction<CannonSettings.Builder, Boolean, CannonSettings.Builder> settingSetter;

		ToggleSetting(Predicate<CannonSettings> settingGetter, BiFunction<CannonSettings.Builder, Boolean, CannonSettings.Builder> settingSetter) {
			this.settingGetter = settingGetter;
			this.settingSetter = settingSetter;
		}
	}
}
