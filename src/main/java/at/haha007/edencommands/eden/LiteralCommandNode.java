package at.haha007.edencommands.eden;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class LiteralCommandNode extends CommandNode<LiteralCommandNode> {

    @Getter
    @Accessors(fluent = true)
    @NotNull
    private final String literal;

    public LiteralCommandNode(@NotNull String literal) {
        if (literal.contains(" ")) throw new IllegalArgumentException();
        this.literal = literal.toLowerCase();
    }

    protected @NotNull LiteralCommandNode getThis() {
        return this;
    }

    List<String> tabComplete(InternalContext context) {
        if (!canUse(context.sender()))
            return List.of();
        if (context.hasNext() && literal.equalsIgnoreCase(context.current())) {
            return super.tabComplete(context);
        }
        if (startsWith(literal, context.current())) {
            return List.of(literal);
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
