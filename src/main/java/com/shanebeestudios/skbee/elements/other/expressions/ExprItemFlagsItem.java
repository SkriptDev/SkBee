package com.shanebeestudios.skbee.elements.other.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("ItemFlag - Item with ItemFlags")
@Description("Get an item with ItemFlags.")
@Examples({"set {_sword} to diamond sword with all item flags",
    "set {_sword} to diamond sword of sharpness 3 with hide enchants item flag",
    "set {_sword} to diamond sword of sharpness 3 with item flag hide enchants",
    "give player fishing rod of lure 10 with item flag hide enchants",
    "give player potion of extended regeneration with hide enchants itemflag",
    "give player netherite leggings with itemflag hide attributes"})
@Since("3.4.0")
public class ExprItemFlagsItem extends SimpleExpression<ItemStack> {

    static {
        Skript.registerExpression(ExprItemFlagsItem.class, ItemStack.class, ExpressionType.COMBINED,
            "%itemstack% with all item[ ]flags",
            "%itemstack% with item[ ]flag[s] %itemflags%",
            "%itemstack% with %itemflags% item[ ]flag[s]");
    }

    private int pattern;
    private Expression<ItemStack> itemStack;
    private Expression<ItemFlag> itemFlags;

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        this.itemStack = (Expression<ItemStack>) exprs[0];
        if (matchedPattern > 0) {
            this.itemFlags = (Expression<ItemFlag>) exprs[1];
        }
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected ItemStack @Nullable [] get(Event event) {
        ItemStack itemStack = this.itemStack.getSingle(event);
        if (itemStack == null) return null;

        itemStack = itemStack.clone();
        ItemMeta itemMeta = itemStack.getItemMeta();
        ItemFlag[] flags = this.pattern == 0 ? ItemFlag.values() : this.itemFlags.getArray(event);
        itemMeta.addItemFlags(flags);
        itemStack.setItemMeta(itemMeta);
        return new ItemStack[]{itemStack};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public @NotNull String toString(@Nullable Event e, boolean d) {
        if (this.pattern == 0) return this.itemStack.toString(e, d) + " with all item flags";
        return this.itemStack.toString(e, d) + " with item flag[s] " + this.itemFlags.toString(e, d);
    }

}
