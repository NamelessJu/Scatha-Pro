package com.namelessju.scathapro.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

public class Util
{
    public enum Color
    {
        DARK_RED(0xFFAA0000),
        RED(0xFFFF5555),
        GOLD(0xFFFFAA00),
        YELLOW(0xFFFFFF55),
        DARK_GREEN(0xFF00AA00),
        GREEN(0xFF55FF55),
        AQUA(0xFF55FFFF),
        DARK_AQUA(0xFF00AAAA),
        DARK_BLUE(0xFF0000AA),
        BLUE(0xFF5555FF),
        LIGHT_PURPLE(0xFFFF55FF),
        DARK_PURPLE(0xFFAA00AA),
        WHITE(0xFFFFFFFF),
        GRAY(0xFFAAAAAA),
        DARK_GRAY(0xFF555555),
        BLACK(0xFF000000);
        
        private final int value;
        
        Color(int value)
        {
            this.value = value;
        }
        
        public int getValue()
        {
            return value;
        }
    }

    
    public static long getCurrentTime()
    {
        return System.currentTimeMillis();
    }
    
    public static String getUUIDString(UUID uuid)
    {
        if (uuid != null) return uuid.toString().replace("-", "").toLowerCase();
        return null;
    }

    public static String getPlayerUUIDString()
    {
        return getUUIDString(Minecraft.getMinecraft().getSession().getProfile().getId());
    }
    
    public static boolean isDeveloper(GameProfile profile)
    {
        if (ScathaPro.getInstance().getConfig().getBoolean(Config.Key.devMode) && profile.getName().equals("JuCraft")) return true;
        
        UUID uuid = profile.getId();
        if (uuid != null) return getUUIDString(uuid).equals("e9be3984b09740c98fb4d8aaeb2b4838");
        
        return false;
    }

    public static String numberToString(int number)
    {
        return numberToString(number, 0, false);
    }
    
    public static String numberToString(double number, int maxDecimalPlaces)
    {
        return numberToString(number, maxDecimalPlaces, false);
    }
    
    public static String numberToString(double number, int maxDecimalPlaces, boolean showTrailingDecimalZeros)
    {
        DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols();
        decimalSymbols.setDecimalSeparator('.');
        decimalSymbols.setGroupingSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("#,###.#", decimalSymbols);
        decimalFormat.setMaximumFractionDigits(maxDecimalPlaces);
        if (showTrailingDecimalZeros) decimalFormat.setMinimumFractionDigits(maxDecimalPlaces);
        return decimalFormat.format(number);
    }
    
    public static String formatTime(long timestamp)
    {
        return new SimpleDateFormat().format(new Date(timestamp));
    }
    
    public static String getUnicodeString(String hexValue)
    {
        return Character.toString((char) Integer.parseInt(hexValue, 16));
    }
    
    public static BlockPos entityBlockPos(Entity entity)
    {
        return new BlockPos(
            (int) Math.floor(entity.posX),
            (int) Math.floor(entity.posY),
            (int) Math.floor(entity.posZ)
        );
    }
    
    public static int getFacing(EntityPlayer player)
    {
        int facing = (int) Math.floor(player.rotationYaw / 90 - 1.5f) % 4;
        if (facing < 0) facing += 4;
        return facing;
    }
    
    public static void copyToClipboard(String str)
    {
        StringSelection selection = new StringSelection(str);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
    
    public static float calculatePetChance(float initialChance, float magicFind, float petLuck, int looting)
    {
        return initialChance * (1f + (magicFind + petLuck)/100f) * (1 + looting * 0.15f);
    }
    
    public static boolean openFileInExplorer(File file)
    {
        if (!file.exists()) return false;
        
        try
        {
            Runtime.getRuntime().exec("explorer.exe " + (file.isFile() ? "/select," : "") + file.getAbsolutePath());
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return false;
    }
    
    
    private Util() {}
}
