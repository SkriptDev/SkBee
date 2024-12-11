package com.shanebeestudios.skbee.elements.itemcomponent.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
@Name("ItemComponent - Enchantment Glint Override")
@Description({"Represents the enchantment glint override of an item.",
    "Overrides the enchantment glint effect on an item. When `true`, the item will display a glint, even without enchantments.",
    "When `false`, the item will not display a glint, even with enchantments.",
    "**Note**: If no override is applied, will return null.",
    "**Changers**:",
    "- `set` = Allows you to override the glint.",
    "- `delete` = Removes this component from the item.",
    "- `reset` = Will reset back to the vanilla value of the item."})
@Examples({"set glint override of player's tool to true",
    "set glint override of player's tool to false",
    "reset glint override of player's tool",
    "delete glint override of player's tool"})
@Since("3.6.0")
public class ExprEnchantmentGlintOverride extends SimplePropertyExpression<ItemStack, Boolean> {

    static {
        if (Skript.methodExists(ItemMeta.class, "getEnchantmentGlintOverride")) {
            register(ExprEnchantmentGlintOverride.class, Boolean.class,
                "[enchantment] glint [override]", "itemstacks");
        }
    }

    @Override
    public @Nullable Boolean convert(ItemStack itemStack) {
        return itemStack.getData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (mode == ChangeMode.SET) return CollectionUtils.array(Boolean.class);
        else if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) return CollectionUtils.array();
        return null;
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
        Boolean glint = delta != null && delta[0] instanceof Boolean bool ? bool : null;
        for (ItemStack itemStack : getExpr().getArray(event)) {
            if (mode == ChangeMode.DELETE) {
                itemStack.unsetData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
            } else if (mode == ChangeMode.RESET) {
                itemStack.resetData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
            } else if (glint != null) {
                itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glint);
            }
        }
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "enchantment glint override";
    }

    @Override
    public @NotNull Class<? extends Boolean> getReturnType() {
        return Boolean.class;
    }

}
