
package com.andrei1058.bedwars.upgrades.upgradeaction;

import com.andrei1058.bedwars.api.arena.generator.GeneratorType;
import com.andrei1058.bedwars.api.arena.generator.IGenerator;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.upgrades.UpgradeAction;
import com.andrei1058.bedwars.arena.OreGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GeneratorEditAction implements UpgradeAction {

    private final int amount;
    private final int delay;
    private final int limit;
    private final ApplyType type;

    public GeneratorEditAction(ApplyType type, int amount, int delay, int limit) {
        this.type = type;
        this.amount = amount;
        this.delay = delay;
        this.limit = limit;
    }

    @Override
    public void onBuy(Player player, ITeam bwt) {
        List<IGenerator> generator = new ArrayList<>();
        if (type == ApplyType.IRON) {
            generator = bwt.getGenerators().stream().filter(g -> g.getType() == GeneratorType.IRON).collect(Collectors.toList());
        } else if (type == ApplyType.GOLD) {
            generator = bwt.getGenerators().stream().filter(g -> g.getType() == GeneratorType.GOLD).collect(Collectors.toList());
        } else if (type == ApplyType.EMERALD) {
            if (!bwt.getArena().getConfig().getArenaLocations("Team." + bwt.getName() + ".Emerald").isEmpty()) {
                for (Location l : bwt.getArena().getConfig().getArenaLocations("Team." + bwt.getName() + ".Emerald")) {
                    IGenerator gen = new OreGenerator(l, bwt.getArena(), GeneratorType.CUSTOM, bwt);
                    gen.setOre(new ItemStack(Material.EMERALD));
                    gen.setType(GeneratorType.EMERALD);
                    bwt.getGenerators().add(gen);
                    //bwt.getArena().getOreGenerators().add(gen);
                    generator.add(gen);
                }
            } else {
                IGenerator gen = new OreGenerator(bwt.getGenerators().get(0).getLocation().clone(), bwt.getArena(), GeneratorType.CUSTOM, bwt);
                gen.setOre(new ItemStack(Material.EMERALD));
                gen.setType(GeneratorType.EMERALD);
                bwt.getGenerators().add(gen);
                //bwt.getArena().getOreGenerators().add(gen);
                generator.add(gen);
            }
        }
        for (IGenerator g : generator){
            g.setAmount(amount);
            g.setDelay(delay);
            g.setSpawnLimit(limit);
        }
    }


    public enum ApplyType {
        IRON, GOLD, EMERALD
    }
}
