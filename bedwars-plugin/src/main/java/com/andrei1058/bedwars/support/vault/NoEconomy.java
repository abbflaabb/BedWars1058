
package com.andrei1058.bedwars.support.vault;

import org.bukkit.entity.Player;

public class NoEconomy implements Economy {
    @Override
    public boolean isEconomy() {
        return false;
    }

    @Override
    public double getMoney(Player p) {
        return 0;
    }

    @Override
    public void giveMoney(Player p, double money) {
        p.sendMessage("§cVault support missing!");
    }

    @Override
    public void buyAction(Player p, double cost) {
        p.sendMessage("§cVault support missing!");
    }
}
