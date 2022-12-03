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