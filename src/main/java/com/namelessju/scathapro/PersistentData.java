package com.namelessju.scathapro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.apache.logging.log4j.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.minecraftforge.common.config.Configuration.UnicodeInputStreamReader;

public class PersistentData {
    
    private static final File saveFile = Util.getModFile("persistentData.json");
    private static final PersistentData instance = new PersistentData();
    
    private JsonObject data = new JsonObject();
    
    private PersistentData() {}

    public static PersistentData getInstance() {
        return instance;
    }

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
                }
                else ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't load persistent data (JSON root is not an object)");
            }
            catch (Exception e) {
                ScathaPro.getInstance().logger.log(Level.ERROR, "Error while trying to load persistent data (" + e.getClass().getSimpleName() + ")");
            }
        }
    }
    
    public void saveData() {
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
    
    public JsonElement get(String path) {
        String[] pathNodes = path.split("/");

        JsonElement currentElement = data.get(Util.getPlayerUUIDString());
        
        if (currentElement != null) {
            for (int i = 0; i < pathNodes.length; i ++) {
                if (currentElement != null && currentElement.isJsonObject()) {
                    JsonElement nextElement = currentElement.getAsJsonObject().get(pathNodes[i]);
                    currentElement = nextElement;
                }
                else return null;
            }
            
            return currentElement;
        }
        
        return null; 
    }

    public boolean set(String path, JsonElement value) {
        String[] pathNodes = path.split("/");

        String uuid = Util.getPlayerUUIDString();
        if (uuid == null) return false;
        
        JsonElement currentElement = data.get(uuid);
        
        if (currentElement == null) {
            currentElement = new JsonObject();
            data.add(uuid, currentElement);
        }
        
        for (int i = 0; i < pathNodes.length; i ++) {
            if (i == pathNodes.length - 1) {
                if (currentElement.isJsonObject()) {
                    currentElement.getAsJsonObject().add(pathNodes[i], value);
                    return true;
                }
                else break;
            }
            else if (currentElement.isJsonObject()) {
                JsonElement nextElement = currentElement.getAsJsonObject().get(pathNodes[i]);
                currentElement = nextElement;
            }
            else break;
        }
        
        return false;
    }
    
    private JsonPrimitive getJsonPrimitive(String path) {
        JsonElement jsonElement = get(path);
        if (jsonElement instanceof JsonPrimitive)
            return (JsonPrimitive) jsonElement;
        return null;
    }
    
    public String getString(String path, String defaultValue) {
        JsonPrimitive jsonPrimitive = getJsonPrimitive(path);
        if (jsonPrimitive != null)
            return jsonPrimitive.getAsString();
        return defaultValue;
    }
    
    public int getInt(String path, int defaultValue) {
        JsonPrimitive jsonPrimitive = getJsonPrimitive(path);
        if (jsonPrimitive != null)
            return jsonPrimitive.getAsInt();
        return defaultValue;
    }
    
    public double getDouble(String path, double defaultValue) {
        JsonPrimitive jsonPrimitive = getJsonPrimitive(path);
        if (jsonPrimitive != null)
            return jsonPrimitive.getAsDouble();
        return defaultValue;
    }
    
    public boolean getBoolean(String path, boolean defaultValue) {
        JsonPrimitive jsonPrimitive = getJsonPrimitive(path);
        if (jsonPrimitive != null)
            return jsonPrimitive.getAsBoolean();
        return defaultValue;
    }
}
