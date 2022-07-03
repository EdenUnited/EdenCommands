package at.haha007.edencommands.eden;

import at.haha007.edencommands.eden.argument.Argument;
import at.haha007.edencommands.eden.argument.ParsedArgument;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Accessors(fluent = true)
public final class ArgumentCommandNode<T> extends CommandNode<ArgumentCommandNode<T>> {
    @NotNull
    private final String key;
    @Getter
    private final Argument<T> argument;

    public ArgumentCommandNode(@NotNull String key, Argument<T> argument) {
        this.key = key;
        this.argument = argument;
    }

    protected @NotNull ArgumentCommandNode<T> getThis() {
        return this;
    }

    List<String> tabComplete(InternalContext context) {
        try {
            ParsedArgument<T> parse = argument.parse(context.context());
            context = context.next(parse.pointerIncrements() - 1);
            if (context.hasNext()) {
                return super.tabComplete(context.next(parse.pointerIncrements() - 1));
            }
            return argument.tabComplete(context.context());
        } catch (CommandException e1) {
            return argument.tabComplete(context.context());
        }
    }

    boolean execute(InternalContext context) {
        try {
            ParsedArgument<T> parse = argument.parse(context.context());
            context.putArgument(key, parse);
            context = context.next(parse.pointerIncrements() - 1);
            return super.execute(context);
        } catch (CommandException e) {
            e.sendErrorMessage(context.sender());
            return true;
        }
    }

    public @NotNull String name() {
        return key;
    }
}
