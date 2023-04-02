package at.haha007.edencommands.annotations;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import at.haha007.edencommands.CommandExecutor;
import net.kyori.adventure.text.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class MethodCommandExecutor implements CommandExecutor {
    private final Object obj;
    private final Method method;

    public MethodCommandExecutor(Object obj, Method method) {
        this.obj = obj;
        this.method = method;
    }

    @Override
    public void execute(CommandContext context) throws CommandException {
        method.setAccessible(true);
        try {
            method.invoke(obj, context);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new CommandException(Component.text("Failed to execute command! Method couldn't be invoked!"), context);
        }
    }
}
