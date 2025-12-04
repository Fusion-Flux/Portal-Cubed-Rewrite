package io.github.fusionflux.portalcubed.content.portal.graphics.color;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;

public record JebPortalColor(int colorOffset) implements PortalColor {
	public static final MapCodec<JebPortalColor> CODEC = Codec.INT.optionalFieldOf("color_offset", 0).xmap(JebPortalColor::new, JebPortalColor::colorOffset);
	public static final StreamCodec<ByteBuf, JebPortalColor> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(JebPortalColor::new, JebPortalColor::colorOffset);

	@Override
	public int get(float ticks) {
		// copied from SheepWoolLayer
		// TODO 1.21.6: use new logic, it moved
		int k = Mth.floor(ticks);
		int l = k / 25 + this.colorOffset;
		int m = DyeColor.values().length;
		int n = l % m;
		int o = (l + 1) % m;
		float h = (k % 25 + Mth.frac(ticks)) / 25f;
		int p = Sheep.getColor(DyeColor.byId(n));
		int q = Sheep.getColor(DyeColor.byId(o));
		return ARGB.lerp(h, p, q);
	}

	@Override
	public Type type() {
		return Type.JEB;
	}

	public static JebPortalColor parse(StringReader reader) {
		reader.skipWhitespace();
		try {
			return new JebPortalColor(reader.readInt());
		} catch (CommandSyntaxException ignored) {
			return new JebPortalColor(0);
		}
	}
}
