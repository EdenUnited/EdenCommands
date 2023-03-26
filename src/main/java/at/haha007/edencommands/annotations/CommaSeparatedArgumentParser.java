package at.haha007.edencommands.annotations;

import at.haha007.edencommands.argument.Argument;
import at.haha007.edencommands.argument.CommaSeparatedArgument;

import java.util.HashMap;
import java.util.Map;

public class CommaSeparatedArgumentParser implements ArgumentParser<CommaSeparatedArgument<?>> {
    private final ArgumentParserProvider provider;

    public CommaSeparatedArgumentParser(ArgumentParserProvider provider) {
        this.provider = provider;
    }

    public CommaSeparatedArgument<?> parse(Map<String, String> params) {
        params = new HashMap<>(params);
        params.put("type", params.get("subtype"));
        Argument<?> argument = provider.parse(params);
        if (argument == null)
            throw new IllegalArgumentException("Unknown subtype: " + params.get("subtype"));
        return new CommaSeparatedArgument<>(argument, params.getOrDefault("distinct", "true").equals("true"));
    }
}
