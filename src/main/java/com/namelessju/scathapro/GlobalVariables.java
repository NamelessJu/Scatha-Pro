package com.namelessju.scathapro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public int sessionRegularWormKills = 0;
    public int sessionScathaKills = 0;
    public int lobbyRegularWormKills = 0;
    public int lobbyScathaKills = 0;
    public int scathaSpawnStreak = 0; // positive -> Scatha streak / negative -> regular worm streak
    public long lastWormSpawnTime = -1;
    public long wormSpawnCooldownStartTime = -1;
    public long lastScathaKillTime = -1;
    
    public int rarePetDrops = 0;
    public int epicPetDrops = 0;
    public int legendaryPetDrops = 0;
    public int scathaKillsAtLastDrop = -1;
    
    public int hardstoneMined = 0;
    
    public GuiScreen openGuiNextTick = null;
    public boolean cheaterDetected = false;

    public List<String> devsInLobby = new ArrayList<String>();
    
    
    public void startWormSpawnCooldown()
    {
        if (wormSpawnCooldownStartTime >= Constants.pingTreshold) return;
        wormSpawnCooldownStartTime = Util.getCurrentTime();
    }
    
    public void addRegularWormKill()
    {
        if (regularWormKills >= 0) regularWormKills ++;
        sessionRegularWormKills ++;
        lobbyRegularWormKills ++;
    }
    
    public void addScathaKill()
    {
        if (scathaKills >= 0) scathaKills ++;
        sessionScathaKills ++;
        lobbyScathaKills ++;
    }
    
    public void resetForNewLobby()
    {
        currentAreaCheckTimeIndex = 0;
        currentArea = null;
        devsInLobby.clear();
        
        previousScathaPets = null;
        
        lobbyRegularWormKills = 0;
        lobbyScathaKills = 0;
        scathaSpawnStreak = 0;
        lastWormSpawnTime = -1;
        wormSpawnCooldownStartTime = -1;
    }
}
