package at.haha007.edencommands.tree.node;

import lombok.Getter;

import java.util.List;

public class LiteralCommandNode extends CommandNode {

    @Getter
    private final String literal;

    private LiteralCommandNode(String literal) {
        this.literal = literal;
        tabCompletes(c -> List.of(literal));
    }

    public static LiteralCommandNode literal(String literal) {
        return new LiteralCommandNode(literal);
    }

    @Override
    protected boolean matches(String s) {
        return literal.toLowerCase().equalsIgnoreCase(s.toLowerCase());
    }
}
