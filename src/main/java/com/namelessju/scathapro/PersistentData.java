package com.namelessju.scathapro;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.achievements.UnlockedAchievement;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class PersistentData {
	
    private static final File saveFile = SaveManager.getModFile("persistentData.json");
    
    private static final String unlockedAchievementsKey = "unlockedAchievements";
    private static final String petDropsKey = "petDrops";
    private static final String wormKillsKey = "wormKills";
    
    public static final ScathaPro scathaPro = ScathaPro.getInstance();
    
    private JsonObject data = new JsonObject();
    
    public void loadData() {
        if (saveFile.exists() && saveFile.canRead())
        {
        	String jsonString = SaveManager.readFile(saveFile);
        	if (jsonString == null) {
            	ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't load persistent data (failed to read file)");
        		return;
        	}
        	
            JsonElement dataJson = new JsonParser().parse(jsonString);
            if (dataJson.isJsonObject()) {
                data = dataJson.getAsJsonObject();
                
                String uuid = Util.getPlayerUUIDString(); 
                if (uuid != null && (data.get("unlockedAchievements") != null || data.get("petDrops") != null)) {
                    JsonObject oldData = data;
                    data = new JsonObject();
                    data.add(uuid, oldData);
                }
                
                loadAchievements();
                loadPetDrops();
                loadWormKills();
            }
            else {
            	ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't load persistent data (JSON root is not an object)");
                onLoadError();
            }
        }
    }
    
    public void saveData() {
        if (Util.getPlayerUUIDString() != null) {
            if (!SaveManager.writeFile(saveFile, data.toString())) {
                ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to save persistent data");
            }
        }
        else MessageUtil.sendModErrorMessage("Persistent data can't be saved, your session is offline!");
    }

    public void backup() {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd_kk-mm-ss-SSS");
        backup(date.format(formatter), false);
    }
    public void backup(String name) {
        backup(name, false);
    }
    public void backup(String name, boolean overwrite) {
        File backupFile = SaveManager.getModFile("backups/persistentData_" + name + ".json");

        File backupFolder = backupFile.getParentFile();
        if (!backupFolder.exists()) backupFolder.mkdirs();
        
        if (backupFile.exists() && !overwrite) {
            MessageUtil.sendModErrorMessage("Couldn't backup persistent data: File already exists");
            return;
        }
        
        try {
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
        catch (IOException e) {
            e.printStackTrace();
            MessageUtil.sendModErrorMessage("Couldn't backup persistent data: Failed to write file");
        }
    }
    
    public JsonObject getCurrentPlayerObject() {
        JsonElement playerElement = data.get(Util.getPlayerUUIDString());
        
        if (playerElement != null && playerElement.isJsonObject())
            return playerElement.getAsJsonObject();
        
        return null;
    }
    
    public JsonObject getData() {
        return data;
    }

    public boolean set(String path, JsonElement value) {
        String uuid = Util.getPlayerUUIDString();
        if (uuid == null) return false;
        
        JsonElement playerData = data.get(uuid);
        if (playerData == null || !playerData.isJsonObject()) {
            playerData = new JsonObject();
            data.add(uuid, playerData);
        }
        
        JsonUtil.set(playerData.getAsJsonObject(), path, value);
        return true;
    }
    
    
    private void loadAchievements() {
        try {
            JsonArray achievementsJsonArray = JsonUtil.getJsonArray(getCurrentPlayerObject(), unlockedAchievementsKey);
            
            if (achievementsJsonArray != null) {
            	AchievementManager achievementManager = ScathaPro.getInstance().achievementManager;
            
            	achievementManager.unlockedAchievements.clear();
                
                long now = Util.getCurrentTime();
                
                for (JsonElement achievementJsonElement : achievementsJsonArray) {
                    
                    String achievementID = JsonUtil.getString(achievementJsonElement, "achievementID");
                    Long unlockedAtTimestamp = JsonUtil.getLong(achievementJsonElement, "unlockedAt");
                    
                    if (achievementID != null && unlockedAtTimestamp != null) {
                        
                        Achievement achievement = Achievement.getByID(achievementID);
    
                        if (achievement != null && !achievementManager.isAchievementUnlocked(achievement)) {
                            
                            if (unlockedAtTimestamp > now || unlockedAtTimestamp < 1640991600000L)
                                scathaPro.showFakeBan = true;
                            
                            achievementManager.unlockedAchievements.add(new UnlockedAchievement(achievement, unlockedAtTimestamp));
                            achievement.setProgress(achievement.goal);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to load achievements data:");
            e.printStackTrace();
            
            onLoadError();
        }
    }
    
    public void saveAchievements() {
        JsonArray unlockedAchievementsJson = new JsonArray();
        
        for (UnlockedAchievement unlockedAchievement : ScathaPro.getInstance().achievementManager.unlockedAchievements) {
            JsonObject achievementObject = new JsonObject();
            achievementObject.add("achievementID", new JsonPrimitive(unlockedAchievement.achievement.getID()));
            achievementObject.add("unlockedAt", new JsonPrimitive(unlockedAchievement.unlockedAtTimestamp));
            unlockedAchievementsJson.add(achievementObject);
        }
        
        set(unlockedAchievementsKey, unlockedAchievementsJson);
        
        saveData();
    }
    
    
    private void loadPetDrops() {
        try {
        	JsonObject playerJson = getCurrentPlayerObject();
            
            if (playerJson != null) {
                
                Integer rarePetDrops = JsonUtil.getInt(playerJson, petDropsKey + "/rare");
                Integer epicPetDrops = JsonUtil.getInt(playerJson, petDropsKey + "/epic");
                Integer legendaryPetDrops = JsonUtil.getInt(playerJson, petDropsKey + "/legendary");
                
                if (rarePetDrops != null) scathaPro.rarePetDrops = rarePetDrops;
                if (epicPetDrops != null) scathaPro.epicPetDrops = epicPetDrops;
                if (legendaryPetDrops != null) scathaPro.legendaryPetDrops = legendaryPetDrops;
                
                
                if (
                        scathaPro.rarePetDrops > 9999 || scathaPro.rarePetDrops < 0
                        || scathaPro.epicPetDrops > 9999 || scathaPro.epicPetDrops < 0
                        || scathaPro.legendaryPetDrops > 9999 || scathaPro.legendaryPetDrops < 0
                    )
                    scathaPro.showFakeBan = true;
                
                
                Integer scathaKillsAtLastDrop = JsonUtil.getInt(playerJson, petDropsKey + "/scathaKillsAtLastDrop");
                if (scathaKillsAtLastDrop != null) {
                    scathaPro.scathaKillsAtLastDrop = scathaKillsAtLastDrop;
                }
                
                ScathaPro.getInstance().overlayManager.updateScathaKillsSinceLastDrop();
            }
        }
        catch (Exception e) {
            ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to load pet drops data:");
            e.printStackTrace();
            
            onLoadError();
        }
    }
    
    public void savePetDrops() {
        JsonObject petDropsJsonObject = new JsonObject();
        
        petDropsJsonObject.add("rare", new JsonPrimitive(scathaPro.rarePetDrops));
        petDropsJsonObject.add("epic", new JsonPrimitive(scathaPro.epicPetDrops));
        petDropsJsonObject.add("legendary", new JsonPrimitive(scathaPro.legendaryPetDrops));
        
        if (scathaPro.scathaKillsAtLastDrop >= 0) {
        	petDropsJsonObject.add("scathaKillsAtLastDrop", new JsonPrimitive(scathaPro.scathaKillsAtLastDrop));
        }
        
        set(petDropsKey, petDropsJsonObject);
        
        saveData();
    }
    
    
    private void loadWormKills() {
        try {
        	JsonObject playerJson = getCurrentPlayerObject();
            
            if (playerJson != null) {
                
                Integer regularWormKills = JsonUtil.getInt(playerJson, wormKillsKey + "/regularWorms");
                Integer scathaKills = JsonUtil.getInt(playerJson, wormKillsKey + "/scathas");
                
                if (regularWormKills != null) scathaPro.overallRegularWormKills = regularWormKills;
                if (scathaKills != null) scathaPro.overallScathaKills = scathaKills;
                
                if ((regularWormKills == null || scathaKills == null) && scathaPro.config.getBoolean(Config.Key.automaticStatsParsing)) {
                	MessageUtil.sendModChatMessage(EnumChatFormatting.YELLOW + "Open \"/be worms\" to load previous worm kills into the overlay!");
                }
                
                ScathaPro.getInstance().overlayManager.updateWormKills();
                ScathaPro.getInstance().overlayManager.updateScathaKills();
            }
        }
        catch (Exception e) {
            ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to load worm kills data:");
            e.printStackTrace();
            
            onLoadError();
        }
    }
    
    public void saveWormKills() {
        JsonObject wormKillsJsonObject = new JsonObject();
        
        wormKillsJsonObject.add("regularWorms", new JsonPrimitive(scathaPro.overallRegularWormKills));
        wormKillsJsonObject.add("scathas", new JsonPrimitive(scathaPro.overallScathaKills));
        
        set(wormKillsKey, wormKillsJsonObject);
        
        saveData();
    }
    
    
    private void onLoadError() {
    	MessageUtil.sendModErrorMessage("Error while loading persistent data, creating backup...");
    	backup();
    }
}
