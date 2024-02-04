package com.namelessju.scathapro;

import java.util.HashMap;

import com.namelessju.scathapro.miscellaneous.SkyblockArea;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

public class GlobalVariables
{
    public long lastWorldJoinTime = -1;
    public int currentAreaCheckTimeIndex = 0;
    public SkyblockArea currentArea = null;
    
    public boolean inBedrockWallRange = false;
    public HashMap<Integer, Integer> previousScathaPets = null;
    public ItemStack lastProjectileWeaponUsed = null;
    
    public int regularWormKills = 0;
    public int scathaKills = 0;
    public int lobbyRegularWormKills = 0;
    public int lobbyScathaKills = 0;
    public int scathaSpawnStreak = 0; // positive -> Scatha streak / negative -> regular worm streak
    public long lastWormSpawnTime = -1;
    public long lastScathaKillTime = -1;
    
    public int rarePetDrops = 0;
    public int epicPetDrops = 0;
    public int legendaryPetDrops = 0;
    public int scathaKillsAtLastDrop = -1;
    
    public int hardstoneMined = 0;
    
    public GuiScreen openGuiNextTick = null;
    public boolean cheaterDetected = false;
}
