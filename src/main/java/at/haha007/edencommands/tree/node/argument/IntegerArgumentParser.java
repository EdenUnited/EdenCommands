package at.haha007.edencommands.tree.node.argument;

public class IntegerArgumentParser implements ArgumentParser<Integer> {
    private static final IntegerArgumentParser std = new IntegerArgumentParser();

    public static IntegerArgumentParser intParser() {
        return std;
    }

    public static IntegerArgumentParser intParser(int min, int max) {
        return new IntegerArgumentParser() {
            public Integer parse(String s) {
                Integer i = super.parse(s);
                if (i == null) return i;
                return i >= min && i <= max ? i : null;
            }
        };
    }

    public Integer parse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
