package at.haha007.edencommands.tree.node.argument;

import java.util.Optional;

public class OptionalArgumentParser<T> implements ArgumentParser<Optional<T>> {
    private final ArgumentParser<T> parser;

    private OptionalArgumentParser(ArgumentParser<T> t) {
        parser = t;
    }

    public static <T> OptionalArgumentParser<T> optionalParser(ArgumentParser<T> parser) {
        return new OptionalArgumentParser<>(parser);
    }

    public Optional<T> parse(String s) {
        return Optional.ofNullable(parser.parse(s));
    }
}
