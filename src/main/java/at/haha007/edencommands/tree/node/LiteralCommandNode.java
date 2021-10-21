package at.haha007.edencommands.tree.node;

import at.haha007.edencommands.tree.CommandContext;
import lombok.Getter;

import java.util.List;
import java.util.Stack;

public class LiteralCommandNode extends CommandNode {

    @Getter
    private final String literal;

    private LiteralCommandNode(String literal) {
        this.literal = literal;
        tabCompletes(this::tabComplete);
    }

    public static LiteralCommandNode literal(String literal) {
        return new LiteralCommandNode(literal);
    }

    public void execute(Stack<String> command, CommandContext context) {
        super.execute(command, context);
    }

    private List<String> tabComplete(CommandContext context) {
        return List.of(literal);
    }

    @Override
    protected boolean matches(String s) {
        return literal.toLowerCase().startsWith(s.toLowerCase());
    }
}
