package com.shanebeestudios.skbee.elements.recipe.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.shanebeestudios.skbee.api.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmithingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Recipe - Ingredients of Recipe")
@Description("Get the ingredients from a recipe. Requires 1.13+")
@Examples({"set {_ing::*} to ingredients of recipe \"minecraft:diamond_sword\"",
    "loop recipes for iron ingot:",
    "\tset {_ing::*} to ingredients of recipe %loop-value%"})
@Since("1.4.0")
public class ExprIngredientsOfRecipe extends SimpleExpression<ItemStack> {

    static {
        Skript.registerExpression(ExprIngredientsOfRecipe.class, ItemStack.class, ExpressionType.COMBINED,
            "[(all [[of] the]|the)] ingredients (for|of) recipe %string%");
    }

    private Expression<String> recipe;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.recipe = (Expression<String>) exprs[0];
        return true;
    }

    @SuppressWarnings("deprecation")
    @Nullable
    @Override
    protected ItemStack[] get(Event event) {
        String recipeSingle = this.recipe.getSingle(event);
        if (recipeSingle == null) return null;

        NamespacedKey namespacedKey = Util.getNamespacedKey(recipeSingle, false);
        if (namespacedKey == null) return null;

        List<ItemStack> items = new ArrayList<>();
        Recipe recipe = Bukkit.getRecipe(namespacedKey);
        if (recipe instanceof Keyed keyed && keyed.getKey().equals(namespacedKey)) {
            if (recipe instanceof ShapedRecipe shapedRecipe) {
                String[] shape = shapedRecipe.getShape();
                int length = Math.max(shape.length, shape[0].length());
                for (int i = 0; i < Math.pow(length, 2); i++) {
                    items.add(new ItemStack(Material.AIR));
                }
                for (int i = 0; i < shape.length; i++) {
                    for (int x = 0; x < shape[i].length(); x++) {
                        ItemStack ingredient = shapedRecipe.getIngredientMap().get(shape[i].toCharArray()[x]);
                        if (ingredient != null) items.set(i * length + x, ingredient);
                    }
                }
            } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
                for (ItemStack ingredient : shapelessRecipe.getIngredientList()) {
                    if (ingredient == null) continue;
                    items.add(ingredient);
                }
            } else if (recipe instanceof CookingRecipe<?> cookingRecipe) {
                items.add(cookingRecipe.getInput());
            } else if (recipe instanceof MerchantRecipe merchantRecipe) {
                for (ItemStack ingredient : merchantRecipe.getIngredients()) {
                    if (ingredient == null) continue;
                    items.add(ingredient);
                }
            } else if (recipe instanceof StonecuttingRecipe stonecuttingRecipe) {
                items.add(stonecuttingRecipe.getInput());
            } else if (recipe instanceof SmithingRecipe smithingRecipe) {
                items.add(smithingRecipe.getBase().getItemStack());
                items.add(smithingRecipe.getAddition().getItemStack());
            }
        }

        return items.toArray(new ItemStack[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends ItemStack> getReturnType() {
        return ItemStack.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean d) {
        return "ingredients of recipe " + this.recipe.toString(e, d);
    }

}
