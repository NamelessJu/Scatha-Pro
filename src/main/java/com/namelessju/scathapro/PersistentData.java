package com.namelessju.scathapro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.minecraftforge.common.config.Configuration.UnicodeInputStreamReader;

public class PersistentData {
    
    private static final File saveFile = Util.getModFile("persistentData.json");
    private static final PersistentData instance = new PersistentData();
    
    private JsonObject data = new JsonObject();
    
    private PersistentData() {
        loadData();
    }

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
                if (dataJson instanceof JsonObject)
                    data = dataJson.getAsJsonObject();
            }
            catch (Exception e) {
                System.out.println("Error while trying to load persistent data");
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
            System.out.println("Error while trying to save persistent data");
        }
    }

    public JsonObject getData() {
        return data;
    }
    
    public JsonElement getJsonElement(String path) {
        String[] pathNodes = path.split("/");
        
        JsonElement currentElement = getData();
        for (int i = 0; i < pathNodes.length; i ++) {
            if (currentElement instanceof JsonObject) {
                JsonElement nextElement = ((JsonObject) currentElement).get(pathNodes[i]);
                currentElement = nextElement;
            }
            else return null;
        }
        
        return currentElement;
    }
    
    public JsonPrimitive getJsonPrimitive(String path) {
        JsonElement jsonElement = getJsonElement(path);
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
