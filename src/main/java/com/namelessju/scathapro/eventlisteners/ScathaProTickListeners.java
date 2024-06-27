package com.namelessju.scathapro.eventlisteners;

import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.events.TickEvent.CrystalHollowsTickEvent;
import com.namelessju.scathapro.events.TickEvent.FirstCrystalHollowsTickEvent;
import com.namelessju.scathapro.events.TickEvent.FirstIngameTickEvent;
import com.namelessju.scathapro.events.TickEvent.FirstWorldTickEvent;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.UpdateChecker;
import com.namelessju.scathapro.util.MessageUtil;

import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ScathaProTickListeners extends ScathaProListener
{
    public ScathaProTickListeners(ScathaPro scathaPro)
    {
        super(scathaPro);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFirstIngameTick(FirstIngameTickEvent event)
    {
        if (scathaPro.getConfig().getBoolean(Config.Key.automaticUpdateChecks))
        {
            UpdateChecker.checkForUpdate(false);
        }

        scathaPro.getAchievementLogicManager().updatePetDropAchievements();
        scathaPro.getAchievementLogicManager().updateProgressAchievements();
        scathaPro.getAchievementLogicManager().updateDailyScathaStreakAchievements();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFirstWorldTick(FirstWorldTickEvent event)
    {
        for (IChatComponent message : scathaPro.variables.cachedChatMessages)
        {
            MessageUtil.sendChatMessage(message);
        }
        scathaPro.variables.cachedChatMessages.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFirstCrystalHollowsTick(FirstCrystalHollowsTickEvent event)
    {
        if (scathaPro.getOverlay().easterEggTitleActive)
        {
            Achievement.easter_egg_overlay_title.unlock();
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onCrystalHollowsTick(CrystalHollowsTickEvent event)
    {
        // Sneak start
        
        boolean sneaking = mc.thePlayer.isSneaking();
        if (!scathaPro.variables.sneakingBefore && sneaking)
        {
            scathaPro.variables.lastSneakStartTime = event.now;
        }
        scathaPro.variables.sneakingBefore = sneaking;
        
        
        // Reset b2b scatha pet drop
        
        if (scathaPro.variables.droppedPetAtLastScatha && scathaPro.variables.lastKillIsScatha && event.now - scathaPro.variables.lastKillTime > Constants.pingTreshold && scathaPro.variables.lastPetDropTime < scathaPro.variables.lastKillTime)
        {
            scathaPro.variables.droppedPetAtLastScatha = false;
        }
        
        
        // Achievements
        
        float hours = (event.now - scathaPro.variables.lastWorldJoinTime) / (1000f*60*60);
        Achievement.crystal_hollows_time_1.setProgress(hours);
        Achievement.crystal_hollows_time_2.setProgress(hours);
        Achievement.crystal_hollows_time_3.setProgress(hours);
    }
}
