package com.namelessju.scathapro;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration.UnicodeInputStreamReader;
import net.minecraftforge.fml.common.Loader;

public abstract class SaveManager {
	
    public static File getModFile(String relativePath) {
        File saveLocation = getSaveLocation();
        if (!saveLocation.exists()) saveLocation.mkdirs();
        
        return new File(saveLocation, relativePath);
    }
    
    public static File getSaveLocation() {
        return new File(Loader.instance().getConfigDir(), ScathaPro.MODID);
    }
    
    public static String readInputStream(InputStream inputStream) {
    	try {
	        UnicodeInputStreamReader inputStreamReader = new UnicodeInputStreamReader(inputStream, "UTF-8");
	        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	        
	        StringBuilder stringBuilder = new StringBuilder();
	        
	        String line;
	        while ((line = bufferedReader.readLine()) != null) {
	        	stringBuilder.append(line);
	        }
	        bufferedReader.close();
	        
	        return stringBuilder.toString();
    	}
    	catch (Exception e) {
    		return "";
    	}
    }
    
    public static void updateOldSaveLocations() {
    	File saveLocationV1 = new File(Loader.instance().getConfigDir(), ScathaPro.MODID + ".cfg");
    	File saveLocationV2 = new File(Minecraft.getMinecraft().mcDataDir, "mods/" + ScathaPro.MODID);
    	File saveLocationV3 = getSaveLocation();

        if (saveLocationV1.exists() && !saveLocationV2.exists()) move(saveLocationV1, new File(saveLocationV2, "config.cfg"));
        if (saveLocationV2.exists() && !saveLocationV3.exists()) move(saveLocationV2, saveLocationV3);
    }
    
    private static void move(File source, File destination) {
    	System.out.println("Destination: " + destination);
		File directory = destination.getParentFile();
    	System.out.println("Destination dir: " + directory);
    	if (!directory.exists()) directory.mkdirs();
    	
    	source.renameTo(destination);
    }
    
}
