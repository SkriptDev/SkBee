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
import com.shanebeestudios.skbee.elements.itemcomponent.sections.SecToolComponent.ToolComponentApplyRulesEvent;
import io.papermc.paper.datacomponent.item.Tool;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.util.TriState;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Tag;
import org.bukkit.block.BlockType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Name("ItemComponent - Tool Rule Apply")
@Description({"Apply rules to a tool component. You can add as many as you'd like.",
    "See [**McWiki Tool Component**](https://minecraft.wiki/w/Data_component_format#tool) for more details.",
    "",
    "**Entries/Sections**:",
    "NOTE: One of either `block types` or `block tag` MUST be used.",
    "`block types` = The blocks to match for this rule to apply.",
    "`block tag` = A Minecraft Tag to match for this rule to apply.",
    "`speed` = If the blocks match, overrides the default mining speed (Must be a positive number). [Optional]",
    "`correct for drops` = If the blocks match, overrides whether or not this tool is " +
        "considered correct to mine at its most efficient speed, and to drop items if the block's loot table requires it. [Optional]"})
@Examples({"set {_i} to a stick",
    "apply tool component to {_i}:",
    "\tdefault mining speed: 2.3",
    "\tdamage per block: 2",
    "\trules:",
    "\t\tapply tool rule:",
    "\t\t\tblock tag: minecraft block tag \"minecraft:dirt\"",
    "\t\t\tspeed: 1.0",
    "\t\t\tcorrect for drops: true",
    "\t\tapply tool rule:",
    "\t\t\tblock types: granite, stone and andesite",
    "\t\t\tspeed: 0.5",
    "\t\t\tcorrect for drops: false",
    "give {_i} to player"})
@Since("3.5.8")
public class SecToolRule extends Section {

    private static final EntryValidator.EntryValidatorBuilder VALIDATOR = EntryValidator.builder();

    static {
        if (Skript.classExists("org.bukkit.inventory.meta.components.ToolComponent")) {
            VALIDATOR.addEntryData(new ExpressionEntryData<>("block types", null, true, Material.class));
            VALIDATOR.addEntryData(new ExpressionEntryData<>("block tag", null, true, Tag.class));
            VALIDATOR.addEntryData(new ExpressionEntryData<>("speed", null, true, Number.class));
            VALIDATOR.addEntryData(new ExpressionEntryData<>("correct for drops", null, true, Boolean.class));
            Skript.registerSection(SecToolRule.class, "apply tool rule");
        }
    }

    private Expression<Material> blockTypes;
    private Expression<Tag<Material>> blockKey;
    private Expression<Number> speed;
    private Expression<Boolean> correctForDrops;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentEvent(ToolComponentApplyRulesEvent.class)) {
            Skript.error("Tool rules can only be applied in a 'rules' section of a tool component section.");
            return false;
        }

        EntryContainer container = VALIDATOR.build().validate(sectionNode);
        if (container == null) return false;

        this.blockTypes = (Expression<Material>) container.getOptional("block types", false);
        this.blockKey = (Expression<Tag<Material>>) container.getOptional("block tag", false);
        if (this.blockTypes == null && this.blockKey == null) {
            Skript.error("Either a 'block types' or 'block tag' entry needs to be used.");
            return false;
        }
        this.speed = (Expression<Number>) container.getOptional("speed", false);
        this.correctForDrops = (Expression<Boolean>) container.getOptional("correct for drops", false);
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable TriggerItem walk(Event event) {
        if (event instanceof ToolComponentApplyRulesEvent rulesEvent) {
            Tool.Builder toolBuilder = rulesEvent.getToolBuilder();

            Number speedNum = this.speed != null ? this.speed.getSingle(event) : null;
            Float speed = speedNum != null ? speedNum.floatValue() : null;
            if (speed != null && speed <= 0) speed = null;
            Boolean aBoolean = this.correctForDrops != null ? this.correctForDrops.getSingle(event) : null;
            TriState correctForDrops = aBoolean != null ? TriState.valueOf(aBoolean.toString()) : TriState.NOT_SET;

            if (this.blockTypes != null) {
                List<TypedKey<BlockType>> blockKeys = new ArrayList<>();
                for (Material material : this.blockTypes.getArray(event)) {
                    if (material.isBlock()) {
                        TypedKey<BlockType> key = TypedKey.create(RegistryKey.BLOCK, material.key());
                        blockKeys.add(key);
                    }
                }

                RegistryKeySet<BlockType> keys = RegistrySet.keySet(RegistryKey.BLOCK, blockKeys);
                toolBuilder.addRule(Tool.rule(keys, speed, correctForDrops));

            } else if (this.blockKey != null) {
                Tag<Material> bukkitTag = this.blockKey.getSingle(event);
                if (bukkitTag != null) {
                    TagKey<BlockType> tagKey = TagKey.create(RegistryKey.BLOCK, bukkitTag.key());
                    io.papermc.paper.registry.tag.Tag<BlockType> tag = Registry.BLOCK.getTag(tagKey);
                    Tool.Rule rule = Tool.rule(tag, speed, correctForDrops);
                    toolBuilder.addRule(rule);
                }
            }
        }
        return super.walk(event, false);
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        return "apply tool rule";
    }

}
