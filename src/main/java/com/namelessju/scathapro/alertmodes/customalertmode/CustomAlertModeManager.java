package com.namelessju.scathapro.alertmodes.customalertmode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.logging.log4j.Level;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.SaveManager;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

public class CustomAlertModeManager implements IResourceManagerReloadListener {
	
	public static final String resourceDomain = "scathapro_customalertmode";
	public static final File submodesDirectory = SaveManager.getModFile("customAlertModes");
    
    public static String getResourceName(String resourcePath) {
    	return resourceDomain + ":" + resourcePath;
    }
	
    public static final CustomAlertModeManager instance = new CustomAlertModeManager();
    
    public static File getSubModeFile(String path) {
    	return new File(submodesDirectory, path);
    }
    
    
    public final CustomAlertModeResourcePack resourcePack;
    
	private String currentSubmodeId = null;
    private JsonObject currentProperties = null;
    private HashMap<String, JsonObject> metas = new HashMap<String, JsonObject>();
    
    
    private CustomAlertModeManager() {
        resourcePack = new CustomAlertModeResourcePack();
        updateCurrentSubmode();
    }
    
    
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		if (resourceManager instanceof SimpleReloadableResourceManager) {
			loadResourcePack((SimpleReloadableResourceManager) resourceManager);
		}
		else {
    		ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't load custom alert mode resource pack - resource manager of unexpected type " + resourceManager.getClass().getCanonicalName() + " (expected " + SimpleReloadableResourceManager.class.getCanonicalName() + ")");
		}
	}
	
	public void updateCurrentSubmode() {
		String configSubmode = Config.instance.getString(Config.Key.customModeSubmode);
    	currentSubmodeId = configSubmode.isEmpty() ? null : configSubmode;
    	
		loadCurrentSubmodeProperties();
	}
	
	private void loadResourcePack(SimpleReloadableResourceManager resourceManager) {
		resourceManager.reloadResourcePack(resourcePack);
		
		loadCurrentSubmodeProperties();
		
		ScathaPro.getInstance().logger.log(Level.INFO, "Custom alert mode resource pack loaded");
	}
	
	public void reloadResourcePack() {
		// Haven't found a way to reload just a single resource pack
		// reloading all resources is slow, but it does the job for now
		Minecraft.getMinecraft().refreshResources();
	}
	
    
    public String getCurrentSubmodeId() {
    	return currentSubmodeId;
    }
    
    public String getSubmodeDisplayName(String submodeId) {
    	if (submodeId == null) return "<none>";
    	if (!doesSubmodeExist(submodeId)) return "<not found>";
    	
    	String submodeName = JsonUtil.getString(metas.get(submodeId), "name");
    	if (submodeName == null) return "<unnamed>";
    	return StringUtils.stripControlCodes(submodeName);
    }
    
    public void loadMeta() {
    	String[] submodeIds = getAllSubmodeIds();
    	
    	metas.clear();
    	for (String submodeId : submodeIds) {
            File file = CustomAlertModeManager.getSubModeFile(submodeId + "/meta.json");
            if (file == null || !file.isFile()) continue;
            
            FileInputStream inputStream;
    		try {
    			inputStream = new FileInputStream(file);
    		}
    		catch (FileNotFoundException e) {
    			continue;
    		}
    		
        	String propertiesString = SaveManager.readInputStream(inputStream);
        	metas.put(submodeId, JsonUtil.parseObject(propertiesString));
    	}
    }
    
    public void setMeta(String submodeId, String path, JsonElement value) {
    	JsonObject metaJson = metas.get(submodeId);
    	if (metaJson == null) {
    		metaJson = new JsonObject();
    		metas.put(submodeId, metaJson);
    	}
    	
    	JsonUtil.set(metaJson, path, value);
    }
    
    public void updateCurrentSubmodeLastUsed(String submodeId, String path, JsonElement value) {
    	setMeta(currentSubmodeId, "lastUsed", new JsonPrimitive(Util.getCurrentTime()));
    }
    
	public void loadCurrentSubmodeProperties() {
		IResource resource = null;
		try {
			resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(getResourceName("properties.json")));
		} catch (IOException e) {}
		
		if (resource == null) {
			ScathaPro.getInstance().logger.log(Level.WARN, "Couldn't load custom alert mode properties - resource not found");
			return;
		}
		
    	String propertiesString = SaveManager.readInputStream(resource.getInputStream());
    	currentProperties = JsonUtil.parseObject(propertiesString);
	}

    public JsonElement getCurrentSubmodePropertyJsonElement(String path) {
    	if (currentProperties == null) return null;
    	return JsonUtil.getJsonElement(currentProperties, path);
    }

    public String getCurrentSubmodeProperty(String path) {
    	if (currentProperties == null) return null;
    	String propertyValue = JsonUtil.getString(currentProperties, path);
    	if (propertyValue == null) return null;
    	return StringUtils.stripControlCodes(propertyValue);
    }
    
    public String[] getAllSubmodeIds() {
    	return submodesDirectory.list(DirectoryFileFilter.DIRECTORY);
    }
    
    public boolean doesSubmodeExist(String submodeId) {
    	String[] submodeIds = getAllSubmodeIds();
    	for (String id : submodeIds) {
    		if (id.equals(submodeId)) return true;
    	}
    	return false; 
    }
    
    public String getNewSubmodeId() {
    	int tries = 0;
    	String newId;
    	do {
    		if (tries > 999) return null;
    		newId = UUID.randomUUID().toString();
    		tries ++;
    	}
    	while (doesSubmodeExist(newId));
    	
    	return newId;
    }
}
