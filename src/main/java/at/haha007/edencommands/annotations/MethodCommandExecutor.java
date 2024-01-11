package at.haha007.edencommands.annotations;

import at.haha007.edencommands.CommandContext;
import at.haha007.edencommands.CommandException;
import at.haha007.edencommands.CommandExecutor;
import net.kyori.adventure.text.Component;
import org.slf4j.LoggerFactory;

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
        try {
            if(!method.trySetAccessible())
                throw new CommandException(Component.text("Failed to execute command! Method couldn't be invoked!"), context);
            method.invoke(obj, context);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LoggerFactory.getLogger(this.getClass()).error("Failed to execute command!", e);
            throw new CommandException(Component.text("Failed to execute command! Method couldn't be invoked!"), context);
        }
    }
}
