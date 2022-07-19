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

        LiteralCommandNode node = LiteralCommandNode("mycommand");

        node.executor(context -> {
            CommandSender sender = context.sender();
            sender.sendMessage("command executed");
        });
        node.requires(CommandRegistry.permission("myplugin.mycommand"));

        //adding a subcommand -> /mycommand subcommand
        node.then(CommandRegistry.literal("subcommand").executor(c -> c.sender().sendMessage("subcommand executed")));

        //register it
        CommandRegistry.register(node);
    }
}
```

#### Arguments
```java
class MyPlugin extends JavaPlugin {
    public void onEnable() {
        CommandRegistry registry = new CommandRegistry(this);
        CommandNode node = new LiteralCommandNode("mycommand");

        ArgumentCommandNode<Integer> argument = new ArgumentCommandNode<>(
                "key",
                IntegerArgument.integer(0, 10, Component.text("You have to supply an integer between 0 and 10!"))
        );
        argument.executes(context -> {
            Integer number = context.parameter("key");
            context.getSender().sendMessage(number.toString());
        });
        
        node.then(argument);

        registry.register(node);
    }
}
```

### Brigadier
Register the root object via ``BrigadierCommandRegistry.register(object)``.

```java
class MyPlugin extends JavaPlugin {
    public void onEnable() {
        BrigadierCommandRegistry registry = new BrigadierCommandRegistry(this);

        //creating a command node
        LiteralArgumentBuilder<Player> command = registry.literal("mycommand");
        command.executes(cmd -> 1);
        command.requires(p -> p.hasPermission("myplugin.mycommand"));

        //adding a subcommand -> /mycommand subcommand
        LiteralArgumentBuilder<Player> subcommand = registry.literal("subcommand");
        command.then(subcommand);

        //register it
        registry.register(node);
    }
}
```

```java
class MyPlugin extends JavaPlugin {
    public void onEnable() {
        BrigadierCommandRegistry registry = new BrigadierCommandRegistry(this);
        LiteralArgumentBuilder<Player> node = registry.literal("mycommand");
        RequiredArgumentBuilder<Player, Integer> argument = registry.argument("key", IntegerArgumentType.integer(0, 10));
        argument.executes(cmd -> {
            Integer number = cmd.getArgument("key", Integer.class);
            cmd.getSource().sendMessage(number.toString());
            return 1;
        });
        node.then(argument);
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
    <version>version</version>
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
    implementation 'com.github.EdenUnited:EdenCommands:version'
}
```

## Version

[![](https://jitpack.io/v/EdenUnited/EdenCommands.svg)](https://jitpack.io/#EdenUnited/EdenCommands)
