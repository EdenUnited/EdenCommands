package at.haha007.edencommands.tree.node.argument;

public class DoubleArgumentParser implements ArgumentParser<Double> {
    private static final DoubleArgumentParser std = new DoubleArgumentParser();

    public static DoubleArgumentParser doubleParser() {
        return std;
    }

    public static DoubleArgumentParser doubleParser(double min, double max) {
        return new DoubleArgumentParser() {
            public Double parse(String s) {
                Double i = super.parse(s);
                if (i == null) return i;
                return i >= min && i <= max ? i : null;
            }
        };
    }

    public Double parse(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
