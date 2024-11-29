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
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Recipe - All Recipes")
@Description({"Get a list of all recipes. May be from a specific item, may be just Minecraft recipes or custom recipes.",
    "Due to some items having more than 1 recipe this may return multiple recipes. Requires 1.13+"})
@Examples("set {_recipes::*} to all recipes of iron ingot")
@Since("1.4.0")
public class ExprAllRecipes extends SimpleExpression<String> {

    static {
        Skript.registerExpression(ExprAllRecipes.class, String.class, ExpressionType.COMBINED,
            "[(all [[of] the]|the)] [(1:(mc|minecraft)|2:custom)] recipe[s] [(for|of) %-materials/itemstacks%]");
    }

    private int pattern;
    private Expression<Object> items;

    @SuppressWarnings({"unchecked", "null"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        pattern = parseResult.mark;
        items = (Expression<Object>) exprs[0];
        return true;
    }

    @Nullable
    @Override
    protected String[] get(Event e) {
        if (this.items != null) {
            return getRecipesForItems(this.items.getAll(e));
        }
        return getAllRecipes();
    }

    private String[] getAllRecipes() {
        List<String> recipes = new ArrayList<>();
        Bukkit.recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof Keyed) {
                NamespacedKey key = ((Keyed) recipe).getKey();
                if (pattern == 0 || isMinecraft(key) || isCustom(key)) {
                    recipes.add(key.toString());
                }
            }
        });
        return recipes.toArray(new String[0]);
    }

    private String[] getRecipesForItems(Object[] objects) {
        List<String> recipes = new ArrayList<>();
        for (Object object : objects) {
            ItemStack itemStack;
            if (object instanceof ItemStack is) itemStack = is;
            else if (object instanceof Material material && material.isItem()) itemStack = new ItemStack(material);
            else continue;

            for (Recipe recipe : Bukkit.getRecipesFor(itemStack)) {
                if (recipe instanceof Keyed keyed) {
                    NamespacedKey key = keyed.getKey();
                    if (pattern == 0 || isMinecraft(key) || isCustom(key)) {
                        recipes.add(key.toString());
                    }
                }
            }
        }
        return recipes.toArray(new String[0]);
    }

    private boolean isMinecraft(NamespacedKey key) {
        return pattern == 1 && key.getNamespace().equalsIgnoreCase("minecraft");
    }

    private boolean isCustom(NamespacedKey key) {
        return pattern == 2 && !key.getNamespace().equalsIgnoreCase("minecraft");
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends String> getReturnType() {
        return String.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean d) {
        return "recipes of " + items.toString(e, d);
    }

}
