package com.namelessju.scathapro;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.miscellaneous.WormStats;
import com.namelessju.scathapro.miscellaneous.SkyblockArea;
import com.namelessju.scathapro.util.TimeUtil;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

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

    public LocalDate lastPlayedDate = null;
    public int scathaFarmingStreak = 0;
    public LocalDate lastScathaFarmedDate = null;
    
    public long lastKillTime = -1;
    public long lastPetDropTime = -1;
    public boolean lastKillIsScatha = false;
    public boolean droppedPetAtLastScatha = false;
    
    public boolean sneakingBefore = false;
    public long lastSneakStartTime = -1;
    
    public GuiScreen openGuiNextTick = null;
    public boolean firstWorldTickPending = true;
    public boolean cheaterDetected = false;

    public List<IChatComponent> cachedChatMessages = Lists.newArrayList();
    public boolean lastChatMessageIsDivider = false;
    
    
    public void startWormSpawnCooldown(boolean forceRestart)
    {
        if (!forceRestart && wormSpawnCooldownStartTime >= Constants.pingTreshold) return;
        wormSpawnCooldownStartTime = TimeUtil.now();
    }
    
    public void addRegularWormKill()
    {
        if (regularWormKills >= 0) regularWormKills ++;
        WormStats.addRegularWormKill();
    }
    
    public void addScathaKill()
    {
        if (scathaKills >= 0) scathaKills ++;
        WormStats.addScathaKill();
    }
    
    public void resetForNewLobby()
    {
        firstWorldTickPending = true;
        
        currentAreaCheckTimeIndex = 0;
        currentArea = null;
        
        previousScathaPets = null;
        
        WormStats.resetForNewLobby();
        lastWormSpawnTime = -1;
        wormSpawnCooldownStartTime = -1;
    }
}
