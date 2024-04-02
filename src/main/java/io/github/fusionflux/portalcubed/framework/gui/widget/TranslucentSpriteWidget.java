package io.github.fusionflux.portalcubed.framework.gui.widget;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class TranslucentSpriteWidget extends AbstractWidget {
	private final ResourceLocation sprite;

	public TranslucentSpriteWidget(int width, int height, ResourceLocation sprite) {
		super(0, 0, width, height, CommonComponents.EMPTY);
		this.sprite = sprite;
		this.active = false;
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		RenderSystem.enableBlend();
		// z400 is where the advancement screen renders it's translucent textures
		graphics.pose().translate(0, 0, 400);
		graphics.blitSprite(this.sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
		RenderSystem.disableBlend();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
	}

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent event) {
		return null;
	}
}
