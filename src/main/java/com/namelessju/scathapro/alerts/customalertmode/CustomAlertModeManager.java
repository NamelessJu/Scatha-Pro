package com.namelessju.scathapro.alerts.customalertmode;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.logging.log4j.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.SaveManager;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;

public class CustomAlertModeManager implements IResourceManagerReloadListener
{
    public static final String resourceDomain = "scathapro_customalertmode";
    public static final File submodesDirectory = SaveManager.getModFile("customAlertModes");
    
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
        return submodesDirectory.list(DirectoryFileFilter.DIRECTORY);
    }
    
    public static boolean doesSubmodeExist(String submodeId)
    {
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
            if (tries > 999) return null;
            newId = UUID.randomUUID().toString().replace("-", "");
            tries ++;
        }
        while (doesSubmodeExist(newId));
        
        return newId;
    }
    
    
    public final CustomAlertModeResourcePack resourcePack;
    
    private String currentSubmodeId = null;
    private JsonObject currentProperties = null;
    private HashMap<String, JsonObject> metas = new HashMap<String, JsonObject>();
    
    
    public CustomAlertModeManager()
    {
        resourcePack = new CustomAlertModeResourcePack();
        updateCurrentSubmode();
    }
    
    
    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        if (resourceManager != Minecraft.getMinecraft().getResourceManager()) return;
        
        if (resourceManager instanceof SimpleReloadableResourceManager)
        {
            loadResourcePack((SimpleReloadableResourceManager) resourceManager);
        }
        else
        {
            ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't load custom alert mode resource pack - resource manager of unexpected type " + resourceManager.getClass().getCanonicalName() + " (expected " + SimpleReloadableResourceManager.class.getCanonicalName() + ")");
        }
    }
    
    public void reloadResourcePack()
    {
        // Haven't found a way to reload just a single resource pack
        // reloading all resources is slow, but it does the job
        Minecraft.getMinecraft().refreshResources();
    }
    
    public void changeSubmode(String submodeId)
    {
        if (submodeId == null) submodeId = "";
        
        Config config = ScathaPro.getInstance().config;
        config.set(Config.Key.customModeSubmode, submodeId);
        config.save();
        setCurrentSubmode(submodeId);
        updateCurrentSubmodeLastUsed();
        reloadResourcePack();
    }
    
    private void updateCurrentSubmode()
    {
        String configSubmode = ScathaPro.getInstance().config.getString(Config.Key.customModeSubmode);
        setCurrentSubmode(configSubmode);
        loadCurrentSubmodeProperties();
    }
    
    private void setCurrentSubmode(String submodeId)
    {
        currentSubmodeId = submodeId.isEmpty() ? null : submodeId;
    }
    
    private void loadResourcePack(SimpleReloadableResourceManager resourceManager)
    {
        resourceManager.reloadResourcePack(resourcePack);
        
        loadCurrentSubmodeProperties();
        
        ScathaPro.getInstance().logger.log(Level.INFO, "Custom alert mode resource pack loaded");
    }
    
    
    public String getCurrentSubmodeId()
    {
        return currentSubmodeId;
    }
    
    public boolean isSubmodeActive(String submodeId)
    {
        if (currentSubmodeId == null) return false;
        else return currentSubmodeId.equals(submodeId);
    }
    
    public void deleteSubmode(String customModeId)
    {
        if (isSubmodeActive(customModeId))
        {
            changeSubmode(null);
        }
        
        File modeFolder = getSubModeFile(customModeId);
        if (!SaveManager.deleteDirectoryRecursive(modeFolder))
        {
            ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't delete custom alert mode - recursively deleting the directory failed");
        }
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
        if (submodeId == null) return EnumChatFormatting.ITALIC + "<missing mode ID>";
        if (!doesSubmodeExist(submodeId)) return EnumChatFormatting.ITALIC + "<mode not found>";
        
        String submodeName = getSubmodeName(submodeId);
        if (submodeName == null || submodeName.replace(" ", "").isEmpty()) return EnumChatFormatting.ITALIC + "<unnamed>";
        else submodeName = submodeName.trim();
        return StringUtils.stripControlCodes(submodeName);
    }
    
    
    public void loadAllMeta()
    {
        String[] submodeIds = getAllSubmodeIds();
        
        unloadAllMeta();
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
        String propertiesString = SaveManager.readFile(getMetaFile(submodeId));
        if (propertiesString != null) metas.put(submodeId, JsonUtil.parseObject(propertiesString));
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
        
        if (!SaveManager.writeFile(getMetaFile(submodeId), metaJson.toString()))
        {
            ScathaPro.getInstance().logger.log(Level.ERROR, "Failed to write custom alert mode meta file (" + submodeId + ")");
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
    
    public void updateCurrentSubmodeLastUsed()
    {
        setMeta(currentSubmodeId, "lastUsed", new JsonPrimitive(Util.getCurrentTime()));
        saveMeta(currentSubmodeId);
    }
    
    
    public JsonObject loadSubmodeProperties(String submodeId)
    {
        String propertiesString = SaveManager.readFile(getPropertiesFile(submodeId));
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
        if (!SaveManager.writeFile(propertiesFile, properties.toString()))
        {
            ScathaPro.getInstance().logger.log(Level.ERROR, "Failed to write custom alert mode properties file (" + submodeId + ")");
        }
    }
    
}
