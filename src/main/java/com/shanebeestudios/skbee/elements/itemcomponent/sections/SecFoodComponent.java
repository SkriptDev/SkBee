package com.shanebeestudios.skbee.elements.itemcomponent.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.FoodProperties;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.List;

@SuppressWarnings({"DataFlowIssue", "UnstableApiUsage"})
@Name("ItemComponent - Food Component Apply")
@Description({"Apply a food component to any item making it an edible item.",
    "See [**McWiki Food Component**](https://minecraft.wiki/w/Data_component_format#food) for more details.",
    "",
    "**Entries/Sections**:",
    "- `nutrition` = The number of food points restored by this item when eaten. Must be a non-negative integer.",
    "- `saturation` = The amount of saturation restored by this item when eaten.",
    "- `can always eat` = If true, this item can be eaten even if the player is not hungry. Defaults to false. [Optional]"})
@Examples({"# Directly apply a food component to the player's tool",
    "apply food component to player's tool:",
    "\tnutrition: 5",
    "\tsaturation: 3",
    "",
    "# Create a new item and apply a food item to it",
    "set {_i} to 1 of book",
    "apply food component to {_i}:",
    "\tnutrition: 5",
    "\tsaturation: 3",
    "\tcan always eat: true",
    "give player 1 of {_i}"})
@Since("3.5.8")
public class SecFoodComponent extends Section {

    private static final EntryValidator.EntryValidatorBuilder VALIDATOR = EntryValidator.builder();

    static {
        VALIDATOR.addEntryData(new ExpressionEntryData<>("nutrition", null, false, Number.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("saturation", null, false, Number.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("can always eat", null, true, Boolean.class));
        Skript.registerSection(SecFoodComponent.class, "apply food component to %itemstacks%");
    }

    private Expression<ItemStack> items;
    private Expression<Number> nutrition;
    private Expression<Number> saturation;
    private Expression<Boolean> canAlwaysEat;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        EntryContainer container = VALIDATOR.build().validate(sectionNode);
        if (container == null) return false;

        this.items = (Expression<ItemStack>) exprs[0];
        this.nutrition = (Expression<Number>) container.getOptional("nutrition", false);
        this.saturation = (Expression<Number>) container.getOptional("saturation", false);
        this.canAlwaysEat = (Expression<Boolean>) container.getOptional("can always eat", false);
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        Number nutritionNum = this.nutrition.getSingle(event);
        Number saturationNum = this.saturation.getSingle(event);
        if (nutritionNum == null || saturationNum == null) return super.walk(event, false);

        int nutrition = Math.max(nutritionNum.intValue(), 0);
        float saturation = saturationNum.floatValue();

        boolean canAlwaysEat = this.canAlwaysEat != null ? this.canAlwaysEat.getSingle(event) : false;

        for (ItemStack itemStack : this.items.getArray(event)) {
            FoodProperties.Builder food = FoodProperties.food().nutrition(nutrition).saturation(saturation).canAlwaysEat(canAlwaysEat);
            itemStack.setData(DataComponentTypes.FOOD, food);
        }
        return super.walk(event, false);
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        return "apply food component to " + this.items.toString(e, d);
    }

}
