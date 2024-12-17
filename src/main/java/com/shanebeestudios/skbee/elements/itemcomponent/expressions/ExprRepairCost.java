package com.shanebeestudios.skbee.elements.itemcomponent.expressions;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
@Name("ItemComponent - Repair Cost")
@Description({"The number of experience levels to add to the base level cost when repairing, combining, or renaming this item with an anvil.",
    "Must be a non-negative integer, defaults to 0."})
@Examples({"set repair cost of player's tool to 3",
    "add 2 to repair cost of player's tool",
    "subtract 1 from repair cost of player's tool",
    "reset repair cost of player's tool",
    "if repair cost of player's tool > 0:"})
@Since("3.6.0")
public class ExprRepairCost extends SimplePropertyExpression<ItemStack, Number> {

    static {
        register(ExprRepairCost.class, Number.class, "repair cost", "itemstacks");
    }

    @Override
    public @Nullable Number convert(ItemStack itemType) {
        return itemType.getData(DataComponentTypes.REPAIR_COST);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        if (mode == ChangeMode.SET || mode == ChangeMode.REMOVE || mode == ChangeMode.ADD)
            return CollectionUtils.array(Number.class);
        else if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) return CollectionUtils.array();
        return null;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
        int cost = delta != null && delta[0] instanceof Number num ? num.intValue() : 0;

        for (ItemStack itemStack : getExpr().getArray(event)) {
            if (mode == ChangeMode.DELETE) {
                itemStack.unsetData(DataComponentTypes.REPAIR_COST);
            } else if (mode == ChangeMode.RESET) {
                itemStack.resetData(DataComponentTypes.REPAIR_COST);
            } else {
                int previousCost = 0;
                if (itemStack.hasData(DataComponentTypes.REPAIR_COST)) {
                    previousCost = itemStack.getData(DataComponentTypes.REPAIR_COST);
                }
                int newCost = switch (mode) {
                    case ADD -> previousCost + cost;
                    case REMOVE -> previousCost - cost;
                    default -> cost;
                };
                itemStack.setData(DataComponentTypes.REPAIR_COST, Math.max(newCost, 0));
            }
        }
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "repair cost";
    }

    @Override
    public @NotNull Class<? extends Number> getReturnType() {
        return Number.class;
    }

}
