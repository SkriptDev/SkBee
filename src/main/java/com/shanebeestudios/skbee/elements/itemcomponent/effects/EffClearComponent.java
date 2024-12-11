package com.shanebeestudios.skbee.elements.itemcomponent.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import io.papermc.paper.datacomponent.DataComponentType;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
@Name("ItemComponent - Clear/Reset Components")
@Description({"Clear/reset components of an ItemStack.",
    "Clear will remove the component from the item completely.",
    "Reset will reset the component back to the original vanilla value."})
@Examples({"clear food component of player's tool",
    "clear tool component of player's tool",
    "clear attribute modifier components of player's tool"})
@Since("3.5.8")
public class EffClearComponent extends Effect {

    static {
        Skript.registerEffect(EffClearComponent.class,
            "(clear|:reset) %datacomponenttypes% component[s] of %itemstacks%");
    }

    private boolean reset;
    private Expression<DataComponentType> componentTypes;
    private Expression<ItemStack> itemTypes;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.reset = parseResult.hasTag("reset");
        this.componentTypes = (Expression<DataComponentType>) exprs[0];
        this.itemTypes = (Expression<ItemStack>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        for (ItemStack itemStack : this.itemTypes.getArray(event)) {
            for (DataComponentType dataComponentType : this.componentTypes.getArray(event)) {
                if (this.reset) {
                    itemStack.resetData(dataComponentType);
                } else {
                    itemStack.unsetData(dataComponentType);
                }
            }
        }
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        String clear = this.reset ? "reset " : "clear ";
        return clear + this.componentTypes.toString(e, d) + " components of " + this.itemTypes.toString(e, d);
    }

}
