package com.namelessju.scathapro;

import java.util.UUID;

import net.minecraft.util.EnumChatFormatting;

public class Constants
{
    public static final String chatPrefix = EnumChatFormatting.GRAY + "[" + ScathaPro.DYNAMIC_MODNAME + "] " + EnumChatFormatting.RESET;
    public static final String chatPrefixShort = EnumChatFormatting.GRAY + "[SP] " + EnumChatFormatting.RESET;
    public static final String chatPrefixDev = EnumChatFormatting.DARK_GREEN + "[" + EnumChatFormatting.OBFUSCATED + "[" + EnumChatFormatting.GREEN + "Scatha_Dev" + EnumChatFormatting.DARK_GREEN + EnumChatFormatting.OBFUSCATED + "]" + EnumChatFormatting.DARK_GREEN + "]" + EnumChatFormatting.RESET + " ";
    public static final String msgHighlightingColor = EnumChatFormatting.YELLOW.toString();
    
    public static final int pingTreshold = 2000;
    
    public static final int[] postWorldJoinAreaCheckTimes = new int[] {200, 500, 1000, 2000, 3000, 5000, 10000};
    /** The numbers of ticks after opening a chest GUI at which the parser tries to parse it's contents */
    public static final int[] chestGuiParserTickCounts = new int[] {4, 10, 20, 40, 60, 100};
    
    public static final int wormSpawnCooldown = 30000;
    public static final int wormLifetime = 30000;
    
    public static final float scathaPetBaseChanceRare = 0.0024f;
    public static final float scathaPetBaseChanceEpic = 0.0012f;
    public static final float scathaPetBaseChanceLegendary = 0.0004f;
    
    public static final int maxLegitPetDropsAmount = 9999;
    /** Dry streak gets invalidated if the mod's and the bestiary's Scatha kills differ more than this threshold */
    public static final int dryStreakMaxAllowedScathaKillsDeviation = 10;
    
    // These are the wall coordinates
    public static final int crystalHollowsBoundsMin = 201;
    public static final int crystalHollowsBoundsMax = 824;

    public static final int anomalousDesireEffectDuration = 30000;
    
    public static final UUID devUUID = UUID.fromString("e9be3984-b097-40c9-8fb4-d8aaeb2b4838");
    
    
    private Constants() {}
}
