package at.haha007.edencommands;

import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Accessors(fluent = true)
public final class LiteralCommandNode extends CommandNode<LiteralCommandNode> {
    @NotNull
    private final String literal;
    @Nullable
    private final Component tooltip;

    public LiteralCommandNode(@NotNull String literal) {
        this(literal, null);
    }

    public LiteralCommandNode(@NotNull String literal, @Nullable Component tooltip) {
        if (literal.contains(" ")) throw new IllegalArgumentException();
        this.literal = literal.toLowerCase();
        this.tooltip = tooltip;
    }

    protected @NotNull LiteralCommandNode getThis() {
        return this;
    }

    List<AsyncTabCompleteEvent.Completion> tabComplete(InternalContext context) {
        if (!testRequirement(context.sender()))
            return List.of();
        if (context.hasNext() && literal.equalsIgnoreCase(context.current())) {
            return super.tabComplete(context);
        }
        if (startsWith(literal, context.current())) {
            return List.of(AsyncTabCompleteEvent.Completion.completion(literal, tooltip));
        }
        return List.of();
    }

    boolean execute(InternalContext context) {
        if (!context.current().equalsIgnoreCase(literal))
            return false;
        return super.execute(context);
    }

    public @NotNull String name() {
        return literal;
    }

    private boolean startsWith(String literal, String start) {
        return literal.toLowerCase().startsWith(start.toLowerCase());
    }
}
