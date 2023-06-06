package com.namelessju.scathapro;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Loader;

public abstract class FileManager {
	
	private static boolean useAlternativeSaveLocation = false;
	
    public static File getModFile(String relativePath) {

    	System.out.println("Main path: " + Loader.instance().getConfigDir().getAbsolutePath());
    	System.out.println("Alt path: " + Minecraft.getMinecraft().mcDataDir + "mods/");
    	
        File modFolder = new File(getSaveLocation(), ScathaPro.MODID + "/");
        if (!modFolder.exists()) modFolder.mkdirs();
        
        return new File(modFolder, relativePath);
    }
    
    public static String getSaveLocation() {
    	return useAlternativeSaveLocation
    			? Loader.instance().getConfigDir().getAbsolutePath()
				: Minecraft.getMinecraft().mcDataDir + "mods/";
    }
    
}
