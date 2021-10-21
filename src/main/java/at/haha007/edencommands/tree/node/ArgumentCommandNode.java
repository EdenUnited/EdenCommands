package at.haha007.edencommands.tree.node;

import at.haha007.edencommands.tree.CommandContext;
import at.haha007.edencommands.tree.node.argument.ArgumentParser;

import java.util.Stack;

public class ArgumentCommandNode<T> extends CommandNode {
    private final ArgumentParser<T> argumentParser;
    private final String key;

    private ArgumentCommandNode(String key, ArgumentParser<T> parser) {
        argumentParser = parser;
        this.key = key;
    }

    public static <T> ArgumentCommandNode<T> argument(String key, ArgumentParser<T> parser) {
        return new ArgumentCommandNode<>(key, parser);
    }

    public void execute(Stack<String> command, CommandContext context) {
        if (command.isEmpty() || context.isWasExecuted()) return;
        context.addParameter(key, argumentParser.parse(command.peek()));
        super.execute(command, context);
    }

    protected boolean matches(String s) {
        return argumentParser.parse(s) != null;
    }
}
