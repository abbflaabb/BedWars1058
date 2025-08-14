package com.andrei1058.bedwars.support.papi;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.stats.IPlayerStats;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.commands.shout.ShoutCommand;
import com.andrei1058.bedwars.stats.PlayerStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class PAPISupport extends PlaceholderExpansion {
    public DecimalFormat df = new DecimalFormat("#.##");

    public static String formatText(Player player, IArena arena, ITeam team, String text) {
        return text;
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "bw1058";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return "andrei1058";
    }

    @NotNull
    @Override
    public String getVersion() {
        return BedWars.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String s) {

        if (s.startsWith("arena_status_")) {
            IArena a = Arena.getArenaByName(s.replace("arena_status_", ""));
            return a == null
                    ? (player == null
                    ? Language.getDefaultLanguage().m(Messages.ARENA_STATUS_RESTARTING_NAME)
                    : Language.getMsg(player, Messages.ARENA_STATUS_RESTARTING_NAME))
                    : a.getDisplayStatus(Language.getDefaultLanguage());
        }

        if (s.startsWith("arena_count_")) {
            int players = 0;
            String[] arenas = s.replace("arena_count_", "").split("\\+");
            for (String arena : arenas) {
                IArena a = Arena.getArenaByName(arena);
                if (a != null) players += a.getPlayers().size();
            }
            return String.valueOf(players);
        }

        if (s.startsWith("group_count_")) {
            return String.valueOf(Arena.getPlayers(s.replace("group_count_", "")));
        }

        if (s.startsWith("arena_group_")) {
            IArena arena = Arena.getArenaByName(s.replace("arena_group_", ""));
            return arena != null ? arena.getGroup() : "-";
        }

        if (player == null) return null;

        // Stats placeholders
        if (s.startsWith("stats_")) {
            IPlayerStats stats = BedWars.getStatsManager().getUnsafe(player.getUniqueId());
            if (stats == null) return null;

            switch (s.replace("stats_", "")) {
                case "firstplay":
                    Instant firstPlay = stats.getFirstPlay();
                    return firstPlay != null ? new SimpleDateFormat(getMsg(player, Messages.FORMATTING_STATS_DATE_FORMAT)).format(Timestamp.from(firstPlay)) : "";
                case "lastplay":
                    Instant lastPlay = stats.getLastPlay();
                    return lastPlay != null ? new SimpleDateFormat(getMsg(player, Messages.FORMATTING_STATS_DATE_FORMAT)).format(Timestamp.from(lastPlay)) : "";
                case "total_kills": return String.valueOf(stats.getTotalKills());
                case "kills": return String.valueOf(stats.getKills());
                case "wins": return String.valueOf(stats.getWins());
                case "finalkills": return String.valueOf(stats.getFinalKills());
                case "deaths": return String.valueOf(stats.getDeaths());
                case "losses": return String.valueOf(stats.getLosses());
                case "finaldeaths": return String.valueOf(stats.getFinalDeaths());
                case "bedsdestroyed": return String.valueOf(stats.getBedsDestroyed());
                case "gamesplayed": return String.valueOf(stats.getGamesPlayed());
                case "kdr": {
                    if (stats.getDeaths() == 0 && stats.getKills() == 0) {
                        return "N/D";
                    }
                    return String.valueOf(df.format((double) stats.getKills() / stats.getDeaths()));
                }
                case "fkdr": {
                    if (stats.getFinalDeaths() == 0 && stats.getFinalKills() == 0) {
                        return "N/D";
                    }
                    return String.valueOf(df.format((double) stats.getFinalKills() / stats.getFinalDeaths()));
                }
                case "wlr": {
                    if (stats.getWins() == 0 && stats.getLosses() == 0) {
                        return "N/D";
                    }
                    return String.valueOf(df.format((double) stats.getWins() / stats.getLosses()));
                }
            }
        }

        IArena a = Arena.getArenaByPlayer(player);
        String response = "";

        switch (s) {
            case "current_online":
                response = String.valueOf(Arena.getArenaByPlayer().size());
                break;
            case "current_arenas":
                response = String.valueOf(Arena.getArenas().size());
                break;
            case "current_playing":
                if (a != null) response = String.valueOf(a.getPlayers().size());
                break;
            case "player_team_color":
                if (a != null && a.isPlayer(player) && a.getStatus() == GameState.playing) {
                    ITeam team = a.getTeam(player);
                    if (team != null) response = String.valueOf(team.getColor().chat());
                }
                break;
            case "player_team":
                if (a != null) {
                    if (ShoutCommand.isShout(player)) {
                        response += Language.getMsg(player, Messages.FORMAT_PAPI_PLAYER_TEAM_SHOUT);
                    } else if (a.isPlayer(player)) {
                        if (a.getStatus() == GameState.playing) {
                            ITeam team = a.getTeam(player);
                            if (team != null) {
                                response = Language.getMsg(player, Messages.FORMAT_PAPI_PLAYER_TEAM_TEAM)
                                        .replace("{TeamName}", team.getDisplayName(Language.getPlayerLanguage(player)))
                                        .replace("{TeamColor}", String.valueOf(team.getColor().chat()));
                            }
                        }
                    } else {
                        response += Language.getMsg(player, Messages.FORMAT_PAPI_PLAYER_TEAM_SPECTATOR);
                    }
                }
                break;
            case "player_level":
                response = BedWars.getLevelSupport().getLevel(player);
                break;
            case "player_level_raw":
                response = String.valueOf(BedWars.getLevelSupport().getPlayerLevel(player));
                break;
            case "player_progress":
                response = BedWars.getLevelSupport().getProgressBar(player);
                break;
            case "player_xp_formatted":
                response = BedWars.getLevelSupport().getCurrentXpFormatted(player);
                break;
            case "player_xp":
                response = String.valueOf(BedWars.getLevelSupport().getCurrentXp(player));
                break;
            case "player_rerq_xp_formatted":
                response = BedWars.getLevelSupport().getRequiredXpFormatted(player);
                break;
            case "player_rerq_xp":
                response = String.valueOf(BedWars.getLevelSupport().getRequiredXp(player));
                break;
            case "player_status":
                if (a != null) {
                    switch (a.getStatus()) {
                        case waiting:
                        case starting:
                            response = "WAITING";
                            break;
                        case playing:
                            response = a.isPlayer(player) ? "PLAYING" : a.isSpectator(player) ? "SPECTATING" : "IN_GAME_BUT_NOT";
                            break;
                        case restarting:
                            response = "RESTARTING";
                            break;
                    }
                } else {
                    response = "NONE";
                }
                break;
            case "current_arena_group":
                if (a != null) response = a.getGroup();
                break;
            case "elapsed_time":
                if (a != null && a.getStartTime() != null) {
                    Duration time = Duration.between(a.getStartTime(), Instant.now());
                    response = time.toHours() > 0
                            ? String.format("%02d:%02d:%02d", time.toHours(), time.toMinutesPart(), time.toSecondsPart())
                            : String.format("%02d:%02d", time.toMinutes(), time.toSecondsPart());
                }
                break;
            case "alive":
                response = getAlivePlayersCount(a, player);
                break;
        }

        return response;
    }

    private String getAlivePlayersCount(IArena arena, Player player) {
        if (arena == null || player == null) return getMsg(player, Messages.BED_STATUS_UNKNOWN);
        ITeam playerTeam = arena.getTeam(player);
        if (playerTeam == null) return getMsg(player, Messages.BED_STATUS_UNKNOWN);
        return playerTeam.isBedDestroyed() ? getMsg(player, Messages.BED_STATUS_DESTROYED) : getMsg(player, Messages.BED_STATUS_ALIVE);
    }

}