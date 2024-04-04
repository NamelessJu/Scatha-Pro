package com.namelessju.scathapro;

import java.util.HashMap;

import com.namelessju.scathapro.miscellaneous.OverlayStats;
import com.namelessju.scathapro.miscellaneous.SkyblockArea;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;

public class GlobalVariables
{
    public long lastWorldJoinTime = -1;
    public int currentAreaCheckTimeIndex = 0;
    public SkyblockArea currentArea = null;
    
    public HashMap<Integer, Integer> previousScathaPets = null;
    public ItemStack lastProjectileWeaponUsed = null;
    
    public int regularWormKills = 0;
    public int scathaKills = 0;

    public long wormSpawnCooldownStartTime = -1;
    
    public long lastWormSpawnTime = -1;
    public long lastScathaKillTime = -1;
    
    public int rarePetDrops = 0;
    public int epicPetDrops = 0;
    public int legendaryPetDrops = 0;
    
    public int scathaKillsAtLastDrop = -1;
    
    public GuiScreen openGuiNextTick = null;
    public boolean cheaterDetected = false;
    
    
    public void startWormSpawnCooldown(boolean forceRestart)
    {
        if (!forceRestart && wormSpawnCooldownStartTime >= Constants.pingTreshold) return;
        wormSpawnCooldownStartTime = Util.getCurrentTime();
    }
    
    public void addRegularWormKill()
    {
        if (regularWormKills >= 0) regularWormKills ++;
        OverlayStats.addRegularWormKill();
    }
    
    public void addScathaKill()
    {
        if (scathaKills >= 0) scathaKills ++;
        OverlayStats.addScathaKill();
    }
    
    public void resetForNewLobby()
    {
        currentAreaCheckTimeIndex = 0;
        currentArea = null;
        
        previousScathaPets = null;
        
        OverlayStats.resetForNewLobby();
        lastWormSpawnTime = -1;
        wormSpawnCooldownStartTime = -1;
    }
}
