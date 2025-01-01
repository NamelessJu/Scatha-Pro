package com.namelessju.scathapro;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.miscellaneous.enums.SkyblockArea;
import com.namelessju.scathapro.miscellaneous.enums.WormStatsType;
import com.namelessju.scathapro.util.TimeUtil;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

public class GlobalVariables
{
    public Runnable runnableNextTick = null;
    public GuiScreen openGuiNextTick = null;
    
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
    public int scathaFarmingStreakHighscore = 0;
    public LocalDate lastScathaFarmedDate = null;
    
    public long lastKillTime = -1;
    public long lastPetDropTime = -1;
    
    public boolean sneakingBefore = false;
    public long lastSneakStartTime = -1;
    
    /** Used for the high heat alert and doesn't get updated if the alert is disabled! */
    public int lastHeat = -1;
    
    public long anomalousDesireReadyTime = -1;
    public long anomalousDesireCooldownEndTime = -1;
    public long anomalousDesireStartTime = -1;
    public boolean anomalousDesireWastedForRecovery = false;
    
    public boolean firstWorldTickPending = true;
    public boolean firstCrystalHollowsTickPending = true;
    public boolean cheaterDetected = false;

    public List<IChatComponent> cachedChatMessages = Lists.newArrayList();
    public List<IChatComponent> cachedCrystalHollowsMessages = Lists.newArrayList();
    public boolean lastChatMessageIsDivider = false;
    
    public boolean scappaModeActiveTemp = false;
    public boolean scappaModeUnlocked = false;
    
    public float avgMoneyCalcScathaPriceRare = -1f;
    public float avgMoneyCalcScathaPriceEpic = -1f;
    public float avgMoneyCalcScathaPriceLegendary = -1f;
    public float avgMoneyCalcMagicFind = -1f;
    public float avgMoneyCalcPetLuck = -1f;
    public float avgMoneyCalcScathaRate = -1f;
    
    
    public void startWormSpawnCooldown(boolean forceRestart)
    {
        if (!forceRestart && wormSpawnCooldownStartTime >= Constants.pingTreshold) return;
        wormSpawnCooldownStartTime = TimeUtil.now();
    }
    
    public void addRegularWormKill()
    {
        if (regularWormKills >= 0) regularWormKills ++;
        WormStatsType.addRegularWormKill();
    }
    
    public void addScathaKill()
    {
        if (scathaKills >= 0) scathaKills ++;
        WormStatsType.addScathaKill();
    }
    
    public void resetForNewLobby()
    {
        firstWorldTickPending = true;
        firstCrystalHollowsTickPending = true;
        currentAreaCheckTimeIndex = 0;
        currentArea = null;
        previousScathaPets = null;
        WormStatsType.resetForNewLobby();
        lastWormSpawnTime = -1;
        wormSpawnCooldownStartTime = -1;
        lastHeat = -1;
        sneakingBefore = false;
        anomalousDesireWastedForRecovery = false;
    }
}
