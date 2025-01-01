package com.namelessju.scathapro.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Session;

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

    
    public static String getUUIDString(UUID uuid)
    {
        if (uuid != null) return uuid.toString().replace("-", "").toLowerCase();
        return null;
    }
    
    public static UUID getPlayerUUID()
    {
        Session session = Minecraft.getMinecraft().getSession();
        
        if ("true".equalsIgnoreCase(System.getProperty("scathapro.offlineUuid"))) // JVM argument: -Dscathapro.offlineUuid=true
        {
            return Util.getOfflinePlayerUUID(session.getUsername());
        }
        
        try
        {
            return session.getProfile().getId();
        }
        catch (NullPointerException e)
        {
        	e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a UUID from a player name the same way Minecraft does it when the session is offline
     */
    public static UUID getOfflinePlayerUUID(String username)
    {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
    }

    public static String getPlayerUUIDString()
    {
        return getUUIDString(getPlayerUUID());
    }

    public static String numberToString(int number)
    {
        return numberToString(number, 0);
    }
    public static String numberToString(double number, int maxDecimalPlaces)
    {
        return numberToString(number, maxDecimalPlaces, false);
    }
    public static String numberToString(double number, int maxDecimalPlaces, boolean showTrailingDecimalZeros)
    {
        return numberToString(number, maxDecimalPlaces, showTrailingDecimalZeros, RoundingMode.HALF_EVEN);
    }
    public static String numberToString(double number, int maxDecimalPlaces, boolean showTrailingDecimalZeros, RoundingMode roundingMode)
    {
        DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols();
        decimalSymbols.setDecimalSeparator('.');
        decimalSymbols.setGroupingSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("#,###.#", decimalSymbols);
        decimalFormat.setMaximumFractionDigits(maxDecimalPlaces);
        decimalFormat.setRoundingMode(roundingMode);
        if (showTrailingDecimalZeros) decimalFormat.setMinimumFractionDigits(maxDecimalPlaces);
        return decimalFormat.format(number);
    }
    
    /**
     * Returns a zero-size AABB at the position of a given entity
     * @param entity
     * @return
     */
    public static AxisAlignedBB getEntityPositionAABB(Entity entity)
    {
        return new AxisAlignedBB(entity.posX, entity.posY, entity.posZ, entity.posX, entity.posY, entity.posZ);
    }
    
    /**
     * The default Entity.getBlockPos() method is fucked up and changes in the middle of a block, hence this method to receive an actual block position
     */
    public static BlockPos entityBlockPos(Entity entity)
    {
        return new BlockPos(
            (int) Math.floor(entity.posX),
            (int) Math.floor(entity.posY),
            (int) Math.floor(entity.posZ)
        );
    }
    
    /**
     * <p>Returns the direction that the player is looking towards:</p>
     * 
     * 0: -Z<br> 
     * 1: +X<br> 
     * 2: +Z<br> 
     * 3: -X
     */
    public static int getDirection(EntityPlayer player)
    {
        int direction = (int) Math.floor(player.rotationYaw / 90 - 1.5f) % 4;
        if (direction < 0) direction += 4;
        return direction;
    }
    
    public static boolean isPlayerListOpened()
    {
        ScoreObjective scoreobjective = Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(0);
        NetHandlerPlayClient handler = Minecraft.getMinecraft().thePlayer.sendQueue;
        return Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown() && (!Minecraft.getMinecraft().isIntegratedServerRunning() || handler.getPlayerInfoMap().size() > 1 || scoreobjective != null);
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
    
    /**
     * @param integer A nullable <code>Integer</code> object
     * @return 0 if <code>integer</code> is <code>null</code>, otherwise returns <code>integer</code>
     */
    public static int intOrZero(Integer integer)
    {
        return integer == null ? 0 : integer;
    }
    
    
    private Util() {}
}
