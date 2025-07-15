
package com.andrei1058.bedwars.support.papi;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;

public class SupportPAPI {

    private static supp supportPAPI = new noPAPI();

    public interface supp {
        String replace(Player p, String s);

        List<String> replace(Player p, List<String> strings);
    }

    public static class noPAPI implements supp {

        @Override
        public String replace(Player p, String s) {
            return s;
        }

        @Override
        public List<String> replace(Player p, List<String> strings) {
            return strings;
        }
    }

    public static class withPAPI implements supp {

        @Override
        public String replace(Player p, String s) {
            return PlaceholderAPI.setPlaceholders(p, s);
        }

        @Override
        public List<String> replace(Player p, List<String> strings) {
            return PlaceholderAPI.setPlaceholders(p, strings);
        }
    }

    public static supp getSupportPAPI() {
        return supportPAPI;
    }

    public static void setSupportPAPI(supp s) {
        supportPAPI = s;
    }
}
