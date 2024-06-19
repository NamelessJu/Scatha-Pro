package com.namelessju.scathapro.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
import com.namelessju.scathapro.miscellaneous.OverlayStats;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.util.TimeUtil;
import com.namelessju.scathapro.util.FileUtil;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;

public class PersistentData
{
    public static final File saveFile = FileUtil.getModFile("persistentData.json");
    
    private static final String unlockedAchievementsKey = "unlockedAchievements";
    private static final String petDropsKey = "petDrops";
    private static final String wormKillsKey = "wormKills";
    private static final String dayKey = "daily";
    

    private final ScathaPro scathaPro;
    
    private JsonObject data = new JsonObject();
    
    public PersistentData(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void loadData()
    {
        if (saveFile.exists() && saveFile.canRead())
        {
            String jsonString = FileUtil.readFile(saveFile);
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
                
                String uuid = Util.getPlayerUUIDString(); 
                if (uuid != null && (data.get("unlockedAchievements") != null || data.get("petDrops") != null))
                {
                    JsonObject oldData = data;
                    data = new JsonObject();
                    data.add(uuid, oldData);
                }
                
                loadAchievements();
                loadPetDrops();
                loadWormKills();
                loadDayData();
                
                scathaPro.logDebug("Persistent data loaded");
            }
            else
            {
                scathaPro.logError("Couldn't load persistent data (JSON root is not an object)");
                onLoadError();
            }
        }
    }
    
    public void saveData()
    {
        if (Util.getPlayerUUIDString() != null)
        {
            if (!FileUtil.writeFile(saveFile, data.toString()))
            {
                scathaPro.logError("Error while trying to save persistent data");
            }
        }
        else MessageUtil.sendModErrorMessage("Persistent data can't be saved, your session is offline!");
    }

    public void backup()
    {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd_kk-mm-ss-SSS");
        backup(date.format(formatter), false);
    }
    
    public void backup(String name)
    {
        backup(name, false);
    }
    
    public void backup(String name, boolean overwrite)
    {
        File backupFile = FileUtil.getModFile("backups/persistentData_" + name + ".json");

        File backupFolder = backupFile.getParentFile();
        if (!backupFolder.exists()) backupFolder.mkdirs();
        
        if (backupFile.exists() && !overwrite)
        {
            MessageUtil.sendModErrorMessage("Couldn't backup persistent data: File already exists");
            return;
        }
        
        try
        {
            Files.copy(saveFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            ChatComponentText message = new ChatComponentText("Created persistent data backup as ");

            ChatComponentText path = new ChatComponentText(EnumChatFormatting.UNDERLINE + "persistentData_" + name + ".json");
            ChatStyle pathStyle = new ChatStyle()
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Open backup folder")))
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, backupFolder.getCanonicalPath()));
            path.setChatStyle(pathStyle);
            message.appendSibling(path);
            
            MessageUtil.sendModChatMessage(message);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            MessageUtil.sendModErrorMessage("Couldn't backup persistent data: Failed to write file");
        }
    }
    
    public JsonObject getCurrentPlayerObject()
    {
        JsonElement playerElement = data.get(Util.getPlayerUUIDString());
        
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
        String uuid = Util.getPlayerUUIDString();
        if (uuid == null) return false;
        
        JsonElement playerData = data.get(uuid);
        if (playerData == null || !playerData.isJsonObject())
        {
            playerData = new JsonObject();
            data.add(uuid, playerData);
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
            
            if ((regularWormKills == null || scathaKills == null) && scathaPro.getConfig().getBoolean(Config.Key.automaticStatsParsing))
            {
                MessageUtil.sendModChatMessage(EnumChatFormatting.YELLOW + "Open \"/be worms\" to load previous worm kills into the overlay!");
            }
            
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
            OverlayStats.PER_DAY.regularWormKills = Util.intOrZero(JsonUtil.getInt(dayData, "stats/wormKills/regularWorms"));
            OverlayStats.PER_DAY.scathaKills = Util.intOrZero(JsonUtil.getInt(dayData, "stats/wormKills/scathas"));
            OverlayStats.PER_DAY.scathaSpawnStreak = Util.intOrZero(JsonUtil.getInt(dayData, "stats/scathaSpawnStreak"));
            
            Integer scathaFarmingStreak = JsonUtil.getInt(dayData, "scathaFarming/streak");
            scathaPro.variables.scathaFarmingStreak = scathaFarmingStreak != null ? scathaFarmingStreak : 0;
            
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
        JsonUtil.set(dayData, "stats/wormKills/regularWorms", new JsonPrimitive(OverlayStats.PER_DAY.regularWormKills));
        JsonUtil.set(dayData, "stats/wormKills/scathas", new JsonPrimitive(OverlayStats.PER_DAY.scathaKills));
        JsonUtil.set(dayData, "stats/scathaSpawnStreak", new JsonPrimitive(OverlayStats.PER_DAY.scathaSpawnStreak));
        saveData();
    }
    
    public void resetDailyStats()
    {
        scathaPro.variables.lastPlayedDate = TimeUtil.today();
        setInCurrentPlayer(dayKey + "/lastPlayed", new JsonPrimitive(TimeUtil.formatDate(scathaPro.variables.lastPlayedDate)));
        
        OverlayStats.PER_DAY.regularWormKills = 0;
        OverlayStats.PER_DAY.scathaKills = 0;
        OverlayStats.PER_DAY.scathaSpawnStreak = 0;
        
        saveDailyStatsData();

        scathaPro.getOverlay().updateWormKills();
        scathaPro.getOverlay().updateScathaKills();
        scathaPro.getOverlay().updateTotalKills();

        scathaPro.logDebug("Daily stats reset");
        
        if (MessageUtil.getPlayerInWorld() != null)
        {
            MessageUtil.sendModChatMessage("New IRL day started - per day stats reset");
        }
    }
    
    public void updateScathaFarmingStreak()
    {
        LocalDate today = TimeUtil.today();
        
        boolean streakUpdated = false;
        
        if (today.minusDays(1).equals(scathaPro.variables.lastScathaFarmedDate))
        {
            scathaPro.variables.scathaFarmingStreak ++;
            streakUpdated = true;
            
            if (scathaPro.getConfig().getBoolean(Config.Key.dailyScathaFarmingStreakMessage))
            {
                MessageUtil.sendModChatMessage(EnumChatFormatting.RESET + "Reached a daily Scatha farming streak of " + EnumChatFormatting.GREEN + scathaPro.variables.scathaFarmingStreak + " days");
            }
        }
        else if (scathaPro.variables.lastScathaFarmedDate == null || !scathaPro.variables.lastScathaFarmedDate.equals(today))
        {
            scathaPro.variables.scathaFarmingStreak = 1;
            streakUpdated = true;
            
            if (scathaPro.getConfig().getBoolean(Config.Key.dailyScathaFarmingStreakMessage) && scathaPro.variables.lastScathaFarmedDate != null)
            {
                // TODO: do this when loading
                MessageUtil.sendModChatMessage(EnumChatFormatting.RESET + "Daily Scatha farming streak was broken and " + EnumChatFormatting.YELLOW + "starts back from 1");
            }
        }
        
        if (streakUpdated)
        {
            scathaPro.variables.lastScathaFarmedDate = today;
            
            JsonObject dayData = getDayData();
            JsonUtil.set(dayData, "scathaFarming/streak", new JsonPrimitive(scathaPro.variables.scathaFarmingStreak));
            JsonUtil.set(dayData, "scathaFarming/lastFarmed", new JsonPrimitive(TimeUtil.formatDate(scathaPro.variables.lastScathaFarmedDate)));
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
        MessageUtil.sendModErrorMessage("Error while loading persistent data, creating backup...");
        backup();
    }
}
