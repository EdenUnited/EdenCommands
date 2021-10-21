package at.haha007.edencommands.annotations;

import at.haha007.edencommands.tree.node.CommandNode;
import at.haha007.edencommands.tree.node.LiteralCommandNode;
import at.haha007.edencommands.tree.node.argument.DoubleArgumentParser;
import at.haha007.edencommands.tree.node.argument.IntegerArgumentParser;

import static at.haha007.edencommands.tree.node.ArgumentCommandNode.argument;
import static at.haha007.edencommands.tree.node.argument.DoubleArgumentParser.doubleParser;
import static at.haha007.edencommands.tree.node.argument.IntegerArgumentParser.intParser;
import static at.haha007.edencommands.tree.node.argument.StringArgumentParser.stringParser;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public enum EnumCommandType {
    LITERAL {
        public CommandNode parse(String s) {
            return LiteralCommandNode.literal(s);
        }
    }, INTEGER {
        public CommandNode parse(String s) {
            String[] args = s.split(",");
            if (args.length == 3) {
                IntegerArgumentParser parser = intParser(parseInt(args[1]), parseInt(args[2]));
                return argument(args[0], parser);
            } else {
                return argument(args[0], intParser());
            }
        }
    }, DOUBLE {
        public CommandNode parse(String s) {
            String[] args = s.split(",");
            if (args.length == 3) {
                DoubleArgumentParser parser = doubleParser(parseDouble(args[1]), parseDouble(args[2]));
                return argument(args[0], parser);
            } else {
                return argument(args[0], doubleParser());
            }
        }
    }, STRING {
        public CommandNode parse(String s) {
            return argument(s, stringParser());
        }
    };

    public abstract CommandNode parse(String s);
}
