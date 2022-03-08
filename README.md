# EdenCommands
A command library to register Commands in a tree-style fashion.

## Usage

### Nodes
CommandNodes are a tree based system.  
There are 2 basic variations of CommandNodes, the LiteralCommandNode and the ArgumentCommandNode.  
The root node has to be a `LiteralCommandNode`.

```java
class MyClass {
    public MyClass() {
        //creating a command node
        CommandNode node = LiteralCommandNode.literal("mycommand");
        node.executes(context -> {});
        node.withPermission("myplugin.mycommand");
        
        //adding a subcommand -> /mycommand subcommand
        node.then(LiteralCommandNode.literal("subcommand"));
        
        //register it
        CommandRegistry.register(node);
    }
}
```

```java
class MyClass {
    public MyClass() {
        CommandNode node = LiteralCommandNode.literal("mycommand");
        CommandNode argument = ArgumentCommandNode("key", IntegerArgumentParser.intParser(0, 10));
        argument.executes(context -> {
            Integer number = context.getParameter("key", Integer.class);
            context.getSender().sendMessage(number.toString());
        });
        node.then(argument);
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


### Annotations
Register the root object via ``CommandRegistry.register(object)``.

```java
class MyClass {
    public MyClass() {
        CommandRegistry.register(this);
    }

    @Command("mycommand")
    void onCommand(CommandContext context){
    }
}
```

```java
class MyClass {
    @Command("mycommand")
    private final Object command = new Object() {
        @Command("subcommand")
        void onCommand(CommandContext context) {
        }
    };

    public MyClass() {
        CommandRegistry.register(this);
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
