
package com.andrei1058.bedwars.support.party;

import com.andrei1058.bedwars.api.party.Party;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NoParty implements Party {
    @Override
    public boolean hasParty(Player p) {
        return false;
    }

    @Override
    public int partySize(Player p) {
        return 0;
    }

    @Override
    public boolean isOwner(Player p) {
        return false;
    }

    @Override
    public List<Player> getMembers(Player owner) {
        return new ArrayList<>();
    }

    @Override
    public void createParty(Player owner, Player... members) {

    }

    @Override
    public void addMember(Player owner, Player member) {

    }

    @Override
    public void removeFromParty(Player member) {

    }

    @Override
    public void disband(Player owner) {

    }

    @Override
    public boolean isMember(Player owner, Player check) {
        return false;
    }

    @Override
    public void removePlayer(Player owner, Player target) {

    }

    @Override
    public Player getOwner(Player member) {
        return null;
    }

    @Override
    public void promote(@NotNull Player owner, @NotNull Player target) {

    }

    @Override
    public boolean isInternal() {
        return false;
    }
}
