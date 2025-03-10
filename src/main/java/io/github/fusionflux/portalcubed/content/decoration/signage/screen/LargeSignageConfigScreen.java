package io.github.fusionflux.portalcubed.content.decoration.signage.screen;

import org.apache.commons.lang3.function.TriConsumer;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedRegistries;
import io.github.fusionflux.portalcubed.content.decoration.signage.Signage;
import io.github.fusionflux.portalcubed.content.decoration.signage.large.LargeSignageBlockEntity;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.serverbound.ConfigureSignageConfigPacket;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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
	protected ResourceKey<Registry<Signage>> registryKey() {
		return PortalCubedRegistries.LARGE_SIGNAGE;
	}

	@Override
	protected Holder<Signage> selectedImage() {
		return this.largeSignage.getImage();
	}

	@Override
	protected void configure(Holder<Signage> image) {
		PortalCubedPackets.sendToServer(new ConfigureSignageConfigPacket.Large(this.largeSignage.getBlockPos(), image));
	}
}
