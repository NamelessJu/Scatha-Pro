package com.namelessju.scathapro.alerts.alertmodes.customalertmode;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

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
        for (Alert alert : alertSounds)
        {
            String soundName = alert.alertId;
            if (resourceExists(new ResourceLocation(CustomAlertModeManager.getResourceName("sounds/" + soundName + ".ogg"))))
            {
                addSoundToJson(alert, soundsJson);
            }
            else ScathaPro.getInstance().logDebug("Skipped sound for \"" + alert.alertName + "\" while generating custom mode sound JSON (sound file doesn't exist)");
        }
        return soundsJson;
    }
    
    private void addSoundToJson(Alert alert, JsonObject json)
    {
        // weird indentation to visualize JSON structure
        
        JsonObject soundEntry = new JsonObject();
        
            soundEntry.add("category", new JsonPrimitive("master"));
            
            JsonArray soundsArray = new JsonArray();
                
                JsonObject soundListEntry = new JsonObject();
                
                    soundListEntry.add("name", new JsonPrimitive(alert.alertId));
                    
                    boolean streamed = true;
                    /*
                    // Always streamed for now because the sound engine in 1.8 is buggy af,
                    // will come back to this when porting to latest MC version
                    
                    String currentSubMode = ScathaPro.getInstance().getCustomAlertModeManager().getCurrentSubmodeId();
                    if (currentSubMode != null)
                    {
                        Path path = Paths.get(CustomAlertModeManager.getAlertAudioFile(currentSubMode, alert).getAbsolutePath());
                        try { streamed = Files.size(path) > 512000L; }
                        catch (Exception e) {}
                    }
                    */
                    
                    soundListEntry.add("stream", new JsonPrimitive(streamed));

                    ScathaPro.getInstance().logDebug("Added sound for \"" + alert.alertName + "\" to custom mode sound JSON (streamed: " + streamed + ")");
                    
                soundsArray.add(soundListEntry);
            
            soundEntry.add("sounds", soundsArray);
        
        json.add(alert.alertId, soundEntry);
    }

    @Override
    public boolean resourceExists(ResourceLocation location)
    {
        ScathaPro.getInstance().logDebug("Checking for resource \"" + location.toString() + "\"...");
        
        String subMode = ScathaPro.getInstance().getCustomAlertModeManager().getCurrentSubmodeId();
        if (subMode == null)
        {
            ScathaPro.getInstance().logDebug("No custom mode submode is set, so resource cannot be found");
            return false;
        }
        
        if (location.getResourcePath().equals("sounds.json"))
        {
            ScathaPro.getInstance().logDebug("Resource is custom mode sounds.json, always exists (dynamically generated)");
            return true;
        }
        
        String path = location.getResourcePath();
        
        File file = CustomAlertModeManager.getSubModeFile(subMode + "/assets/" + path);
        boolean exists = file != null && file.isFile();
        ScathaPro.getInstance().logDebug("Checking for resource file \"" + file.getAbsolutePath() + "\": " + (exists ? "exists" : "not found"));
        return exists;
    }
    
    @Override
    public InputStream getInputStream(ResourceLocation location) throws IOException
    {
        String subMode = ScathaPro.getInstance().getCustomAlertModeManager().getCurrentSubmodeId();
        
        if (subMode == null)
        {
            ScathaPro.getInstance().logWarning("Tried to get input stream for custom alert mode resource, but no custom mode is set!");
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
        return ScathaPro.DYNAMIC_MODNAME + " Custom Alert Mode Resources";
    }

}
