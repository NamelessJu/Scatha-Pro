package com.namelessju.scathapro.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.namelessju.scathapro.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;

public abstract class Util {
    
    public enum Color {
        DARK_RED(0xAA0000), RED(0xFF5555), GOLD(0xFFAA00), YELLOW(16777045), DARK_GREEN(0x00AA00), GREEN(0x55FF55), AQUA(0x55FFFF), DARK_AQUA(0x00AAAA), DARK_BLUE(0x0000AA), BLUE(0x5555FF), LIGHT_PURPLE(0xFF55FF), DARK_PURPLE(0xAA00AA), WHITE(0xFFFFFF), GRAY(0xAAAAAA), DARK_GRAY(0x555555), BLACK(0x000000);
        
        private final int value;
        
        Color(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
    
    public static String getUUIDString(UUID uuid) {
        if (uuid != null) return uuid.toString().replace("-", "").toLowerCase();
        return null;
    }

    public static String getPlayerUUIDString() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player != null) {
            GameProfile gameProfile = player.getGameProfile();
            if (gameProfile != null)
                return getUUIDString(gameProfile.getId());
        }
        return null;
    }
    
    public static boolean inCrystalHollows() {
        if (Config.instance.getBoolean(Config.Key.devMode)) return true;
        
        boolean inCrystalHollows = false;
        
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
        if (netHandler != null) {
            Collection<NetworkPlayerInfo> playerInfos = netHandler.getPlayerInfoMap();
            
            for (Iterator<NetworkPlayerInfo> iterator = playerInfos.iterator(); iterator.hasNext();) {
                NetworkPlayerInfo p = iterator.next();
                IChatComponent displayName = p.getDisplayName();
                
                if (displayName != null && displayName.getUnformattedText().contains("Area:") && displayName.getUnformattedText().contains("Crystal Hollows")) {
                    inCrystalHollows = true;
                    break;
                }
            }
        }
        
        return inCrystalHollows;
    }
    
    public static boolean isDeveloper(NetworkPlayerInfo playerInfo) {
        if (playerInfo != null) {
            UUID uuid = playerInfo.getGameProfile().getId();
            if (uuid != null)
                return getUUIDString(uuid).equals("e9be3984b09740c98fb4d8aaeb2b4838");
        }
        
        return false;
    }

    public static String numberToString(int number) {
        return numberToString(number, 0);
    }
    public static String numberToString(double number, int maxDecimalPlaces) {
        DecimalFormatSymbols decimalSymbols = new DecimalFormatSymbols();
        decimalSymbols.setDecimalSeparator('.');
        decimalSymbols.setGroupingSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("#,###.#", decimalSymbols);
        decimalFormat.setMaximumFractionDigits(maxDecimalPlaces);
        return decimalFormat.format(number);
    }
    
    public static String getUnicodeString(String hexValue) {
        return Character.toString((char) Integer.parseInt(hexValue, 16));
    }
    
    public static BlockPos entityBlockPos(Entity entity) {
        return new BlockPos(
            (int) Math.floor(entity.posX),
            (int) Math.floor(entity.posY),
            (int) Math.floor(entity.posZ)
        );
    }
    
    public static int getFacing(EntityPlayer player) {
        int facing = (int) Math.floor(player.rotationYaw / 90 - 1.5f) % 4;
        if (facing < 0) facing += 4;
        return facing;
    }
    
    public static void copyToClipboard(String str) {
        StringSelection selection = new StringSelection(str);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
    
    public static float calculatePetChance(float initialChance, int magicFind, int petLuck, int looting) {
        
        float lootingMultiplier = 1f;
        switch (looting) {
            case 1:
                lootingMultiplier = 1.15f;
                break;
            case 2:
                lootingMultiplier = 1.3f;
                break;
            case 3:
                lootingMultiplier = 1.45f;
                break;
            case 4:
                lootingMultiplier = 1.6f;
                break;
            case 5:
                lootingMultiplier = 1.75f;
                break;
        }
        
        return initialChance * (1 + (magicFind + petLuck)/100f) * lootingMultiplier;
    }
}
