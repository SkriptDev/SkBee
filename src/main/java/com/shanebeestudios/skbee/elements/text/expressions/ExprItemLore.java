package com.shanebeestudios.skbee.elements.text.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import com.shanebeestudios.skbee.api.wrapper.ComponentWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("TextComponent - Item Lore")
@Description("Get/set the lore of an item using text components.")
@Examples("set component lore of player's tool to mini message from \"<rainbow>OOO RAINBOW LORE\"")
@Since("2.4.0")
public class ExprItemLore extends SimpleExpression<ComponentWrapper> {

    static {
        Skript.registerExpression(ExprItemLore.class, ComponentWrapper.class, ExpressionType.PROPERTY,
            "[the] component [item] lore of %itemstack%",
            "%itemstack%'[s] component [item] lore");
    }

    private Expression<ItemStack> item;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.item = (Expression<ItemStack>) exprs[0];
        return true;
    }

    @Override
    protected ComponentWrapper @Nullable [] get(Event event) {
        ItemStack itemStack = this.item.getSingle(event);
        if (itemStack == null) return null;

        ItemMeta meta = itemStack.getItemMeta();

        List<ComponentWrapper> components = new ArrayList<>();
        List<Component> lore = meta.lore();
        if (lore != null) {
            lore.forEach(component -> components.add(ComponentWrapper.fromComponent(component)));
        }
        return components.toArray(new ComponentWrapper[0]);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (mode == ChangeMode.SET || mode == ChangeMode.ADD) {
            return CollectionUtils.array(ComponentWrapper[].class, String[].class);
        }
        return null;
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
        ItemStack itemStack = this.item.getSingle(event);
        if (itemStack == null) return;

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null) return;

        List<Component> lores = mode == ChangeMode.ADD ? itemMeta.lore() : new ArrayList<>();
        if (lores == null) {
            lores = new ArrayList<>();
        }

        for (Object object : delta) {
            if (object instanceof ComponentWrapper component) {
                lores.add(component.getComponent());
            } else if (object instanceof String string) {
                lores.add(ComponentWrapper.fromText(string).getComponent());
            }
        }
        itemMeta.lore(lores);
        itemStack.setItemMeta(itemMeta);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NotNull Class<? extends ComponentWrapper> getReturnType() {
        return ComponentWrapper.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event e, boolean d) {
        return "component item lore of " + this.item.toString(e, d);
    }

}
