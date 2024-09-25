package com.namelessju.scathapro.managers;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.util.FileUtil;
import com.namelessju.scathapro.util.TextUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.Loader;

public class SaveManager
{
    public static final File saveDirectory = new File(Loader.instance().getConfigDir(), ScathaPro.MODID);
    public static final File backupDirectory = new File(Loader.instance().getConfigDir(), ScathaPro.MODID + "_backups");
    
    
    public static void updateOldSaveLocations()
    {
        // Save location
        
        File saveLocationV1 = new File(Loader.instance().getConfigDir(), "scathapro.cfg");
        File saveLocationV2 = new File(Minecraft.getMinecraft().mcDataDir, "mods/scathapro");
        File saveLocationV3 = saveDirectory;
        
        if (saveLocationV1.exists() && !saveLocationV2.exists()) FileUtil.move(saveLocationV1, new File(saveLocationV2, "config.cfg"));
        if (saveLocationV2.exists() && !saveLocationV3.exists()) FileUtil.move(saveLocationV2, saveLocationV3);
        
        // Backup location

        File backupLocationV1 = new File(saveDirectory, "backups");
        File backupLocationV2 = backupDirectory;
        
        if (backupLocationV1.exists())
        {
            backupLocationV2.mkdirs();
            for (File file : backupLocationV1.listFiles())
            {
                FileUtil.moveToDirectory(file, backupLocationV2);
            }
            FileUtil.deleteDirectoryRecursive(backupLocationV1);
        }
    }
    
    public static File getSaveFile(String relativePath)
    {
        File saveLocation = SaveManager.saveDirectory;
        if (!saveLocation.exists()) saveLocation.mkdirs();
        
        return new File(saveLocation, relativePath);
    }
    
    // BACKUPS
    
    public static void backup()
    {
        backup(null);
    }
    
    public static void backup(String name)
    {
        File backupFile = prepareBackupFile(name, "zip");
        boolean success = FileUtil.zip(saveDirectory, backupFile.getAbsolutePath(), true);
        sendBackupMessage(success, backupFile);
    }

    public static void backupPersistentData()
    {
        backupPersistentData(null);
    }
    
    public static void backupPersistentData(String name)
    {
        File backupFile = prepareBackupFile("persistentData"  + (name != null ? "_" + name : ""), "json");
        boolean success = FileUtil.copy(PersistentData.file, backupFile);
        sendBackupMessage(success, backupFile);
    }
    
    private static File prepareBackupFile(String name, String extension)
    {
        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd_HH-mm-ss");
        
        backupDirectory.mkdirs();
        
        return FileUtil.getNonexistentFile(backupDirectory, date.format(formatter) + (name != null ? "_" + name : "") + "." + extension);
    }
    
    private static void sendBackupMessage(boolean success, File backupFile)
    {
        if (success)
        {
            ChatComponentText message = new ChatComponentText("Created backup as ");
            
            ChatComponentText path = new ChatComponentText(EnumChatFormatting.UNDERLINE + backupFile.getName());
            ChatStyle pathStyle = new ChatStyle()
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GRAY + "Open backup folder")))
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, backupDirectory.getAbsolutePath()));
            path.setChatStyle(pathStyle);
            message.appendSibling(path);
            
            TextUtil.sendModChatMessage(message);
        }
        else
        {
            ChatComponentText message = new ChatComponentText(EnumChatFormatting.RED + "Couldn't create backup: Failed to create/write the ");
            
            ChatComponentText path = new ChatComponentText(EnumChatFormatting.RED + "file");
            ChatStyle pathStyle = new ChatStyle()
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.RED + "Tried to save as " + backupFile.getName() + "\n" + EnumChatFormatting.GRAY + "(Click to open backup folder)")))
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, backupDirectory.getAbsolutePath()));
            path.setChatStyle(pathStyle);
            message.appendSibling(path);
            
            TextUtil.sendModChatMessage(message);
        }
    }
    
    
    private SaveManager() {}
}
