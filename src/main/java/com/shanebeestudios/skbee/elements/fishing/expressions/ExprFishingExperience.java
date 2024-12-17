package com.shanebeestudios.skbee.elements.fishing.expressions;

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
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Fishing - Experience")
@Description("Get and modify the amount of experience dropped in a fishing event.")
@Examples({"on fishing:",
    "\tadd 10 to fishing experience",
    "\tsend fishing experience to player"})
@Since("2.14.0")
public class ExprFishingExperience extends SimpleExpression<Number> {

    static {
        Skript.registerExpression(ExprFishingExperience.class, Number.class, ExpressionType.SIMPLE,
            "fish[ing] [event] experience");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!getParser().isCurrentEvent(PlayerFishEvent.class)) {
            Skript.error("'" + parseResult.expr + "' can only be used in a fishing event");
            return false;
        }
        return true;
    }

    @Override
    protected @Nullable Number[] get(Event event) {
        if (!(event instanceof PlayerFishEvent playerFishEvent)) {
            return null;
        }
        return new Number[]{playerFishEvent.getExpToDrop()};
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> CollectionUtils.array(Number.class);
            case DELETE -> CollectionUtils.array();
            default -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
        if (!(event instanceof PlayerFishEvent playerFishEvent)) return;
        if (delta == null) {
            playerFishEvent.setExpToDrop(0);
            return;
        }
        int experience = delta[0] instanceof Number number ? number.intValue() : 0;
        int curentExperience = playerFishEvent.getExpToDrop();
        int value = 0;
        switch (mode) {
            case SET -> value = experience;
            case ADD -> value = curentExperience + experience;
            case REMOVE -> {
                value = curentExperience - experience;
                if (value < 0) value = 0;
            }
        }
        playerFishEvent.setExpToDrop(value);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "fishing experience";
    }

}
