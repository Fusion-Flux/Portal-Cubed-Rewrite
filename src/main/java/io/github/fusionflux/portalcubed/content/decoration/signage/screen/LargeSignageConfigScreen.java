package io.github.fusionflux.portalcubed.content.decoration.signage.screen;

import java.util.Collection;

import org.apache.commons.lang3.function.TriConsumer;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlockEntity;
import io.github.fusionflux.portalcubed.framework.signage.Signage;
import io.github.fusionflux.portalcubed.framework.signage.SignageManager;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigureSignageConfigPacket;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class LargeSignageConfigScreen extends SignageConfigScreen {
	public static final ResourceLocation BACKGROUND = PortalCubed.id("textures/gui/container/signage/large_signage.png");

	public static final Component TITLE = Component.translatable("container.portalcubed.large_signage");
	public static final Component AGED_TITLE = Component.translatable("container.portalcubed.aged_large_signage");

	private final LargeSignageBlockEntity largeSignage;

    public LargeSignageConfigScreen(LargeSignageBlockEntity largeSignage) {
		super(largeSignage, largeSignage.aged ? AGED_TITLE : TITLE);
		this.largeSignage = largeSignage;
	}

	@Override
	protected ResourceLocation background() {
		return BACKGROUND;
	}

	@Override
	protected void addExtraElements(TriConsumer<Integer, Integer, LayoutElement> consumer) {

	}

	@Override
	protected Collection<Signage.Holder> signage() {
		return SignageManager.INSTANCE.allOfSize(Signage.Size.LARGE);
	}

	@Override
	protected Signage.Holder selectedSignage() {
		return this.largeSignage.holder();
	}

	@Override
	protected void updateSignage(Signage.Holder holder) {
		PortalCubedPackets.sendToServer(new ConfigureSignageConfigPacket.Large(this.largeSignage.getBlockPos(), holder));
	}
}
