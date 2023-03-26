# EdenCommands
A command library to register Commands in a tree-style fashion.

## Usage

### Nodes
CommandNodes are a tree based system.
There are 2 basic variations of CommandNodes, the LiteralCommandNode and the ArgumentCommandNode.
The root node has to be a `LiteralCommandNode`.

#### Literals

```java
class MyPlugin extends JavaPlugin {
    public void onEnable() {
        //creating a command node
        CommandRegistry registry = new CommandRegistry(this);

        LiteralCommandNode.LiteralCommandBuilder node = LiteralCommandNode.builder("mycommand");

        node.executor(context -> {
            CommandSender sender = context.sender();
            sender.sendMessage("command executed");
        });
        node.requires(CommandRegistry.permission("myplugin.mycommand"));

        //adding a subcommand -> /mycommand subcommand
        node.then(CommandRegistry.literal("subcommand").executor(c -> c.sender().sendMessage("subcommand executed")));

        //register it
        registry.register(node.build());
    }
}
```

#### Arguments

```java
class MyPlugin extends JavaPlugin {
    public void onEnable() {
        CommandRegistry registry = new CommandRegistry(this);
        LiteralCommandNode.LiteralCommandBuilder node = CommandRegistry.literal("mycommand");

        ArgumentCommandNode.ArgumentCommandBuilder<Integer > argument = CommandRegistry.argument(
                "key",
                IntegerArgument.builder()
                        .filter(new IntegerArgument.MinimumFilter(Component.text("The argument must be positive!"), 0))
                        .filter(new IntegerArgument.MaximumFilter(Component.text("The argument is caped at 10!"), 0))
                        .build()
        );
        argument.executor(context -> {
            Integer number = context.parameter("key");
            context.sender().sendMessage(number.toString());
        });

        node.then(argument);

        registry.register(node);
    }
}
```

### Annotations


The `@Command("mycommand")` annotation on the class can be used as a prefix, but is optional.
Just load all classes with command executors with the `AnnotationCommandLoader` and register them with the `CommandRegistry`.
You have to add the parsers for the arguments you want to use with `AnnotationCommandLoader#addArgumentParser`,
the basics are included using the `AnnotationCommandLoader#addDefaultArgumentParsers` (boolean, double, float, int, long, offline_player, player, string).


```java
@Command("mycommand")
class MyPlugin extends JavaPlugin {
    public void onEnable() {
        CommandRegistry registry = new CommandRegistry(this);
        AnnotatedCommandLoader loader = new AnnotatedCommandLoader(this);
        loader.addDefaultArgumentParsers();
        loader.addArgumentParser("item", EnumArgumentParser.itemMaterialParser());
        loader.addAnnotated(this);
        loader.register(registry);
    }

    // -> /mycommand subcommand
    @Command("subcommand")
    public void subcommand(CommandContext context) {
        context.sender().sendMessage("subcommand executed");
    }

    // -> /mycommand setblock
    @Command(value = "setblock", sync = true)
    public void subcommand(CommandContext context) {
        if (!(context.sender() instanceof Player player)) {
            context.sender().sendMessage("You must be a player to use this command.");
            return;
        }
        Random random = new Random();
        List<Material> list = Arrays.stream(Material.values()).filter(Material::isBlock).toList();
        Material block = list.get(random.nextInt(list.size()));
        player.getLocation().getBlock().setType(block);
    }

    // -> /mycommand stick
    @Command("item{type:item}")
    public void item(CommandContext context) {
        Material material = context.parameter("item");
        context.sender().sendMessage(material.toString());
    }

    // -> /mycommand Haha007 stick 64
    @Command("player{type:player} item{type:item} amount{type:int,range:'1,64',suggest:'1,8,16,32,64'}")
    public void item(CommandContext context) {
        Player player = context.parameter("player");
        Material material = context.parameter("item");
        int amount = context.parameter("amount");
        player.getInventory().addItem(new ItemStack(material, amount));
    }
}
```

## Maven
```xml
<repository>
    <id>jitpack</id>
    <url>https://jitpack.io</url>
</repository>
```
```xml
<dependency>
    <groupId>com.github.EdenUnited</groupId>
    <artifactId>EdenCommands</artifactId>
    <version>2.3</version>
</dependency>
```

## Gradle
```
repositories {
    maven { url 'https://jitpack.io' }
}
```
```
dependencies {
    implementation 'com.github.EdenUnited:EdenCommands:2.3'
}
```