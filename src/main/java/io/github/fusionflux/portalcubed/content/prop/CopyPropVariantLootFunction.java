package io.github.fusionflux.portalcubed.content.prop;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.github.fusionflux.portalcubed.content.PortalCubedDataComponents;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed.data.loot.PortalCubedLootFunctions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
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

	CopyPropVariantLootFunction(List<LootItemCondition> predicates, NbtProvider source, boolean fromItem) {
		super(predicates);
		this.source = source;
		this.fromItem = fromItem;
	}

	@Override
	@NotNull
	public LootItemFunctionType<? extends LootItemConditionalFunction> getType() {
		return PortalCubedLootFunctions.COPY_PROP_VARIANT;
	}

	@Override
	@NotNull
	protected ItemStack run(ItemStack stack, LootContext context) {
		String key = this.fromItem ? Prop.VARIANT_FROM_ITEM_KEY : Prop.VARIANT_KEY;
		if (this.source.get(context) instanceof CompoundTag tag && tag.contains(key))
			stack.set(PortalCubedDataComponents.PROP_VARIANT, tag.getInt(key));
		return stack;
	}
}
