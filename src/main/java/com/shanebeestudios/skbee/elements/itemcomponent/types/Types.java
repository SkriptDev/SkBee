package com.shanebeestudios.skbee.elements.itemcomponent.types;

import ch.njol.skript.registrations.Classes;
import com.shanebeestudios.skbee.api.wrapper.RegistryClassInfo;
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.Registry;

@SuppressWarnings("UnstableApiUsage")
public class Types {

    static {
        Classes.registerClass(RegistryClassInfo.create(Registry.DATA_COMPONENT_TYPE,
                DataComponentType.class, "datacomponenttype")
            .user("data ?component ?types?")
            .name("Data Component Type")
            .description("Represents the different types of data components for ItemStacks.")
            .since("INSERT VERSION"));
    }

}
