package com.namelessju.scathapro;

import java.util.UUID;

import net.minecraft.util.EnumChatFormatting;

public class Constants
{
    public static final String chatPrefix = EnumChatFormatting.GRAY + "[" + ScathaPro.MODNAME + "] " + EnumChatFormatting.RESET;
    public static final String chatPrefixShort = EnumChatFormatting.GRAY + "[SP] " + EnumChatFormatting.RESET;
    public static final String chatPrefixDev = EnumChatFormatting.DARK_GREEN + "[" + EnumChatFormatting.OBFUSCATED + "[" + EnumChatFormatting.GREEN + "Scatha_Dev" + EnumChatFormatting.DARK_GREEN + EnumChatFormatting.OBFUSCATED + "]" + EnumChatFormatting.DARK_GREEN + "]" + EnumChatFormatting.RESET + " ";
    public static final String msgHighlightingColor = EnumChatFormatting.YELLOW.toString();
    
    public static final int pingTreshold = 2000;
    
    public static final int[] postWorldJoinAreaCheckTimes = new int[] {200, 500, 1000, 2000, 3000, 5000, 10000};

    public static final int wormSpawnCooldown = 30000;
    public static final int wormLifetime = 30000;
    
    public static final float scathaPetBaseChanceRare = 0.0024f;
    public static final float scathaPetBaseChanceEpic = 0.0012f;
    public static final float scathaPetBaseChanceLegendary = 0.0004f;
    
    public static final int maxLegitPetDropsAmount = 9999;
    
    // These are the wall coordinates
    public static final int crystalHollowsBoundsMin = 201;
    public static final int crystalHollowsBoundsMax = 824;

    public static final int anomalousDesireEffectDuration = 30000;
    
    public static final UUID devUUID = UUID.fromString("e9be3984-b097-40c9-8fb4-d8aaeb2b4838");
    
    
    private Constants() {}
}
