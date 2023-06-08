package com.namelessju.scathapro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.achievements.UnlockedAchievement;
import com.namelessju.scathapro.util.ChatUtil;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.config.Configuration.UnicodeInputStreamReader;

public class PersistentData {

    public static final PersistentData instance = new PersistentData();
    
    private static final File saveFile = SaveManager.getModFile("persistentData.json");
    private static final String unlockedAchievementsKey = "unlockedAchievements";
    private static final String petDropsKey = "petDrops";

    public static final ScathaPro scathaPro = ScathaPro.getInstance();
    
    private JsonObject data = new JsonObject();
    
    private PersistentData() {}

    public void loadData() {
        if (saveFile.exists() && saveFile.canRead()) {
            try {
                UnicodeInputStreamReader inputStream = new UnicodeInputStreamReader(new FileInputStream(saveFile), "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStream);
    
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                bufferedReader.close();
                
                JsonElement dataJson = new JsonParser().parse(jsonBuilder.toString());
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
                }
                else ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't load persistent data (JSON root is not an object)");
            }
            catch (Exception e) {
                ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to load persistent data (" + e.getClass().getSimpleName() + ")");
            }
        }
    }
    
    public void saveData() {
        if (Util.getPlayerUUIDString() != null) {
            try {
                FileOutputStream outputStream = new FileOutputStream(saveFile);
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
    
                bufferedWriter.write(data.toString());
                bufferedWriter.close();
            }
            catch (Exception e) {
                ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to save persistent data");
            }
        }
        else ChatUtil.sendModErrorMessage("Your session is offline, so data won't be saved!");
    }

    public void backup(String name) {
        backup(name, false);
    }
    public void backup(String name, boolean overwrite) {
        File backupFile = SaveManager.getModFile("backups/persistentData_" + name + ".json");

        File backupFolder = backupFile.getParentFile();
        if (!backupFolder.exists()) backupFolder.mkdirs();
        
        if (backupFile.exists() && !overwrite) {
            ChatUtil.sendModErrorMessage("Couldn't backup persistent data: File already exists");
            return;
        }
        
        try {
            saveData();
            Files.copy(saveFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            ChatComponentText message = new ChatComponentText("Created persistent data backup as ");

            ChatComponentText path = new ChatComponentText(EnumChatFormatting.UNDERLINE + "persistentData_" + name + ".json");
            ChatStyle pathStyle = new ChatStyle()
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Open backup folder")))
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, backupFolder.getCanonicalPath()));
            path.setChatStyle(pathStyle);
            message.appendSibling(path);
            
            ChatUtil.sendModChatMessage(message);
        }
        catch (Exception e) {
            e.printStackTrace();
            ChatUtil.sendModErrorMessage("Couldn't backup persistent data: Failed to write file");
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
    
    
    public void loadAchievements() {
        try {
            JsonArray achievementsJsonArray = JsonUtil.getJsonArray(PersistentData.instance.getCurrentPlayerObject(), unlockedAchievementsKey);
            
            if (achievementsJsonArray != null) {
            
                AchievementManager.instance.unlockedAchievements.clear();
                
                for (JsonElement achievementJsonElement : achievementsJsonArray) {
                    
                    String achievementID = JsonUtil.getString(achievementJsonElement, "achievementID");
                    Long unlockedAtTimestamp = JsonUtil.getLong(achievementJsonElement, "unlockedAt");
                    
                    if (achievementID != null && unlockedAtTimestamp != null) {
                        
                        Achievement achievement = Achievement.getByID(achievementID);
    
                        if (achievement != null && !AchievementManager.instance.isAchievementUnlocked(achievement)) {
                            
                            if (unlockedAtTimestamp > Util.getCurrentTime() || unlockedAtTimestamp < 1640991600000L)
                                scathaPro.showFakeBan = true;
                            
                            AchievementManager.instance.unlockedAchievements.add(new UnlockedAchievement(achievement, unlockedAtTimestamp));
                            achievement.setProgress(achievement.goal);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to load achievements data:");
            e.printStackTrace();
        }
    }
    
    public void saveAchievements() {
        JsonArray unlockedAchievementsJson = new JsonArray();
        
        for (UnlockedAchievement unlockedAchievement : AchievementManager.instance.unlockedAchievements) {
            JsonObject achievementObject = new JsonObject();
            achievementObject.add("achievementID", new JsonPrimitive(unlockedAchievement.achievement.getID()));
            achievementObject.add("unlockedAt", new JsonPrimitive(unlockedAchievement.unlockedAtTimestamp));
            unlockedAchievementsJson.add(achievementObject);
        }
        
        PersistentData.instance.set(unlockedAchievementsKey, unlockedAchievementsJson);
        
        PersistentData.instance.saveData();
    }
    
    
    public void loadPetDrops() {
        try {
            JsonObject petDropsJson = JsonUtil.getJsonObject(PersistentData.instance.getCurrentPlayerObject(), petDropsKey);
            
            if (petDropsJson != null) {
                
                Integer rarePetDrops = JsonUtil.getInt(petDropsJson, "rare");
                Integer epicPetDrops = JsonUtil.getInt(petDropsJson, "epic");
                Integer legendaryPetDrops = JsonUtil.getInt(petDropsJson, "legendary");
                
                if (rarePetDrops != null) scathaPro.rarePetDrops = rarePetDrops;
                if (epicPetDrops != null) scathaPro.epicPetDrops = epicPetDrops;
                if (legendaryPetDrops != null) scathaPro.legendaryPetDrops = legendaryPetDrops;
                
                
                if (
                        scathaPro.rarePetDrops > 9999 || scathaPro.rarePetDrops < 0
                        || scathaPro.epicPetDrops > 9999 || scathaPro.epicPetDrops < 0
                        || scathaPro.legendaryPetDrops > 9999 || scathaPro.legendaryPetDrops < 0
                    )
                    scathaPro.showFakeBan = true;
                
                
                Integer scathaKillsAtLastDrop = JsonUtil.getInt(petDropsJson, "scathaKillsAtLastDrop");
                if (scathaKillsAtLastDrop != null) {
                    scathaPro.scathaKillsAtLastDrop = scathaKillsAtLastDrop;
                    
                    if (scathaPro.scathaKillsAtLastDrop < 0)
                        scathaPro.showFakeBan = true;
                }
                
                OverlayManager.instance.updateScathaKillsAtLastDrop();
            }
        }
        catch (Exception e) {
            ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to load pet drops data:");
            e.printStackTrace();
        }
    }
    
    public void savePetDrops() {
        JsonObject petDropsJsonObject = new JsonObject();
        
        petDropsJsonObject.add("rare", new JsonPrimitive(scathaPro.rarePetDrops));
        petDropsJsonObject.add("epic", new JsonPrimitive(scathaPro.epicPetDrops));
        petDropsJsonObject.add("legendary", new JsonPrimitive(scathaPro.legendaryPetDrops));
        
        if (scathaPro.scathaKillsAtLastDrop >= 0)
        	petDropsJsonObject.add("scathaKillsAtLastDrop", new JsonPrimitive(scathaPro.scathaKillsAtLastDrop));
        
        PersistentData.instance.set(petDropsKey, petDropsJsonObject);
        
        PersistentData.instance.saveData();
    }
}
