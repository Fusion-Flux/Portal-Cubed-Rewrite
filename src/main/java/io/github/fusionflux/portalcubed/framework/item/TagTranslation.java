package io.github.fusionflux.portalcubed.framework.item;

import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * Tag translation, based on EMI.
 * For a given tag example:folder/tag_name, the key will be tag.item.example.folder.tag_name
 */
public class TagTranslation {
	public static Component translate(TagKey<Item> tag) {
		String id = tag.location().toString();
		String key = "tag.item." + id.replace(':', '.').replace('/', '.');

		if (Language.getInstance().has(key)) {
			return Component.translatable(key);
		} else {
			return Component.literal('#' + id);
		}
	}
}
