package at.haha007.edencommands.annotations;

import at.haha007.edencommands.argument.Argument;

import java.util.Map;

public interface ArgumentParser<T extends Argument<?>> {
    T parse(Map<String, String> s);
}
