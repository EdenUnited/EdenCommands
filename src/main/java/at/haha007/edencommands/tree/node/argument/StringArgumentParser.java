package at.haha007.edencommands.tree.node.argument;

public class StringArgumentParser implements ArgumentParser<String> {
    private static final StringArgumentParser std = new StringArgumentParser();

    public static StringArgumentParser stringParser() {
        return std;
    }

    public String parse(String s) {
        return s;
    }
}
