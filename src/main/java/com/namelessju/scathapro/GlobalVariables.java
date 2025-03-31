package com.namelessju.scathapro;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.managers.ReflectionManager;
import com.namelessju.scathapro.miscellaneous.enums.SkyblockArea;
import com.namelessju.scathapro.miscellaneous.enums.WormStatsType;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.TimeUtil;
import com.namelessju.scathapro.util.UnicodeSymbol;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class GlobalVariables
{
    public List<ChatLine> chatLines = null;
    
    public final List<Runnable> runNextTick = Lists.newArrayList();
    public final List<Runnable> runAfterNextRender = Lists.newArrayList();
    public GuiScreen openGuiNextTick = null;
    
    public long lastWorldJoinTime = -1;
    public int currentAreaCheckTimeIndex = 0;
    public SkyblockArea currentArea = null;
    
    public float magicFind = -1f;
    public float wormBestiaryMagicFind = -1f;
    public float petLuck = -1f;
    
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
    public boolean dropDryStreakInvalidated = false;
    
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
    
    /** The time when the ability should be used (after spawn cooldown!) */
    public long anomalousDesireReadyTime = -1;
    /** The time when the ability is actually available again */
    public long anomalousDesireCooldownEndTime = -1;
    public long anomalousDesireStartTime = -1;
    public boolean anomalousDesireWastedForRecovery = false;
    
    public boolean firstWorldTickPending = true;
    public boolean firstCrystalHollowsTickPending = true;

    public List<IChatComponent> cachedChatMessages = Lists.newArrayList();
    public List<IChatComponent> cachedCrystalHollowsMessages = Lists.newArrayList();
    
    public boolean scappaModeActiveTemp = false;
    public boolean scappaModeUnlocked = false;
    public boolean overlayIconGooglyEyesUnlocked = false;
    
    public float avgMoneyCalcScathaPriceRare = -1f;
    public float avgMoneyCalcScathaPriceEpic = -1f;
    public float avgMoneyCalcScathaPriceLegendary = -1f;
    public float avgMoneyCalcMagicFind = -1f;
    public float avgMoneyCalcPetLuck = -1f;
    public float avgMoneyCalcScathaRate = -1f;
    
    public boolean cheaterDetected = false;
    public short lastAprilFoolsJokeShownYear = -1;
    public byte aprilFoolsJokeRevealTickTimer = 0;
    
    
    public GlobalVariables()
    {
        this.runNextTick.add(() -> {
            ScathaPro.getInstance().logDebug("First tick, getting chat lines reference using reflection");
            GlobalVariables.this.chatLines = ReflectionManager.getChatLines();
        });
    }
    
    
    public String getMagicFindString()
    {
        return EnumChatFormatting.AQUA.toString() + UnicodeSymbol.magicFind + " " + TextUtil.getObfNrStr(magicFind, 2, false, RoundingMode.FLOOR) + EnumChatFormatting.RESET + EnumChatFormatting.AQUA;
    }
    
    public String getBestiaryMagicFindString()
    {
        return EnumChatFormatting.AQUA.toString() + UnicodeSymbol.magicFind + " " + TextUtil.getObfNrStr(wormBestiaryMagicFind, 2, false, RoundingMode.FLOOR) + EnumChatFormatting.RESET + EnumChatFormatting.AQUA;
    }
    
    public String getTotalMagicFindString()
    {
        float totalMagicFind = getTotalMagicFind();
        return EnumChatFormatting.AQUA.toString() + UnicodeSymbol.magicFind + " " + TextUtil.getObfNrStr(totalMagicFind, 2, false, RoundingMode.FLOOR) + EnumChatFormatting.RESET + EnumChatFormatting.AQUA;
    }
    
    public String getPetLuckString()
    {
        return EnumChatFormatting.LIGHT_PURPLE.toString() + UnicodeSymbol.petLuck + " " + (petLuck >= 0f ? TextUtil.numberToString(petLuck, 2, false, RoundingMode.FLOOR) : EnumChatFormatting.OBFUSCATED + "?" + EnumChatFormatting.RESET + EnumChatFormatting.LIGHT_PURPLE);
    }
    
    public String getEffectiveMagicFindString()
    {
        float totalMagicFind = getTotalMagicFind();
        return EnumChatFormatting.BLUE + (totalMagicFind >= 0f && petLuck >= 0f ? TextUtil.numberToString(totalMagicFind + petLuck, 2, false, RoundingMode.FLOOR) : EnumChatFormatting.OBFUSCATED + "?" + EnumChatFormatting.RESET + EnumChatFormatting.BLUE);
    }
    
    public float getTotalMagicFind()
    {
        float totalMagicFind = -1f;
        if (magicFind >= 0) totalMagicFind = magicFind;
        if (wormBestiaryMagicFind >= 0)
        {
            if (totalMagicFind >= 0) totalMagicFind += wormBestiaryMagicFind;
            else totalMagicFind = wormBestiaryMagicFind;
        }
        return totalMagicFind;
    }
    
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
