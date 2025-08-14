package com.andrei1058.bedwars.commands.party;

import com.andrei1058.bedwars.api.language.Messages;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.*;

import static com.andrei1058.bedwars.BedWars.getParty;
import static com.andrei1058.bedwars.api.language.Language.getList;
import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class PartyCommand extends BukkitCommand {

    private static final Map<UUID, UUID> partyInvites = new HashMap<>();

    public PartyCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "invite" -> handleInvite(player, args);
            case "accept" -> handleAccept(player, args);
            case "leave" -> handleLeave(player);
            case "disband" -> handleDisband(player);
            case "remove" -> handleRemove(player, args);
            case "promote" -> handlePromote(player, args);
            case "info", "list" -> handleInfo(player);
            default -> sendHelp(player);
        }

        return true;
    }

    // ------------------------------------
    // Subcommand Handlers
    // ------------------------------------

    private void handleInvite(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getMsg(sender, Messages.COMMAND_PARTY_INVITE_USAGE));
            return;
        }

        if (getParty().hasParty(sender) && !getParty().isOwner(sender)) {
            sender.sendMessage(getMsg(sender, Messages.COMMAND_PARTY_INSUFFICIENT_PERMISSIONS));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(getMsg(sender, Messages.COMMAND_PARTY_INVITE_DENIED_PLAYER_OFFLINE).replace("{player}", args[1]));
            return;
        }

        if (sender.equals(target)) {
            sender.sendMessage(getMsg(sender, Messages.COMMAND_PARTY_INVITE_DENIED_CANNOT_INVITE_YOURSELF));
            return;
        }

        partyInvites.put(sender.getUniqueId(), target.getUniqueId());

        sender.sendMessage(getMsg(sender, Messages.COMMAND_PARTY_INVITE_SENT)
                .replace("{playername}", sender.getName())
                .replace("{player}", target.getName()));

        TextComponent message = new TextComponent(getMsg(target, Messages.COMMAND_PARTY_INVITE_SENT_TARGET_RECEIVE_MSG)
                .replace("{player}", sender.getName()));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party accept " + sender.getName()));

        target.spigot().sendMessage(message);
    }

    private void handleAccept(Player player, String[] args) {
        if (args.length < 2) return;

        if (getParty().hasParty(player)) {
            player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_ACCEPT_DENIED_ALREADY_IN_PARTY));
            return;
        }

        Player inviter = Bukkit.getPlayerExact(args[1]);

        if (inviter == null || !inviter.isOnline()) {
            player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_INVITE_DENIED_PLAYER_OFFLINE).replace("{player}", args[1]));
            return;
        }

        UUID expectedTarget = partyInvites.get(inviter.getUniqueId());
        if (expectedTarget == null || !expectedTarget.equals(player.getUniqueId())) {
            player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_ACCEPT_DENIED_NO_INVITE));
            return;
        }

        partyInvites.remove(inviter.getUniqueId());

        if (getParty().hasParty(inviter)) {
            getParty().addMember(inviter, player);
        } else {
            getParty().createParty(inviter, player);
        }

        for (Player member : getParty().getMembers(inviter)) {
            member.sendMessage(getMsg(player, Messages.COMMAND_PARTY_ACCEPT_SUCCESS)
                    .replace("{playername}", player.getName())
                    .replace("{player}", player.getDisplayName()));
        }
    }

    private void handleLeave(Player player) {
        if (!getParty().hasParty(player)) {
            player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY));
            return;
        }

        if (getParty().isOwner(player)) {
            List<Player> members = new ArrayList<>(getParty().getMembers(player));
            members.remove(player); // exclude self

            if (members.isEmpty()) {
                getParty().disband(player);
                player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_LEAVE_SUCCESS));
            } else {
                Player newOwner = members.get(0);
                getParty().promote(player, newOwner);
                getParty().removeFromParty(player);
                player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_LEAVE_SUCCESS));

                newOwner.sendMessage(getMsg(newOwner, Messages.COMMAND_PARTY_PROMOTE_OWNER));
                for (Player member : getParty().getMembers(newOwner)) {
                    if (!member.equals(newOwner)) {
                        member.sendMessage(getMsg(member, Messages.COMMAND_PARTY_PROMOTE_NEW_OWNER).replace("{player}", newOwner.getName()));
                    }
                }
            }
        } else {
            getParty().removeFromParty(player);
            player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_LEAVE_SUCCESS));
        }
    }

    private void handleDisband(Player player) {
        if (!getParty().hasParty(player)) {
            player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY));
            return;
        }

        if (!getParty().isOwner(player)) {
            player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_INSUFFICIENT_PERMISSIONS));
            return;
        }

        List<Player> members = new ArrayList<>(getParty().getMembers(player));
        getParty().disband(player);

        for (Player member : members) {
            member.sendMessage(getMsg(member, Messages.COMMAND_PARTY_LEAVE_SUCCESS));
        }
    }

    private void handleRemove(Player sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(getMsg(sender, Messages.COMMAND_PARTY_REMOVE_USAGE));
            return;
        }

        if (!getParty().hasParty(sender) || !getParty().isOwner(sender)) {
            sender.sendMessage(getMsg(sender, Messages.COMMAND_PARTY_INSUFFICIENT_PERMISSIONS));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);

        if (target == null || !getParty().isMember(sender, target)) {
            sender.sendMessage(getMsg(sender, Messages.COMMAND_PARTY_REMOVE_DENIED_TARGET_NOT_PARTY_MEMBER)
                    .replace("{player}", args[1]));
            return;
        }

        getParty().removePlayer(sender, target);
    }

    private void handlePromote(Player sender, String[] args) {
        if (!getParty().hasParty(sender)) {
            sender.sendMessage(getMsg(sender, Messages.COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY));
            return;
        }

        if (!getParty().isOwner(sender)) {
            sender.sendMessage(getMsg(sender, Messages.COMMAND_PARTY_INSUFFICIENT_PERMISSIONS));
            return;
        }

        if (args.length < 2) {
            sendHelp(sender);
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !getParty().isMember(sender, target)) {
            sender.sendMessage(getMsg(sender, Messages.COMMAND_PARTY_REMOVE_DENIED_TARGET_NOT_PARTY_MEMBER)
                    .replace("{player}", args[1]));
            return;
        }

        getParty().promote(sender, target);

        for (Player member : getParty().getMembers(sender)) {
            if (member.equals(sender)) {
                member.sendMessage(getMsg(member, Messages.COMMAND_PARTY_PROMOTE_SUCCESS)
                        .replace("{player}", target.getName()));
            } else if (member.equals(target)) {
                member.sendMessage(getMsg(member, Messages.COMMAND_PARTY_PROMOTE_OWNER));
            } else {
                member.sendMessage(getMsg(member, Messages.COMMAND_PARTY_PROMOTE_NEW_OWNER)
                        .replace("{player}", target.getName()));
            }
        }
    }

    private void handleInfo(Player player) {
        if (!getParty().hasParty(player)) {
            player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_GENERAL_DENIED_NOT_IN_PARTY));
            return;
        }

        Player owner = getParty().getOwner(player);
        player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_INFO_OWNER).replace("{owner}", owner.getName()));
        player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_INFO_PLAYERS));

        for (Player member : getParty().getMembers(owner)) {
            player.sendMessage(getMsg(player, Messages.COMMAND_PARTY_INFO_PLAYER).replace("{player}", member.getName()));
        }
    }

    private void sendHelp(Player player) {
        for (String msg : getList(player, Messages.COMMAND_PARTY_HELP)) {
            player.sendMessage(msg);
        }
    }
}
