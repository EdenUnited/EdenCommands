package at.haha007.edencommands.annotations;

import at.haha007.edencommands.argument.*;
import at.haha007.edencommands.argument.player.OfflinePlayerArgument;
import at.haha007.edencommands.argument.player.PlayerArgument;
import com.destroystokyo.paper.event.server.AsyncTabCompleteEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.util.TriState;

import java.util.Map;

public enum DefaultArgumentParsers implements ArgumentParser<Argument<?>> {
    STRING("string") {
        public Argument<?> parse(Map<String, String> params) {
            StringArgument.StringArgumentBuilder builder = StringArgument.builder();
            String parserKey = params.get("parser");
            if (parserKey == null) parserKey = "word";
            StringArgument.StringParser parser = switch (parserKey) {
                case "word" -> StringArgument.word();
                case "greedy" -> StringArgument.greedy();
                case "quoted" -> {
                    String error = params.getOrDefault("error", "Invalid quote usage!");
                    yield StringArgument.quoted(mm.deserialize(error));
                }
                default -> throw new IllegalArgumentException("Unknown parser: " + parserKey);
            };
            builder.parser(parser);
            params.entrySet().stream()
                    .filter(e -> e.getKey()
                            .startsWith("suggest"))
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .map(AsyncTabCompleteEvent.Completion::completion)
                    .forEach(builder::completion);
            return builder.build();
        }
    },
    LONG("long") {
        public Argument<?> parse(Map<String, String> params) {
            LongArgument.LongArgumentBuilder builder = LongArgument.builder();
            if (params.containsKey("filter")) {
                String filter = params.get("filter");
                long min = Long.parseLong(filter.split(",")[0]);
                long max = Long.parseLong(filter.split(",")[1]);
                if (min > max) {
                    long temp = min;
                    min = max;
                    max = temp;
                }
                String errorString = "Argument must be between " + min + " and " + max + "!";
                Component error = mm.deserialize(params.getOrDefault("filter_message", errorString));
                builder.filter(new LongArgument.RangeFilter(error, min, max));
            }
            if (params.containsKey("suggest")) {
                String[] suggest = params.get("suggest").split(",");
                for (String s : suggest) {
                    builder.completion(new Completion<>(Long.parseLong(s)));
                }
            }
            String notLongMessage = params.getOrDefault("error", "Argument {0} must be of type long!");
            builder.notLongMessage(c -> mm.deserialize(notLongMessage.replace("{0}", c)));
            return builder.build();
        }
    },
    INTEGER("int") {
        public Argument<?> parse(Map<String, String> params) {
            IntegerArgument.IntegerArgumentBuilder builder = IntegerArgument.builder();
            if (params.containsKey("filter")) {
                String filter = params.get("filter");
                int min = Integer.parseInt(filter.split(",")[0]);
                int max = Integer.parseInt(filter.split(",")[1]);
                if (min > max) {
                    int temp = min;
                    min = max;
                    max = temp;
                }
                String errorString = "Argument must be between " + min + " and " + max + "!";
                Component error = mm.deserialize(params.getOrDefault("filter_message", errorString));
                builder.filter(new IntegerArgument.RangeFilter(error, min, max));
            }
            if (params.containsKey("suggest")) {
                String[] suggest = params.get("suggest").split(",");
                for (String ss : suggest) {
                    builder.completion(new Completion<>(Integer.parseInt(ss)));
                }
            }
            String notIntMessage = params.getOrDefault("error", "Argument {0} must be of type integer!");
            builder.notIntegerMessage(c -> mm.deserialize(notIntMessage.replace("{0}", c)));
            return builder.build();
        }
    },
    FLOAT("float") {
        public Argument<?> parse(Map<String, String> params) {
            FloatArgument.FloatArgumentBuilder builder = FloatArgument.builder();
            if (params.containsKey("filter")) {
                String filter = params.get("filter");
                float min = Float.parseFloat(filter.split(",")[0]);
                float max = Float.parseFloat(filter.split(",")[1]);
                if (min > max) {
                    float temp = min;
                    min = max;
                    max = temp;
                }
                String errorString = "Argument must be between " + min + " and " + max + "!";
                Component error = mm.deserialize(params.getOrDefault("filter_message", errorString));
                builder.filter(new FloatArgument.RangeFilter(error, min, max));
            }
            if (params.containsKey("suggest")) {
                String[] suggest = params.get("suggest").split(",");
                for (String ss : suggest) {
                    builder.completion(new Completion<>(Float.parseFloat(ss)));
                }
            }
            String notFloatMessage = params.getOrDefault("error", "Argument {0} must be of type float!");
            builder.notFloatMessage(c -> mm.deserialize(notFloatMessage.replace("{0}", c)));
            return builder.build();
        }
    },
    DOUBLE("double") {
        public Argument<?> parse(Map<String, String> params) {
            DoubleArgument.DoubleArgumentBuilder builder = DoubleArgument.builder();
            if (params.containsKey("filter")) {
                String filter = params.get("filter");
                double min = Double.parseDouble(filter.split(",")[0]);
                double max = Double.parseDouble(filter.split(",")[1]);
                if (min > max) {
                    double temp = min;
                    min = max;
                    max = temp;
                }
                String errorString = "Argument must be between " + min + " and " + max + "!";
                Component error = mm.deserialize(params.getOrDefault("filter_message", errorString));
                builder.filter(new DoubleArgument.RangeFilter(error, min, max));
            }
            if (params.containsKey("suggest")) {
                String[] suggest = params.get("suggest").split(",");
                for (String ss : suggest) {
                    builder.completion(new Completion<>(Double.parseDouble(ss)));
                }
            }
            String notDoubleMessage = params.getOrDefault("error", "Argument {0} must be of type double!");
            builder.notDoubleMessage(c -> mm.deserialize(notDoubleMessage.replace("{0}", c)));
            return builder.build();
        }
    },
    BOOLEAN("boolean") {
        public Argument<?> parse(Map<String, String> s) {
            BooleanArgument.BooleanArgumentBuilder builder = BooleanArgument.builder();
            builder.notBooleanMessage(c -> mm.deserialize(s.getOrDefault("error", "Argument " + c + " must be a boolean!")));
            return builder.build();
        }
    },
    PLAYER("player") {
        public Argument<?> parse(Map<String, String> s) {
            PlayerArgument.PlayerArgumentBuilder builder = PlayerArgument.builder();
            builder.exact(TriState.byBoolean(s.getOrDefault("exact", "false").equals("true")));
            builder.playerNotFoundErrorProvider(c -> mm.deserialize(s.getOrDefault("error", "Player " + c + " not found!")));
            return builder.build();
        }
    },
    OFFLINE_PLAYER("offline_player") {
        public Argument<?> parse(Map<String, String> s) {
            OfflinePlayerArgument.OfflinePlayerArgumentBuilder builder = OfflinePlayerArgument.builder();
            builder.playerNotFoundErrorProvider(c -> mm.deserialize(s.getOrDefault("error", "Player " + c + " not found!")));
            return builder.build();
        }
    };
    private static final MiniMessage mm = MiniMessage.miniMessage();
    private final String key;

    DefaultArgumentParsers(String string) {
        this.key = string;
    }

    public String getKey() {
        return key;
    }
}
