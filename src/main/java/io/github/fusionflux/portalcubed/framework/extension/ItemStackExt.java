package io.github.fusionflux.portalcubed.framework.extension;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;

public interface ItemStackExt {
	void pc$hurtAndBreakNoUnbreaking(int amount, ServerLevel level, @Nullable ServerPlayer player, Consumer<Item> onBreak);
}
