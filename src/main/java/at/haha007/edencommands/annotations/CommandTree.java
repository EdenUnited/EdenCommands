package at.haha007.edencommands.annotations;

import at.haha007.edencommands.CommandExecutor;
import at.haha007.edencommands.CommandRegistry;
import at.haha007.edencommands.argument.Argument;
import at.haha007.edencommands.tree.ArgumentCommandNode;
import at.haha007.edencommands.tree.CommandBuilder;
import at.haha007.edencommands.tree.LiteralCommandNode;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

class CommandTree {
    private final List<CommandTree> children = new ArrayList<>();
    private final String value;
    private final String key;
    private final Map<String, String> params;
    private CommandExecutor executor;

    static CommandTree root() {
        return new CommandTree("'root'{'type':'literal'}");
    }

    private CommandTree(String value) {
        this.value = value;
        try {
            Name key = Name.readName(value, " {");
            this.key = key.value();
            params = ParamList.readParams(value.substring(key.len)).asMap();
            if (!params.containsKey("type")) {
                throw new ParseException("", key.len + 1);
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    void add(String toParse, CommandExecutor executor) {
        toParse = toParse.trim();
        if (toParse.isEmpty()) {
            //end of command reached
            this.executor = executor;
            return;
        }
        Name name;
        try {
            name = Name.readName(toParse, " {");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if (toParse.substring(name.len()).isBlank() || toParse.substring(name.len()).charAt(0) == ' ') {
            parseChildren(toParse.substring(name.len()), name + "{'type':'literal'}", executor);
            return;
        }
        ParamList params;
        try {
            params = ParamList.readParams(toParse.substring(name.len()));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        parseChildren(toParse.substring(name.len() + params.len()), name + "" + params, executor);
    }

    private void parseChildren(String toParse, String key, CommandExecutor executor) {
        Optional<CommandTree> oChild = children.stream().filter(c -> c.value.equals(key)).findAny();
        CommandTree child;
        if (oChild.isEmpty()) {
            child = new CommandTree(key);
            children.add(child);
        } else {
            child = oChild.get();
        }
        child.add(toParse, executor);
    }

    CommandBuilder<?> asCommand(ArgumentParserProvider argumentParser) throws ParseException {
        CommandBuilder<?> node;
        if (params.get("type").equalsIgnoreCase("literal")) {
            node = LiteralCommandNode.builder(key);
        } else {
            Argument<?> argument = argumentParser.parse(params);
            if (argument == null)
                throw new ParseException("ArgumentParser for type " + params.get("type") + " not found!", 0);
            node = ArgumentCommandNode.builder(key, argument);
        }
        if (params.containsKey("usage"))
            node.usageText(MiniMessage.miniMessage().deserialize(params.get("usage")));
        if (params.containsKey("permission"))
            node.requires(CommandRegistry.permission(params.get("permission")));

        node.executor(Objects.requireNonNullElseGet(executor, () -> c -> {
        }));

        for (CommandTree child : children) {
            node.then(child.asCommand(argumentParser));
        }
        return node;
    }

    List<CommandBuilder<?>> getChildCommands(ArgumentParserProvider argumentParser) {
        return children.stream().map(c -> {
            try {
                return c.asCommand(argumentParser);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "CommandTree{" +
                "children=" + children +
                ", value=" + value +
                ", key=" + key +
                ", params=" + params +
                ", executor=" + executor +
                '}';
    }

    private record ParamList(List<Param> list, int len) {
        static ParamList readParams(String toParse) throws ParseException {
            toParse = toParse.substring(1);
            int len = 0;
            List<Param> params = new ArrayList<>();
            try {
                while (toParse.trim().charAt(0) != '}') {
                    while (toParse.startsWith(" ") || toParse.startsWith(",")) {
                        len++;
                        toParse = toParse.substring(1);
                    }
                    Param param = Param.readParam(toParse);
                    params.add(param);
                    toParse = toParse.substring(param.len);
                    len += param.len;
                }
            } catch (IndexOutOfBoundsException e) {
                throw new ParseException(toParse, toParse.length());
            }
            boolean hasType = params.stream().map(param -> param.key().value()).anyMatch(s -> s.equalsIgnoreCase("type"));
            if (!hasType)
                params.add(new Param(new Name("type", 0), new Name("literal", 0), 0));
            return new ParamList(List.copyOf(params), len + 2);
        }

        public String toString() {
            List<String> list = this.list.stream().map(Param::toString).sorted().toList();
            return "{" + String.join(",", list) + "}";
        }

        Map<String, String> asMap() {
            Map<String, String> map = new HashMap<>();
            for (Param param : list) {
                map.put(param.key().value(), param.value().value());
            }
            return Map.copyOf(map);
        }
    }

    private record Param(Name key, Name value, int len) {
        static Param readParam(String toParse) throws ParseException {
            Name name = Name.readName(toParse, ":");
            Name value = Name.readName(toParse.substring(name.len() + 1), "},", false);
            return new Param(name, value, name.len() + value.len() + 1);
        }

        public String toString() {
            return key.toString() + ":" + value.toString();
        }
    }

    private record Name(String value, int len) {
        static Name readName(String toParse, String endChars) throws ParseException {
            return readName(toParse, endChars, true);
        }

        static Name readName(String toParse, String endChars, boolean blockSpaces) throws ParseException {
            if (toParse.charAt(0) == '\'') {
                Name param = readQuotedString(toParse, blockSpaces);
                int len = param.len();
                String remaining = toParse.substring(len);
                if (remaining.isBlank()) return param;
                if (endChars.contains(remaining.substring(0, 1))) return param;
                throw new ParseException(toParse, len);
            }
            int min = toParse.length();
            for (int i = 0; i < endChars.length(); i++) {
                int index = toParse.indexOf(endChars.charAt(i));
                if (index < 0) continue;
                min = Math.min(index, min);
            }
            if (min == 0) throw new ParseException(toParse, 0);
            return new Name(toParse.substring(0, min), min);
        }

        private static Name readQuotedString(String toParse, boolean blockSpaces) throws ParseException {
            StringBuilder sb = new StringBuilder();
            int len = 0;
            boolean isEscaped = false;
            for (int i = 1; i < toParse.length(); i++) {
                len++;
                if (!isEscaped && toParse.charAt(i) == '\\') {
                    isEscaped = true;
                    continue;
                }
                if (!isEscaped && toParse.charAt(i) == '\'') {
                    break;
                }
                sb.append(toParse.charAt(i));
                isEscaped = false;
            }
            if (blockSpaces && sb.toString().contains(" ")) {
                throw new ParseException(sb.toString(), sb.indexOf(" "));
            }
            return new Name(sb.toString(), len + 1);
        }

        public String toString() {
            return "'" + value.replace("'", "\\'") + "'";
        }
    }
}
