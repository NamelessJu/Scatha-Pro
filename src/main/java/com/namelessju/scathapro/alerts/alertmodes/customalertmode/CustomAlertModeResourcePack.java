package com.namelessju.scathapro.alerts.alertmodes.customalertmode;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.logging.log4j.Level;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.Alert;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

public class CustomAlertModeResourcePack implements IResourcePack
{
    private static final Set<String> resourceDomains = ImmutableSet.<String>of(CustomAlertModeManager.resourceDomain);
    
    private JsonObject generateSoundsJson()
    {
        Alert[] alertSounds = Alert.values();
        
        JsonObject soundsJson = new JsonObject();
        for (Alert sound : alertSounds)
        {
            String soundName = sound.alertId;
            if (resourceExists(new ResourceLocation(CustomAlertModeManager.getResourceName("sounds/" + soundName + ".ogg"))))
            {
                addSoundToJson(soundName, soundsJson);
                // ScathaPro.getInstance().logger.log(Level.INFO, "Added sound to custom mode sound JSON: " + soundName);
            }
        }
        return soundsJson;
    }
    
    private void addSoundToJson(String soundName, JsonObject json)
    {
        // weird indentation to visualize JSON structure
        
        JsonObject soundEntry = new JsonObject();
        
            soundEntry.add("category", new JsonPrimitive("master"));
            
                JsonArray soundsArray = new JsonArray();
                
                    JsonObject soundListEntry = new JsonObject();
                    soundListEntry.add("name", new JsonPrimitive(soundName));
                    soundListEntry.add("stream", new JsonPrimitive(true));
                    
                soundsArray.add(soundListEntry);
                
            soundEntry.add("sounds", soundsArray);
        
        json.add(soundName, soundEntry);
    }

    @Override
    public boolean resourceExists(ResourceLocation location)
    {
        String subMode = ScathaPro.getInstance().customAlertModeManager.getCurrentSubmodeId();
        if (subMode == null) return false;
        
        if (location.getResourcePath().equals("sounds.json"))
        {
            return true;
        }
        
        String path = location.getResourcePath();
        
        File file = CustomAlertModeManager.getSubModeFile(subMode + "/assets/" + path);
        // ScathaPro.getInstance().logger.log(Level.INFO, "Checking for resource file \"" + file.getAbsolutePath() + "\" (" + (file != null && file.isFile() ? "exists" : "not found") + ")");
        return file != null && file.isFile();
    }
    
    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException
    {
        String subMode = ScathaPro.getInstance().customAlertModeManager.getCurrentSubmodeId();
        
        if (subMode == null)
        {
            ScathaPro.getInstance().logger.log(Level.WARN, "Tried to get input stream for custom alert mode resource, but no custom mode is set!");
            return null;
        }
        
        if (location.getResourcePath().equals("sounds.json"))
        {
            String jsonString = generateSoundsJson().toString();
            InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            return inputStream;
        }
        
        String path = location.getResourcePath();
        
        File file = CustomAlertModeManager.getSubModeFile(subMode + "/assets/" + path);
        return file != null && file.isFile() ? new FileInputStream(file) : null;
    }
    
    @Override
    public Set<String> getResourceDomains()
    {
        return resourceDomains;
    }

    @Override
    public <T extends IMetadataSection> T getPackMetadata(IMetadataSerializer p_135058_1_, String p_135058_2_) throws IOException
    {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException
    {
        return null;
    }

    @Override
    public String getPackName()
    {
        return "Scatha-Pro Custom Alert Mode Resources";
    }

}
