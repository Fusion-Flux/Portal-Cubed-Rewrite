package io.github.fusionflux.portalcubed.content.portal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.FrontAndTop;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;

/**
 * Serializable data for a portal.
 */
public record PortalData(Vec3 origin, FrontAndTop orientation, PortalSettings settings) {
	public static final Codec<PortalData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Vec3.CODEC.fieldOf("origin").forGetter(PortalData::origin),
			StringRepresentable.fromEnum(FrontAndTop::values).fieldOf("orientation").forGetter(PortalData::orientation),
			PortalSettings.CODEC.fieldOf("settings").forGetter(PortalData::settings)
	).apply(instance, PortalData::new));
}
