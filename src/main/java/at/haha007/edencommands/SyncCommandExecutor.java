package at.haha007.edencommands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

//Only needed when registered as async command
//lags the server when registered as sync command due to use of BukkitScheduler::CallSyncMethod
public class SyncCommandExecutor implements CommandExecutor {
    @NotNull
    private final CommandExecutor executor;
    @NotNull
    private final Plugin plugin;

    public SyncCommandExecutor(@NotNull CommandExecutor executor, @NotNull Plugin plugin) {
        this.executor = executor;
        this.plugin = plugin;
    }

    public void execute(CommandContext context) throws CommandException {
        Future<CommandException> r = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
            try {
                executor.execute(context);
            } catch (CommandException t) {
                return t;
            }
            return null;
        });
        try {
            CommandException ex = r.get();
            if (ex != null) throw ex;
        } catch (InterruptedException | ExecutionException e) {
            plugin.getLogger().throwing(getClass().getSimpleName(), "execute", e);
            Thread.currentThread().interrupt();
            throw new CommandException(Component.text("Error while executing command, please look in the console!"), context);
        }
    }
}
