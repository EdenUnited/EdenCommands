package at.haha007.edencommands.annotations;

import at.haha007.edencommands.argument.Argument;

import java.util.HashMap;
import java.util.Map;

public class ArgumentParserProvider {

    private final Map<String, ArgumentParser<?>> parserMap = new HashMap<>();

    public Argument<?> parse(Map<String, String> params) {
        String typeName = params.get("type");
        if (typeName == null) {
            throw new IllegalArgumentException("ArgumentParser type is null!");
        }
        ArgumentParser<?> parser = parserMap.get(typeName);
        if (parser != null) {
            return parser.parse(params);
        }
        return null;
    }

    public ArgumentParserProvider register(String typeName, ArgumentParser<?> parser) {
        if (parserMap.containsKey(typeName)) {
            throw new IllegalArgumentException("ArgumentParser for " + typeName + " is already registered!");
        }
        if (parser == null) {
            throw new IllegalArgumentException("ArgumentParser for " + typeName + " is null!");
        }
        parserMap.put(typeName, parser);
        return this;
    }
}
