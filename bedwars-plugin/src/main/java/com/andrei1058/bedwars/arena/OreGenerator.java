package com.andrei1058.bedwars.arena;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.generator.*;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.events.gameplay.GeneratorUpgradeEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.region.Cuboid;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static com.andrei1058.bedwars.BedWars.*;

@SuppressWarnings("WeakerAccess")
public class OreGenerator implements IGenerator {

    private Location location;
    private int delay = 1, upgradeStage = 1, lastSpawn, spawnLimit = 0, amount = 1;

    // MODIFICATION: Removed @Getter from this field. We will create a manual getter.
    private IArena arena;

    private ItemStack ore;
    private GeneratorType type;
    private GeneratorSpeed speed;
    private int rotate = 0, dropID = 0;
    private ITeam bwt;
    boolean up = true;

    /**
     * Generator holograms per language <iso, holo></iso,>
     */
    private HashMap<String, IGenHolo> armorStands = new HashMap<>();

    private ArmorStand item;
    public boolean stack = getGeneratorsCfg().getBoolean(ConfigPath.GENERATOR_STACK_ITEMS);

    @Getter
    private static final ConcurrentLinkedDeque<OreGenerator> rotation = new ConcurrentLinkedDeque<>();

    public OreGenerator(Location location, IArena arena, GeneratorType type, ITeam bwt, GeneratorSpeed speed) {
        if (type == GeneratorType.EMERALD || type == GeneratorType.DIAMOND) {
            this.location = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY() + 1.3, location.getBlockZ() + 0.5);
        } else {
            this.location = location.add(0, 1.3, 0);
        }
        this.arena = arena;
        this.bwt = bwt;
        this.type = type;
        this.speed = speed;

        loadDefaults();
        BedWars.debug("Initializing new generator at: " + location + " - " + type + " - " + (bwt == null ? "NOTEAM" : bwt.getName()) + " - " + speed);

        Cuboid c = new Cuboid(location, getArena().getConfig().getInt(ConfigPath.ARENA_GENERATOR_PROTECTION), true);
        c.setMaxY(c.getMaxY() + 5);
        c.setMinY(c.getMinY() - 2);
        arena.getRegionsList().add(c);
    }

    @Override
    public void upgrade() {
        switch (type) {
            case DIAMOND:
                upgradeStage++;
                if (upgradeStage == 2) {
                    delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_DELAY) == null ?
                            "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_II_DELAY : arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_DELAY);
                    amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_AMOUNT) == null ?
                            "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_II_AMOUNT : arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_AMOUNT);
                    spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_SPAWN_LIMIT) == null ?
                            "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_II_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_SPAWN_LIMIT);
                } else if (upgradeStage == 3) {
                    delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_DELAY) == null ?
                            "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_III_DELAY : arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_DELAY);
                    amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_AMOUNT) == null ?
                            "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_III_AMOUNT : arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_AMOUNT);
                    spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_II_SPAWN_LIMIT) == null ?
                            "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_III_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_III_SPAWN_LIMIT);
                }
                ore = new ItemStack(Material.DIAMOND);
                for (IGenHolo e : armorStands.values()) {
                    e.setTierName(Language.getLang(e.getIso()).m(Messages.GENERATOR_HOLOGRAM_TIER).replace("{tier}", Language.getLang(e.getIso())
                            .m(upgradeStage == 2 ? Messages.FORMATTING_GENERATOR_TIER2 : Messages.FORMATTING_GENERATOR_TIER3)));
                }
                break;
            case EMERALD:
                upgradeStage++;
                if (upgradeStage == 2) {
                    delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_DELAY) == null ?
                            "Default." + ConfigPath.GENERATOR_EMERALD_TIER_II_DELAY : arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_DELAY);
                    amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_AMOUNT) == null ?
                            "Default." + ConfigPath.GENERATOR_EMERALD_TIER_II_AMOUNT : arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_AMOUNT);
                    spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_SPAWN_LIMIT) == null ?
                            "Default." + ConfigPath.GENERATOR_EMERALD_TIER_II_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_SPAWN_LIMIT);
                } else if (upgradeStage == 3) {
                    delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_DELAY) == null ?
                            "Default." + ConfigPath.GENERATOR_EMERALD_TIER_III_DELAY : arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_DELAY);
                    amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_AMOUNT) == null ?
                            "Default." + ConfigPath.GENERATOR_EMERALD_TIER_III_AMOUNT : arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_AMOUNT);
                    spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_II_SPAWN_LIMIT) == null ?
                            "Default." + ConfigPath.GENERATOR_EMERALD_TIER_III_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_III_SPAWN_LIMIT);
                }
                ore = new ItemStack(Material.EMERALD);
                for (IGenHolo e : armorStands.values()) {
                    e.setTierName(Language.getLang(e.getIso()).m(Messages.GENERATOR_HOLOGRAM_TIER).replace("{tier}",
                            Language.getLang(e.getIso()).m(upgradeStage == 2 ? Messages.FORMATTING_GENERATOR_TIER2 : Messages.FORMATTING_GENERATOR_TIER3)));
                }
                break;
        }
        Bukkit.getPluginManager().callEvent(new GeneratorUpgradeEvent(this));
    }

    @Override
    public void spawn() {
        if (arena.getStatus() != GameState.playing){
            return;
        }

        if (lastSpawn == 0) {
            lastSpawn = delay;

            double multiplier = 1.0;
            if(bwt != null){

                Integer forgeLevel = bwt.getTeamUpgradeTiers().get("upgrade-forge");
                if(forgeLevel != null){
                    switch (forgeLevel){
                        case 1:
                            multiplier = 1.5;
                            break;
                        case 2:
                            multiplier = 2;
                            break;
                        default:
                            break;
                    }
                }
            }

            if (spawnLimit != 0) {
                int oreCount = 0;

                for (Entity e : location.getWorld().getNearbyEntities(location, 3, 3, 3)) {
                    if (e.getType() == EntityType.DROPPED_ITEM) {
                        Item i = (Item) e;
                        if (i.getItemStack().getType() == ore.getType()) {
                            oreCount++;
                        }
                        if (oreCount >= spawnLimit) return;
                    }
                }
                lastSpawn = delay;
            }

            if (bwt == null || bwt.getMembers().size() == 1) {
                dropItem(location);
                return;
            }

            if (plugin.getConfig().getBoolean(ConfigPath.GENERAL_CONFIGURATION_ENABLE_GEN_SPLIT)) {
                Object[] players = location.getWorld().getNearbyEntities(location, 1, 1, 1).stream()
                        .filter(entity -> entity.getType() == EntityType.PLAYER)
                        .filter(entity -> arena.isPlayer((Player) entity))
                        .toArray();

                if (players.length <= 1) {
                    dropItem(location, multiplier);
                    return;
                }
                for (Object o : players) {
                    Player player = (Player) o;
                    ItemStack item = ore.clone();
                    item.setAmount((int) (amount * multiplier));
                    player.playSound(player.getLocation(), Sound.valueOf(BedWars.getForCurrentVersion("ITEM_PICKUP", "ENTITY_ITEM_PICKUP", "ENTITY_ITEM_PICKUP")), 0.6f, 1.3f);
                    Collection<ItemStack> excess = player.getInventory().addItem(item).values();
                    for (ItemStack value : excess) {
                        dropItem(player.getLocation(), value.getAmount());
                    }
                }
                return;
            } else {
                dropItem(location, multiplier);
                return;
            }
        }
        lastSpawn--;
        for (IGenHolo e : armorStands.values()) {
            e.setTimerName(Language.getLang(e.getIso()).m(Messages.GENERATOR_HOLOGRAM_TIMER).replace("{seconds}", String.valueOf(lastSpawn)));
        }
    }

    private void dropItem(Location location, double multiplier) {
        for (int temp = (int) (amount*multiplier); temp > 0; temp--) {
            ItemStack itemStack = new ItemStack(ore);
            if (!stack) {
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName("custom" + dropID++);
                itemStack.setItemMeta(itemMeta);
            }

            Item item = location.getWorld().dropItem(location, itemStack);
            item.setVelocity(new Vector(0, 0, 0));
        }
    }

    /**
     * Drop item stack with ID
     */
    @Override
    public void dropItem(Location location) {
        dropItem(location, 1.0);
    }

    @Override
    public void setOre(ItemStack ore) {
        BedWars.debug("Changing ore for generator at " + location.toString() + " from " + this.ore + " to " + ore);
        this.ore = ore;
    }

    @Override
    public HashMap<String, IGenHolo> getLanguageHolograms() {
        return armorStands;
    }

    @SuppressWarnings("WeakerAccess")
    public class HoloGram implements IGenHolo {
        String iso;
        ArmorStand tier, timer, name;

        public HoloGram(String iso) {
            this.iso = iso;
            this.tier = createArmorStand(Language.getLang(iso).m(Messages.GENERATOR_HOLOGRAM_TIER)
                    .replace("{tier}", Language.getLang(iso).m(Messages.FORMATTING_GENERATOR_TIER1)), location.clone().add(0, 3, 0));
            this.timer = createArmorStand(Language.getLang(iso).m(Messages.GENERATOR_HOLOGRAM_TIMER)
                    .replace("{seconds}", String.valueOf(lastSpawn)), location.clone().add(0, 2.4, 0));
            this.name = createArmorStand(Language.getLang(iso).m(getOre().getType() == Material.DIAMOND ? Messages.GENERATOR_HOLOGRAM_TYPE_DIAMOND
                    : Messages.GENERATOR_HOLOGRAM_TYPE_EMERALD), location.clone().add(0, 2.7, 0));

        }

        @Override
        public void updateForAll() {
            for (Player p2 : timer.getWorld().getPlayers()) {
                updateForPlayer(p2, Language.getPlayerLanguage(p2).getIso());
            }
        }

        @Override
        public void updateForPlayer(Player p, String lang) {
            if (lang.equalsIgnoreCase(iso)) return;
            nms.hideEntity(tier, p);
            nms.hideEntity(timer, p);
            nms.hideEntity(name, p);
        }

        @Override
        public void setTierName(String name) {
            if (tier.isDead()) return;
            tier.setCustomName(name);
        }

        @Override
        public String getIso() {
            return iso;
        }

        @Override
        public void setTimerName(String name) {
            if (timer.isDead()) return;
            timer.setCustomName(name);
        }

        @Override
        public void destroy() {
            tier.remove();
            timer.remove();
            name.remove();
        }
    }

    private static ArmorStand createArmorStand(String name, Location l) {
        ArmorStand a = (ArmorStand) l.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        a.setGravity(false);
        if (name != null) {
            a.setCustomName(name);
            a.setCustomNameVisible(true);
        }
        a.setRemoveWhenFarAway(false);
        a.setVisible(false);
        a.setCanPickupItems(false);
        a.setArms(false);
        a.setBasePlate(false);
        a.setMarker(true);
        return a;
    }

    @Override
    public void rotate() {
        if (up) {
            if (rotate >= 540) {
                up = false;
            }
            if (rotate > 500) {
                item.setHeadPose(new EulerAngle(0, Math.toRadians(rotate += 1), 0));
            } else if (rotate > 470) {
                item.setHeadPose(new EulerAngle(0, Math.toRadians(rotate += 2), 0));
            } else if (rotate > 450) {
                item.setHeadPose(new EulerAngle(0, Math.toRadians(rotate += 3), 0));
            } else {
                item.setHeadPose(new EulerAngle(0, Math.toRadians(rotate += 4), 0));
            }
        } else {
            if (rotate <= 0) {
                up = true;
            }
            if (rotate > 120) {
                item.setHeadPose(new EulerAngle(0, Math.toRadians(rotate -= 4), 0));
            } else if (rotate > 90) {
                item.setHeadPose(new EulerAngle(0, Math.toRadians(rotate -= 3), 0));
            } else if (rotate > 70) {
                item.setHeadPose(new EulerAngle(0, Math.toRadians(rotate -= 2), 0));
            } else {
                item.setHeadPose(new EulerAngle(0, Math.toRadians(rotate -= 1), 0));
            }
        }
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public ItemStack getOre() {
        return ore;
    }

    @Override
    public void disable() {
        if (getOre().getType() == Material.EMERALD || getOre().getType() == Material.DIAMOND) {
            rotation.remove(this);
            for (IGenHolo a : armorStands.values()) {
                a.destroy();
            }
        }
        armorStands.clear();
    }

    @Override
    public void updateHolograms(Player p, String iso) {
        for (IGenHolo h : armorStands.values()) {
            h.updateForPlayer(p, iso);
        }
    }

    @Override
    public void enableRotation() {
        rotation.add(this);
        for (Language lan : Language.getLanguages()) {
            IGenHolo h = armorStands.get(lan.getIso());
            if (h == null) {
                armorStands.put(lan.getIso(), new HoloGram(lan.getIso()));
            }
        }
        for (IGenHolo hg : armorStands.values()) {
            hg.updateForAll();
        }

        item = createArmorStand(null, location.clone().add(0, 0.5, 0));
        item.setHelmet(new ItemStack(type == GeneratorType.DIAMOND ? Material.DIAMOND_BLOCK : Material.EMERALD_BLOCK));
    }

    @Override
    public void setSpawnLimit(int value) {
        this.spawnLimit = value;
    }

    private void loadDefaults() {
        switch (type) {
            case GOLD:
                switch (speed){
                    case SLOW:
                        this.delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SLOW + ".delay") == null ?
                                "Default." + ConfigPath.GENERATOR_GOLD_SLOW + ".delay" : arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SLOW + ".delay");
                        this.ore = new ItemStack(Material.GOLD_INGOT);
                        this.amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SLOW + ".amount") == null ?
                                "Default." + ConfigPath.GENERATOR_GOLD_SLOW + ".amount" : arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SLOW + ".amount");
                        this.spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT) == null ?
                                "Default." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT);
                        break;
                    case FAST:
                        this.delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_FAST + ".delay") == null ?
                                "Default." + ConfigPath.GENERATOR_GOLD_FAST + ".delay" : arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_FAST + ".delay");
                        this.ore = new ItemStack(Material.GOLD_INGOT);
                        this.amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_FAST + ".amount") == null ?
                                "Default." + ConfigPath.GENERATOR_GOLD_FAST + ".amount" : arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_FAST + ".amount");
                        this.spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT) == null ?
                                "Default." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT);
                        break;

                    case NORMAL:
                        this.delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_MEDIUM + ".delay") == null ?
                                "Default." + ConfigPath.GENERATOR_GOLD_MEDIUM + ".delay" : arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_MEDIUM + ".delay");
                        this.ore = new ItemStack(Material.GOLD_INGOT);
                        this.amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_MEDIUM + ".amount") == null ?
                                "Default." + ConfigPath.GENERATOR_GOLD_MEDIUM + ".amount" : arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_MEDIUM + ".amount");
                        this.spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT) == null ?
                                "Default." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_GOLD_SPAWN_LIMIT);
                        break;
                }
                break;

            case IRON:
                switch (speed){
                    case SLOW:
                        this.delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_SLOW + ".delay") == null ?
                                "Default." + ConfigPath.GENERATOR_IRON_SLOW + ".delay" : arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_SLOW + ".delay");
                        this.ore = new ItemStack(Material.IRON_INGOT);
                        this.amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_SLOW + ".amount") == null ?
                                "Default." + ConfigPath.GENERATOR_IRON_SLOW + ".amount" : arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_SLOW + ".amount");
                        this.spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT) == null ?
                                "Default." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT);
                        break;
                    case FAST:
                        this.delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_FAST + ".delay") == null ?
                                "Default." + ConfigPath.GENERATOR_IRON_FAST + ".delay" : arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_FAST + ".delay");
                        this.ore = new ItemStack(Material.IRON_INGOT);
                        this.amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_FAST + ".amount") == null ?
                                "Default." + ConfigPath.GENERATOR_IRON_FAST + ".amount" : arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_FAST + ".amount");
                        this.spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT) == null ?
                                "Default." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT);
                        break;

                    case NORMAL:
                        this.delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_MEDIUM + ".delay") == null ?
                                "Default." + ConfigPath.GENERATOR_IRON_MEDIUM + ".delay" : arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_MEDIUM + ".delay");
                        this.ore = new ItemStack(Material.IRON_INGOT);
                        this.amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_MEDIUM + ".amount") == null ?
                                "Default." + ConfigPath.GENERATOR_IRON_MEDIUM + ".amount" : arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_MEDIUM + ".amount");
                        this.spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT) == null ?
                                "Default." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_IRON_SPAWN_LIMIT);
                        break;
                }
                break;
            case DIAMOND:
                delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_DELAY) == null ?
                        "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_I_DELAY : arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_DELAY);
                amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_AMOUNT) == null ?
                        "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_I_AMOUNT : arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_AMOUNT);
                spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_SPAWN_LIMIT) == null ?
                        "Default." + ConfigPath.GENERATOR_DIAMOND_TIER_I_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_DIAMOND_TIER_I_SPAWN_LIMIT);
                ore = new ItemStack(Material.DIAMOND);
                break;
            case EMERALD:
                delay = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_DELAY) == null ?
                        "Default." + ConfigPath.GENERATOR_EMERALD_TIER_I_DELAY : arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_DELAY);
                amount = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_AMOUNT) == null ?
                        "Default." + ConfigPath.GENERATOR_EMERALD_TIER_I_AMOUNT : arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_AMOUNT);
                spawnLimit = getGeneratorsCfg().getInt(getGeneratorsCfg().getYml().get(arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_SPAWN_LIMIT) == null ?
                        "Default." + ConfigPath.GENERATOR_EMERALD_TIER_I_SPAWN_LIMIT : arena.getGroup() + "." + ConfigPath.GENERATOR_EMERALD_TIER_I_SPAWN_LIMIT);
                ore = new ItemStack(Material.EMERALD);
                break;
        }
        lastSpawn = delay;
    }

    // MODIFICATION: Added the manual getArena() method with @Override
    @Override
    public IArena getArena() {
        return this.arena;
    }

    @Override
    public ITeam getBwt() {
        return bwt;
    }

    @Override
    public ArmorStand getHologramHolder() {
        return item;
    }

    @Override
    public GeneratorType getType() {
        return type;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public int getDelay() {
        return delay;
    }

    @Override
    public int getNextSpawn() {
        return lastSpawn;
    }

    @Override
    public int getSpawnLimit() {
        return spawnLimit;
    }

    @Override
    public void setNextSpawn(int nextSpawn) {
        this.lastSpawn = nextSpawn;
    }

    @Override
    public void setStack(boolean stack) {
        this.stack = stack;
    }

    @Override
    public boolean isStack() {
        return stack;
    }

    @Override
    public void setType(GeneratorType type) {
        this.type = type;
    }

    public void destroyData() {
        rotation.remove(this);
        location = null;
        arena = null;
        ore = null;
        bwt = null;
        armorStands = null;
        item = null;
    }

    @Override
    public GeneratorSpeed getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(GeneratorSpeed speed) {
        this.speed = speed;
    }
}
