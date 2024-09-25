package com.namelessju.scathapro.managers;

import java.io.File;
import java.time.LocalDate;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.achievements.UnlockedAchievement;
import com.namelessju.scathapro.events.DailyScathaFarmingStreakChangedEvent;
import com.namelessju.scathapro.events.DailyStatsResetEvent;
import com.namelessju.scathapro.miscellaneous.WormStats;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.TimeUtil;
import com.namelessju.scathapro.util.FileUtil;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;

public class PersistentData
{
    public static final File file = SaveManager.getSaveFile("persistentData.json");
    
    private static final String unlockedAchievementsKey = "unlockedAchievements";
    private static final String petDropsKey = "petDrops";
    private static final String wormKillsKey = "wormKills";
    private static final String dayKey = "daily";
    
    
    private final ScathaPro scathaPro;
    
    private JsonObject data = new JsonObject();
    private UUID loadedPlayerUuid = null;
    private String loadedPlayerUuidString = null;
    
    public PersistentData(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void loadFile()
    {
        if (file.exists() && file.canRead())
        {
            String jsonString = FileUtil.readFile(file);
            if (jsonString == null)
            {
                scathaPro.logError("Couldn't load persistent data (failed to read file)");
                onLoadError();
                return;
            }
            
            JsonElement dataJson;
            
            try
            {
                dataJson = new JsonParser().parse(jsonString);
            }
            catch (Exception E)
            {
                scathaPro.logError("Couldn't load persistent data (invalid JSON format)");
                onLoadError();
                return;
            }
                
            if (dataJson.isJsonObject())
            {
                data = dataJson.getAsJsonObject();
                
                if (loadedPlayerUuidString != null && (data.get("unlockedAchievements") != null || data.get("petDrops") != null))
                {
                    JsonObject oldData = data;
                    data = new JsonObject();
                    data.add(loadedPlayerUuidString, oldData);
                }
                
                loadCurrentPlayer();
                
                scathaPro.logDebug("Persistent data loaded");
            }
            else
            {
                scathaPro.logError("Couldn't load persistent data (JSON root is not an object)");
                onLoadError();
            }
        }
    }
    
    public void loadCurrentPlayer()
    {
        loadedPlayerUuid = Util.getPlayerUUID();
        loadedPlayerUuidString = Util.getUUIDString(loadedPlayerUuid);
        
        loadAchievements();
        loadPetDrops();
        loadWormKills();
        loadDayData();
    }
    
    /**
     * Checks if the loaded player matches the current client's logged in player and loads the correct data if necessary 
     */
    public void updateLoadedPlayer()
    {
        if (!Util.getPlayerUUID().equals(loadedPlayerUuid))
        {
            loadCurrentPlayer();
        }
    }
    
    public void saveData()
    {
        if (loadedPlayerUuidString != null)
        {
            if (!FileUtil.writeFile(file, data.toString()))
            {
                scathaPro.logError("Error while trying to save persistent data");
            }
        }
        else TextUtil.sendModErrorMessage("Persistent data can't be saved, your session is offline!");
    }
    
    public JsonObject getCurrentPlayerObject()
    {
        JsonElement playerElement = data.get(loadedPlayerUuidString);
        
        if (playerElement != null && playerElement.isJsonObject())
        {
            return playerElement.getAsJsonObject();
        }
        
        return null;
    }
    
    public JsonObject getData()
    {
        return data;
    }

    public boolean setInCurrentPlayer(String path, JsonElement value)
    {
        if (loadedPlayerUuidString == null) return false;
        
        JsonElement playerData = data.get(loadedPlayerUuidString);
        if (playerData == null || !playerData.isJsonObject())
        {
            playerData = new JsonObject();
            data.add(loadedPlayerUuidString, playerData);
        }
        
        JsonUtil.set(playerData.getAsJsonObject(), path, value);
        return true;
    }
    
    
    private void loadAchievements() {
        try
        {
            JsonArray achievementsJsonArray = JsonUtil.getJsonArray(getCurrentPlayerObject(), unlockedAchievementsKey);
            if (achievementsJsonArray != null)
            {
                AchievementManager achievementManager = scathaPro.getAchievementManager();
                achievementManager.clearUnlockedAchievements();
                
                long now = TimeUtil.now();
                
                for (JsonElement achievementJsonElement : achievementsJsonArray)
                {
                    String achievementID = JsonUtil.getString(achievementJsonElement, "achievementID");
                    Long unlockedAtTimestamp = JsonUtil.getLong(achievementJsonElement, "unlockedAt");
                    
                    if (achievementID != null && unlockedAtTimestamp != null)
                    {
                        Achievement achievement = Achievement.getByID(achievementID);
                        
                        if (achievement != null && !achievementManager.isAchievementUnlocked(achievement))
                        {
                            if (unlockedAtTimestamp > now || unlockedAtTimestamp < 1640991600000L)
                            {
                                scathaPro.variables.cheaterDetected = true;
                            }
                            
                            achievementManager.addUnlockedAchievement(new UnlockedAchievement(achievement, unlockedAtTimestamp));
                            achievement.setProgress(achievement.goal);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            scathaPro.logError("Error while trying to load achievements data:");
            e.printStackTrace();
            onLoadError();
        }
    }
    
    public void saveAchievements()
    {
        JsonArray unlockedAchievementsJson = new JsonArray();
        
        UnlockedAchievement[] unlockedAchievements = scathaPro.getAchievementManager().getAllUnlockedAchievements();
        for (UnlockedAchievement unlockedAchievement : unlockedAchievements)
        {
            JsonObject achievementObject = new JsonObject();
            achievementObject.add("achievementID", new JsonPrimitive(unlockedAchievement.achievement.getID()));
            achievementObject.add("unlockedAt", new JsonPrimitive(unlockedAchievement.unlockedAtTimestamp));
            unlockedAchievementsJson.add(achievementObject);
        }
        
        setInCurrentPlayer(unlockedAchievementsKey, unlockedAchievementsJson);
        saveData();
    }
    
    
    private void loadPetDrops()
    {
        try
        {
            JsonObject playerJson = getCurrentPlayerObject();
            if (playerJson == null) return;
            
            Integer rarePetDrops = JsonUtil.getInt(playerJson, petDropsKey + "/rare");
            Integer epicPetDrops = JsonUtil.getInt(playerJson, petDropsKey + "/epic");
            Integer legendaryPetDrops = JsonUtil.getInt(playerJson, petDropsKey + "/legendary");
            
            if (rarePetDrops != null) scathaPro.variables.rarePetDrops = rarePetDrops;
            if (epicPetDrops != null) scathaPro.variables.epicPetDrops = epicPetDrops;
            if (legendaryPetDrops != null) scathaPro.variables.legendaryPetDrops = legendaryPetDrops;
            
            
            if
            (
                scathaPro.variables.rarePetDrops > Constants.maxLegitPetDropsAmount || scathaPro.variables.rarePetDrops < 0
                || scathaPro.variables.epicPetDrops > Constants.maxLegitPetDropsAmount || scathaPro.variables.epicPetDrops < 0
                || scathaPro.variables.legendaryPetDrops > Constants.maxLegitPetDropsAmount || scathaPro.variables.legendaryPetDrops < 0
            )
            {
                scathaPro.variables.cheaterDetected = true;
            }
            
            
            Integer scathaKillsAtLastDrop = JsonUtil.getInt(playerJson, petDropsKey + "/scathaKillsAtLastDrop");
            if (scathaKillsAtLastDrop != null)
            {
                scathaPro.variables.scathaKillsAtLastDrop = scathaKillsAtLastDrop;
            }

            scathaPro.getOverlay().updatePetDrops();
            scathaPro.getOverlay().updateScathaKillsSinceLastDrop();
        }
        catch (Exception e)
        {
            scathaPro.logError("Error while trying to load pet drops data:");
            e.printStackTrace();
            onLoadError();
        }
    }
    
    public void savePetDrops()
    {
        JsonObject petDropsJsonObject = new JsonObject();
        petDropsJsonObject.add("rare", new JsonPrimitive(scathaPro.variables.rarePetDrops));
        petDropsJsonObject.add("epic", new JsonPrimitive(scathaPro.variables.epicPetDrops));
        petDropsJsonObject.add("legendary", new JsonPrimitive(scathaPro.variables.legendaryPetDrops));
        
        if (scathaPro.variables.scathaKillsAtLastDrop >= 0)
        {
            petDropsJsonObject.add("scathaKillsAtLastDrop", new JsonPrimitive(scathaPro.variables.scathaKillsAtLastDrop));
        }
        
        setInCurrentPlayer(petDropsKey, petDropsJsonObject);
        saveData();
    }
    
    
    private void loadWormKills() {
        try
        {
            JsonObject playerJson = getCurrentPlayerObject();
            if (playerJson == null) return;
            
            Integer regularWormKills = JsonUtil.getInt(playerJson, wormKillsKey + "/regularWorms");
            Integer scathaKills = JsonUtil.getInt(playerJson, wormKillsKey + "/scathas");
            
            if (regularWormKills != null) scathaPro.variables.regularWormKills = regularWormKills;
            if (scathaKills != null) scathaPro.variables.scathaKills = scathaKills;
            
            scathaPro.getOverlay().updateWormKills();
            scathaPro.getOverlay().updateScathaKills();
        }
        catch (Exception e)
        {
            scathaPro.logError("Error while trying to load worm kills data:");
            e.printStackTrace();
            onLoadError();
        }
    }
    
    public void saveWormKills()
    {
        JsonObject wormKillsJsonObject = new JsonObject();
        
        wormKillsJsonObject.add("regularWorms", new JsonPrimitive(scathaPro.variables.regularWormKills));
        wormKillsJsonObject.add("scathas", new JsonPrimitive(scathaPro.variables.scathaKills));
        
        setInCurrentPlayer(wormKillsKey, wormKillsJsonObject);
        saveData();
    }
    
    private void loadDayData()
    {
        try
        {
            JsonObject dayData = JsonUtil.getJsonObject(getCurrentPlayerObject(), dayKey);
            
            scathaPro.variables.lastPlayedDate = TimeUtil.parseDate(JsonUtil.getString(dayData, "lastPlayed"));
            
            WormStats.PER_DAY.regularWormKills = Util.intOrZero(JsonUtil.getInt(dayData, "stats/wormKills/regularWorms"));
            WormStats.PER_DAY.scathaKills = Util.intOrZero(JsonUtil.getInt(dayData, "stats/wormKills/scathas"));
            WormStats.PER_DAY.scathaSpawnStreak = Util.intOrZero(JsonUtil.getInt(dayData, "stats/scathaSpawnStreak"));
            
            scathaPro.variables.scathaFarmingStreak = Util.intOrZero(JsonUtil.getInt(dayData, "scathaFarming/streak"));
            scathaPro.variables.scathaFarmingStreakHighscore = Util.intOrZero(JsonUtil.getInt(dayData, "scathaFarming/streakHighscore"));
            
            scathaPro.variables.lastScathaFarmedDate = TimeUtil.parseDate(JsonUtil.getString(dayData, "scathaFarming/lastFarmed"));
        }
        catch (Exception e)
        {
            scathaPro.logError("Error while trying to load day data:");
            e.printStackTrace();
            onLoadError();
        }
    }
    
    public void saveDailyStatsData()
    {
        JsonObject dayData = getDayData();
        JsonUtil.set(dayData, "stats/wormKills/regularWorms", new JsonPrimitive(WormStats.PER_DAY.regularWormKills));
        JsonUtil.set(dayData, "stats/wormKills/scathas", new JsonPrimitive(WormStats.PER_DAY.scathaKills));
        JsonUtil.set(dayData, "stats/scathaSpawnStreak", new JsonPrimitive(WormStats.PER_DAY.scathaSpawnStreak));
        saveData();
    }
    
    public void resetDailyStats()
    {
        scathaPro.variables.lastPlayedDate = TimeUtil.today();
        setInCurrentPlayer(dayKey + "/lastPlayed", new JsonPrimitive(TimeUtil.serializeDate(scathaPro.variables.lastPlayedDate)));
        
        WormStats.PER_DAY.regularWormKills = 0;
        WormStats.PER_DAY.scathaKills = 0;
        WormStats.PER_DAY.scathaSpawnStreak = 0;
        
        saveDailyStatsData();
        
        MinecraftForge.EVENT_BUS.post(new DailyStatsResetEvent());
        
        scathaPro.logDebug("Daily stats reset");
        
        if (TextUtil.getPlayerForChat() != null)
        {
            TextUtil.sendModChatMessage("New IRL day started - per day stats reset");
        }
    }
    
    public void updateScathaFarmingStreak(boolean increase)
    {
        LocalDate today = TimeUtil.today();
        
        boolean streakUpdated = false;
        boolean highscoreUpdated = false;
        
        if (increase && (scathaPro.variables.scathaFarmingStreak == 0 || today.minusDays(1).equals(scathaPro.variables.lastScathaFarmedDate)))
        {
            scathaPro.variables.scathaFarmingStreak ++;
            streakUpdated = true;
            
            if (scathaPro.variables.scathaFarmingStreak > scathaPro.variables.scathaFarmingStreakHighscore)
            {
                scathaPro.variables.scathaFarmingStreakHighscore = scathaPro.variables.scathaFarmingStreak;
                highscoreUpdated = true;
            }
            
            if (scathaPro.getConfig().getBoolean(Config.Key.dailyScathaFarmingStreakMessage))
            {
                TextUtil.sendModChatMessage(Constants.msgHighlightingColor + "First Scatha kill of the day! You reached a daily Scatha farming streak of " + EnumChatFormatting.GREEN + scathaPro.variables.scathaFarmingStreak + " day" + (scathaPro.variables.scathaFarmingStreak != 1 ? "s" : "") + (highscoreUpdated ? EnumChatFormatting.GOLD + " (new highscore!)" : "") + EnumChatFormatting.GREEN + ".");
            }
        }
        else if (scathaPro.variables.lastScathaFarmedDate == null || !(scathaPro.variables.lastScathaFarmedDate.equals(today) || !increase && scathaPro.variables.lastScathaFarmedDate.equals(today.minusDays(1))))
        {
            int targetValue = increase ? 1 : 0;
            if (scathaPro.variables.scathaFarmingStreak != targetValue)
            {
                scathaPro.variables.scathaFarmingStreak = targetValue;
                streakUpdated = true;
                
                if (scathaPro.getConfig().getBoolean(Config.Key.dailyScathaFarmingStreakMessage) && scathaPro.variables.lastScathaFarmedDate != null)
                {
                    TextUtil.sendModChatMessage(EnumChatFormatting.RED + "You broke your daily Scatha farming streak!" + (increase ? (EnumChatFormatting.YELLOW + " Restarting the streak from 1.") : ""));
                }
            }
        }
        
        if (streakUpdated)
        {
            JsonObject dayData = getDayData();
            JsonUtil.set(dayData, "scathaFarming/streak", new JsonPrimitive(scathaPro.variables.scathaFarmingStreak));
            
            if (increase)
            {
                scathaPro.variables.lastScathaFarmedDate = today;
                JsonUtil.set(dayData, "scathaFarming/lastFarmed", new JsonPrimitive(TimeUtil.serializeDate(scathaPro.variables.lastScathaFarmedDate)));
                
                if (highscoreUpdated) JsonUtil.set(dayData, "scathaFarming/streakHighscore", new JsonPrimitive(scathaPro.variables.scathaFarmingStreakHighscore));
            }
            
            saveData();
            
            MinecraftForge.EVENT_BUS.post(new DailyScathaFarmingStreakChangedEvent());
        }
    }
    
    /**
     * Returns a non-null reference to the day data object that can be modified without having to overwrite the whole object again
     */
    private JsonObject getDayData()
    {
        JsonObject dayData = JsonUtil.getJsonObject(getCurrentPlayerObject(), dayKey);
        if (dayData == null)
        {
            dayData = new JsonObject();
            setInCurrentPlayer(dayKey, dayData);
        }
        return dayData;
    }
    
    
    private void onLoadError()
    {
        TextUtil.sendModErrorMessage("Failed to load persistent data, creating backup...");
        SaveManager.backupPersistentData("loadError");
    }
}
