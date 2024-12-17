package com.shanebeestudios.skbee.api.util;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.Tag;
import org.bukkit.Keyed;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for {@link Tag Tags}
 */
public class TagUtils {

    /**
     * Check if Tags are supported
     */
    public static final boolean HAS_TAG = ch.njol.skript.bukkitutil.TagUtils.HAS_TAG;

    /**
     * Get all Materials from a tag
     * <p>
     * Shortcut method for Skript
     *
     * @param tag Tag to get materials from
     * @return List of materials from tag
     */
    @SuppressWarnings("UnstableApiUsage")
    public static List<Material> getItemMaterialsFromTag(Tag<?> tag) {
        List<Material> materials = new ArrayList<>();
        if (tag.registryKey() == RegistryKey.ITEM) {
            for (Keyed tagValue : ch.njol.skript.bukkitutil.TagUtils.getTagValues(tag)) {
                if (tagValue instanceof Material material && material.isItem()) materials.add(material);
            }
        }
        return materials;
    }

}
