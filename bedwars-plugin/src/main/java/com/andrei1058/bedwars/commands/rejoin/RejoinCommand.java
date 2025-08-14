package com.andrei1058.bedwars.commands.rejoin;

import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.ReJoin;
import com.andrei1058.bedwars.configuration.Permissions;
import com.andrei1058.bedwars.configuration.Sounds;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

/**
 * Command handler for the rejoin functionality.
 * Allows players to rejoin their last game if they disconnected.
 */
public class RejoinCommand extends BukkitCommand {

    private static final String CONSOLE_ERROR = "This command is for players only!";
    private static final String REJOIN_ALLOWED_SOUND = "rejoin-allowed";
    private static final String REJOIN_DENIED_SOUND = "rejoin-denied";

    /**
     * Creates a new RejoinCommand instance.
     *
     * @param name The name of the command
     */
    public RejoinCommand(final String name) {
        super(name);
    }

    /**
     * Executes the rejoin command.
     *
     * @param sender The command sender
     * @param currentAlias The alias used to execute the command
     * @param args The command arguments
     * @return true if the command was handled, false otherwise
     */
    @Override
    public boolean execute(final CommandSender sender, final String currentAlias, final String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(CONSOLE_ERROR);
            return true;
        }

        final Player player = (Player) sender;

        if (!player.hasPermission(Permissions.PERMISSION_REJOIN)) {
            sendMessage(player, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS);
            return true;
        }

        final ReJoin rejoinHandler = ReJoin.getPlayer(player);

        if (rejoinHandler == null) {
            sendRejoinDeniedMessage(player, Messages.REJOIN_NO_ARENA);
            return true;
        }

        if (!rejoinHandler.canReJoin()) {
            sendRejoinDeniedMessage(player, Messages.REJOIN_DENIED);
            return true;
        }

        final String arenaName = rejoinHandler.getArena() != null ? rejoinHandler.getArena().getDisplayName() : "";
        sendMessage(player, Messages.REJOIN_ALLOWED, "{arena}", arenaName);
        Sounds.playSound(REJOIN_ALLOWED_SOUND, player);
        rejoinHandler.reJoin(player);
        return true;
    }

    /**
     * Sends a message to the player when rejoin is denied.
     *
     * @param player The player to send the message to
     * @param messageKey The message key to send
     */
    private void sendRejoinDeniedMessage(final Player player, final String messageKey) {
        sendMessage(player, messageKey);
        Sounds.playSound(REJOIN_DENIED_SOUND, player);
    }

    /**
     * Sends a localized message to the player.
     *
     * @param player The player to send the message to
     * @param messageKey The message key to send
     * @param placeholder The placeholder to replace (optional)
     * @param replacement The replacement value (optional)
     */
    private void sendMessage(final Player player, final String messageKey, final String placeholder, final String replacement) {
        String message = Language.getMsg(player, messageKey);
        if (placeholder != null && replacement != null) {
            message = message.replace(placeholder, replacement);
        }
        player.sendMessage(message);
    }

    /**
     * Sends a localized message to the player without replacements.
     *
     * @param player The player to send the message to
     * @param messageKey The message key to send
     */
    private void sendMessage(final Player player, final String messageKey) {
        sendMessage(player, messageKey, null, null);
    }
}