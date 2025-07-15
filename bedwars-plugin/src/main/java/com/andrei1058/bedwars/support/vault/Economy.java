
package com.andrei1058.bedwars.support.vault;

import org.bukkit.entity.Player;

public interface Economy {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isEconomy();
    double getMoney(Player p);
    void giveMoney(Player p, double money);
    void buyAction(Player p, double cost);
}
