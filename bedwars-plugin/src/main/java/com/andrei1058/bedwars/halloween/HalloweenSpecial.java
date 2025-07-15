

package com.andrei1058.bedwars.halloween;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.halloween.shop.PumpkinContent;
import com.andrei1058.bedwars.metrics.MetricsManager;
import com.andrei1058.bedwars.shop.ShopManager;
import com.andrei1058.bedwars.shop.main.ShopCategory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class HalloweenSpecial {

    private static HalloweenSpecial INSTANCE;

    private HalloweenSpecial() {
        BedWars.plugin.getLogger().info(ChatColor.AQUA + "Loaded Halloween Special <3");
        // pumpkin hats
        Bukkit.getPluginManager().registerEvents(new HalloweenListener(), BedWars.plugin);

        // pumpkin in shop
        ShopCategory blockCategory = ShopManager.getShop().getCategoryList().stream().filter(category -> category.getName().equals("blocks-category")).findFirst().orElse(null);
        if (blockCategory != null) {
            PumpkinContent content = new PumpkinContent(blockCategory);
            if (content.isLoaded()) {
                blockCategory.getCategoryContentList().add(content);
            }
        }
    }

    /**
     * Initialize Halloween Special.
     */
    public static void init() {
        var enable = BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ENABLE_HALLOWEEN);
        if (enable) {
            if (INSTANCE == null && checkAvailabilityDate()) {
                //noinspection InstantiationOfUtilityClass
                INSTANCE = new HalloweenSpecial();
            }
        }
        MetricsManager.appendPie("halloween_special_enable", () -> String.valueOf(enable));
    }

    protected static boolean checkAvailabilityDate() {
        // check date
        ZoneId zone = ZoneId.of("Asia/Baghdad");
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(zone).toLocalDate();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

        // allowed between October 21 and November 1
        return (month == 10 && day > 21 || month == 11 && day < 2);
    }

    public static HalloweenSpecial getINSTANCE() {
        return INSTANCE;
    }
}
