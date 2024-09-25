package com.namelessju.scathapro.alerts.alertmodes.customalertmode;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.filefilter.DirectoryFileFilter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.gui.menus.CustomAlertModeResourceLoadingGui;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.SaveManager;
import com.namelessju.scathapro.util.FileUtil;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.TimeUtil;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

public class CustomAlertModeManager
{
    public static final String resourceDomain = "scathapro_customalertmode";
    public static final File submodesDirectory = SaveManager.getSaveFile("customAlertModes");
    
    public static String getResourceName(String resourcePath)
    {
        return resourceDomain + ":" + resourcePath;
    }
    
    public static File getSubModeFile(String path)
    {
        return new File(submodesDirectory, path);
    }
    
    public static File getMetaFile(String submodeId)
    {
        return getSubModeFile(submodeId + "/meta.json");
    }
    
    public static File getPropertiesFile(String submodeId)
    {
        return getSubModeFile(submodeId + "/assets/properties.json");
    }
    
    public static File getAlertAudioFile(String submodeId, Alert alert)
    {
        return getSubModeFile(submodeId + "/assets/sounds/" + alert.alertId + ".ogg");
    }
    
    public static String[] getAllSubmodeIds()
    {
        if (!submodesDirectory.exists()) return new String[0];
        return submodesDirectory.list(DirectoryFileFilter.DIRECTORY);
    }
    
    public static boolean doesSubmodeExist(String submodeId)
    {
        if (submodeId == null) return false;
        
        String[] submodeIds = getAllSubmodeIds();
        for (String id : submodeIds)
        {
            if (id.equals(submodeId)) return true;
        }
        
        return false; 
    }
    
    public static String getNewSubmodeId()
    {
        int tries = 0;
        String newId;
        do
        {
            if (tries > 9999) return null;
            newId = UUID.randomUUID().toString().replace("-", "");
            tries ++;
        }
        while (doesSubmodeExist(newId));
        
        return newId;
    }
    
    
    public final CustomAlertModeResourcePack resourcePack;
    
    private final ScathaPro scathaPro;
    
    private String currentSubmodeId = null;
    private JsonObject currentProperties = null;
    private HashMap<String, JsonObject> metas = new HashMap<String, JsonObject>();
    
    
    public CustomAlertModeManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        
        resourcePack = new CustomAlertModeResourcePack();
        loadCurrentSubmode();
    }
    
    public void reloadResourcePack()
    {
        // Haven't found a way to reload just a single resource pack
        // Reloading all resources is slow, but it does the job
        scathaPro.getMinecraft().refreshResources();
    }
    
    public void changeSubmode(String submodeId)
    {
        if (submodeId == null) submodeId = "";
        
        Config config = scathaPro.getConfig();
        config.set(Config.Key.customModeSubmode, submodeId);
        config.save();
        
        // Update previous mode's last used timestamp
        if (doesSubmodeExist(currentSubmodeId))
        {
            updateSubmodeLastUsed(currentSubmodeId);
            saveMeta(currentSubmodeId);
        }
        
        setCurrentSubmode(submodeId);
        
        // Update new mode's last used timestamp
        updateSubmodeLastUsed(submodeId);
        saveMeta(submodeId);
        
        loadCurrentSubmodeProperties();
        
        scathaPro.getMinecraft().displayGuiScreen(new CustomAlertModeResourceLoadingGui(scathaPro.getMinecraft().currentScreen));
    }
    
    private void loadCurrentSubmode()
    {
        String loadedSubmode = scathaPro.getConfig().getString(Config.Key.customModeSubmode);
        if (!doesSubmodeExist(loadedSubmode)) loadedSubmode = null;
        setCurrentSubmode(loadedSubmode);
        loadCurrentSubmodeProperties();
    }
    
    private void setCurrentSubmode(String submodeId)
    {
        currentSubmodeId = (submodeId != null && (submodeId.replace(" ", "").isEmpty())) ? null : submodeId;
    }
    
    
    public String getCurrentSubmodeId()
    {
        return currentSubmodeId;
    }
    
    public boolean isSubmodeActive(String submodeId)
    {
        if (currentSubmodeId == null) return false;
        return currentSubmodeId.equals(submodeId);
    }
    
    public void deleteSubmode(String customModeId)
    {
        boolean isActive = isSubmodeActive(customModeId);
        
        if (isActive) scathaPro.getMinecraft().getSoundHandler().unloadSounds();
        
        File modeFolder = getSubModeFile(customModeId);
        if (!FileUtil.deleteDirectoryRecursive(modeFolder))
        {
            scathaPro.logError("Couldn't delete custom alert mode - recursively deleting the directory failed");
        }
        
        if (isActive) changeSubmode(null);
    }
    
    
    public String getSubmodeName(String submodeId)
    {
        return JsonUtil.getString(metas.get(submodeId), "name");
    }
    
    public void setSubmodeName(String submodeId, String name)
    {
        setMeta(submodeId, "name", new JsonPrimitive(name));
    }
    
    public String getSubmodeDisplayName(String submodeId)
    {
        if (submodeId == null) return EnumChatFormatting.ITALIC + "(missing mode ID)";
        if (!doesSubmodeExist(submodeId)) return EnumChatFormatting.ITALIC + "(mode not found)";
        
        String submodeName = getSubmodeName(submodeId);
        if (submodeName == null || submodeName.replace(" ", "").isEmpty()) return EnumChatFormatting.ITALIC + "(unnamed)";
        else submodeName = submodeName.trim();
        return StringUtils.stripControlCodes(submodeName);
    }
    
    
    public void loadAllMeta()
    {
        unloadAllMeta();
        
        String[] submodeIds = getAllSubmodeIds();
        for (String submodeId : submodeIds)
        {
            loadMeta(submodeId);
        }
    }
    
    public void unloadAllMeta()
    {
        metas.clear();
    }
    
    public void loadMeta(String submodeId)
    {
        String metaString = FileUtil.readFile(getMetaFile(submodeId));
        if (metaString != null) metas.put(submodeId, JsonUtil.parseObject(metaString));
    }
    
    public void setMeta(String submodeId, String path, JsonElement value)
    {
        JsonObject metaJson = metas.get(submodeId);
        if (metaJson == null)
        {
            metaJson = new JsonObject();
            metas.put(submodeId, metaJson);
        }
        
        JsonUtil.set(metaJson, path, value);
    }
    
    public void saveMeta(String submodeId)
    {
        JsonObject metaJson = metas.get(submodeId);
        if (metaJson == null) metaJson = new JsonObject();
        
        if (!FileUtil.writeFile(getMetaFile(submodeId), metaJson.toString()))
        {
            scathaPro.logError("Failed to write custom alert mode meta file (" + submodeId + ")");
        }
    }

    public long getSubmodeLastUsed(String submodeId)
    {
        JsonObject metaJson = metas.get(submodeId);
        if (metaJson != null)
        {
            Long lastUsed = JsonUtil.getLong(metaJson, "lastUsed");
            if (lastUsed != null) return lastUsed;
        }
        return -1L;
    }
    
    public void updateSubmodeLastUsed(String submodeId)
    {
        if (submodeId == null || !doesSubmodeExist(submodeId)) return;
        setMeta(submodeId, "lastUsed", new JsonPrimitive(TimeUtil.now()));
    }
    
    
    public JsonObject loadSubmodeProperties(String submodeId)
    {
        String propertiesString = FileUtil.readFile(getPropertiesFile(submodeId));
        if (propertiesString != null)
        {
            JsonObject properties = JsonUtil.parseObject(propertiesString);
            if (properties != null) return properties;
        }
        
        return new JsonObject();
    }
    
    public void loadCurrentSubmodeProperties()
    {
        currentProperties = loadSubmodeProperties(currentSubmodeId);
    }

    public JsonElement getCurrentSubmodePropertyJsonElement(String path)
    {
        if (currentProperties == null) return null;
        return JsonUtil.getJsonElement(currentProperties, path);
    }

    public String getCurrentSubmodeProperty(String path)
    {
        if (currentProperties == null) return null;
        String propertyValue = JsonUtil.getString(currentProperties, path);
        if (propertyValue == null) return null;
        return StringUtils.stripControlCodes(propertyValue);
    }

    public void saveSubmodeProperties(String submodeId, JsonObject properties)
    {
        File propertiesFile = getPropertiesFile(submodeId);
        propertiesFile.getParentFile().mkdirs();
        if (!FileUtil.writeFile(propertiesFile, properties.toString()))
        {
            scathaPro.logError("Failed to write custom alert mode properties file (" + submodeId + ")");
        }
    }
    
}
