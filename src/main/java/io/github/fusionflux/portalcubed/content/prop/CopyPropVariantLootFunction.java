package io.github.fusionflux.portalcubed.content.prop;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders;

public class CopyPropVariantLootFunction extends LootItemConditionalFunction {
	public static final MapCodec<CopyPropVariantLootFunction> CODEC = RecordCodecBuilder.mapCodec(
			instance -> commonFields(instance)
					.and(
							instance.group(
									NbtProviders.CODEC.fieldOf("source").forGetter(function -> function.source),
									Codec.BOOL.optionalFieldOf("variant_from_item", true).forGetter(function -> function.fromItem)
							)
					)
					.apply(instance, CopyPropVariantLootFunction::new)
	);

	private final NbtProvider source;
	private final boolean fromItem;

	CopyPropVariantLootFunction(Optional<Holder<LootItemCondition>> condition, NbtProvider source, boolean fromItem) {
		super(condition);
		this.source = source;
		this.fromItem = fromItem;
	}

	@Override
	public MapCodec<? extends LootItemConditionalFunction> codec() {
		return CODEC;
	}

	@Override
	@NotNull
	protected ItemStack run(ItemStack stack, LootContext context) {
		String key = this.fromItem ? Prop.VARIANT_FROM_ITEM_KEY : Prop.VARIANT_KEY;
		if (this.source.get(context) instanceof CompoundTag tag) {
			tag.getInt(key).map(variant -> stack.set(PortalCubedDataComponents.PROP_VARIANT, variant));
		}
		return stack;
	}
}
