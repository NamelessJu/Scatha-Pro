package com.namelessju.scathapro.alertmodes.customalertmode;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.logging.log4j.Level;

import com.google.gson.JsonObject;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.SaveManager;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.util.JsonUtil;

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
    
    
    public final CustomAlertModeResourcePack resourcePack;
    
	private String submodeId = null;
    private JsonObject currentProperties = null;
    
    
    private CustomAlertModeManager() {
        resourcePack = new CustomAlertModeResourcePack();
    }

    
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		if (resourceManager instanceof SimpleReloadableResourceManager) {
			CustomAlertModeManager.instance.loadResourcePack((SimpleReloadableResourceManager) resourceManager);
		}
		else {
    		ScathaPro.getInstance().logger.log(Level.ERROR, "Couldn't load custom alert mode resource pack - resource manager of unexpected type " + resourceManager.getClass().getCanonicalName() + " (expected " + SimpleReloadableResourceManager.class.getCanonicalName() + ")");
		}
	}
	
	private void loadResourcePack(SimpleReloadableResourceManager resourceManager) {
		String configSubmode = Config.instance.getString(Config.Key.customModeSubmode);
    	submodeId = configSubmode.isEmpty() ? null : configSubmode;
    	
		resourceManager.reloadResourcePack(resourcePack);
		loadSubModeProperties();
		
		ScathaPro.getInstance().logger.log(Level.INFO, "Custom alert mode resource pack loaded");
	}
	
	public void reloadResourcePack() {
		// Haven't found a way to reload just a single resource pack :(
		// reloading all resources is slow, but at least it does the job
		Minecraft.getMinecraft().refreshResources();
	}
	
    
    public String getSubmodeId() {
    	return submodeId;
    }
    
    public String getSubmodeDisplayName(String submodeId) {
    	if (submodeId == null) return "<none>";
    	if (!doesSubmodeExist(submodeId)) return "<not found>";
    	return StringUtils.stripControlCodes(submodeId);
    }
    
	
	private void loadSubModeProperties() {
    	IResource resource;
    	
    	try {
			resource = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation(getResourceName("properties.json")));
		} catch (IOException e) {
			currentProperties = null;
			return;
		}
    	
    	String propertiesString = SaveManager.readInputStream(resource.getInputStream());
    	currentProperties = JsonUtil.parseObject(propertiesString);
	}
    
    public String getSubModeProperty(String path) {
    	if (currentProperties == null) return null;
    	return StringUtils.stripControlCodes(JsonUtil.getString(currentProperties, path));
    }
    
    
    public String[] getAllSubmodes() {
    	return submodesDirectory.list(DirectoryFileFilter.DIRECTORY);
    }
    
    public boolean doesSubmodeExist(String submodeId) {
    	for (String mode : getAllSubmodes()) {
    		if (mode.equals(submodeId)) return true;
    	}
    	return false; 
    }
}
