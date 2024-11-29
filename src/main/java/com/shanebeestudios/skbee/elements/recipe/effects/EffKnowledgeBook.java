package com.shanebeestudios.skbee.elements.recipe.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.shanebeestudios.skbee.api.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

@Name("Recipe - Knowledge Book")
@Description({"Add/Remove custom or minecraft recipes to/from a knowledge book item.",
    "Optional string for plugin name, to add recipes from other plugins. Requires MC 1.13+"})
@Examples({"add custom recipe \"my_recipe\" to player's tool",
    "add recipe \"my_recipes:fancy_recipe\" to player's tool",
    "add minecraft recipe \"cooked_cod_from_campfire_cooking\" to {_book}",
    "add recipe \"minecraft:cooked_cod_from_campfire_cooking\" to {_book}",
    "add recipe \"some_recipe\" from plugin \"SomePlugin\" to player's tool",
    "add recipe \"someplugin:some_recipe\" to player's tool"})
@Since("1.0.0")
public class EffKnowledgeBook extends Effect {

    static {
        Skript.registerEffect(EffKnowledgeBook.class,
            "add [(custom|1:(mc|minecraft))] recipe[s] [with id[s]] %strings% [from plugin %-string%] to %itemstack%",
            "remove [(custom|1:(mc|minecraft))] recipe[s] [with id[s]] %strings% [from plugin %-string%] from %itemstack%");
    }

    private Expression<String> recipes;
    private Expression<ItemStack> book;
    private Expression<String> plugin;
    private boolean minecraft;
    private boolean add;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int pattern, Kleenean kleenean, ParseResult parseResult) {
        recipes = (Expression<String>) exprs[0];
        book = (Expression<ItemStack>) exprs[2];
        plugin = (Expression<String>) exprs[1];
        minecraft = parseResult.mark == 1;
        add = pattern != 1;
        return true;
    }

    @Override
    protected void execute(Event event) {

        ItemStack book = this.book.getSingle(event);
        if (book == null || book.getType() != Material.KNOWLEDGE_BOOK) return;

        Plugin plugin = null;
        if (this.plugin != null) {
            String pl = this.plugin.getSingle(event);
            assert pl != null;
            plugin = Bukkit.getPluginManager().getPlugin(pl);
        }

        String[] recipes = this.recipes.getAll(event);
        KnowledgeBookMeta meta = ((KnowledgeBookMeta) book.getItemMeta());

        List<NamespacedKey> allRecipes = new ArrayList<>(meta.getRecipes());
        for (String recipe : recipes) {
            NamespacedKey key;
            if (minecraft)
                key = NamespacedKey.minecraft(recipe);
            else if (plugin != null)
                key = new NamespacedKey(plugin, recipe);
            else
                key = Util.getNamespacedKey(recipe, false);

            if (add)
                allRecipes.add(key);
            else
                allRecipes.remove(key);
        }
        meta.setRecipes(allRecipes);
        book.setItemMeta(meta);
    }

    @Override
    public String toString(Event e, boolean d) {
        return (add ? "add" : "remove") + (minecraft ? " minecraft" : " custom") + " recipe(s) " + recipes.toString(e, d) +
            (add ? " to " : " from ") + book.toString(e, d);
    }

}
