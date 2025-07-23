package namelessju.scathapro.eventlisteners;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.events.TickEvent.CrystalHollowsTickEvent;
import namelessju.scathapro.events.TickEvent.FirstCrystalHollowsTickEvent;
import namelessju.scathapro.events.TickEvent.FirstIngameTickEvent;
import namelessju.scathapro.events.TickEvent.FirstWorldTickEvent;
import namelessju.scathapro.managers.Config;
import namelessju.scathapro.managers.UpdateChecker;
import namelessju.scathapro.parsing.ScoreboardParser;
import namelessju.scathapro.util.TextUtil;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ScathaProTickListeners extends ScathaProListener
{
    private int heatCheckTickTimer = 0; 
    
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
            TextUtil.sendChatMessage(message);
        }
        scathaPro.variables.cachedChatMessages.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onFirstCrystalHollowsTick(FirstCrystalHollowsTickEvent event)
    {
        heatCheckTickTimer = 0;
        
        if (scathaPro.variables.regularWormKills == 0 && scathaPro.variables.scathaKills == 0 && scathaPro.getConfig().getBoolean(Config.Key.automaticWormStatsParsing))
        {
            TextUtil.sendModChatMessage(EnumChatFormatting.YELLOW + "Open the worm bestiary once to load previous worm kills into the overlay!");
        }

        for (IChatComponent message : scathaPro.variables.cachedCrystalHollowsMessages)
        {
            TextUtil.sendChatMessage(message);
        }
        scathaPro.variables.cachedCrystalHollowsMessages.clear();
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
        
        
        // Achievements
        
        float hours = (event.now - scathaPro.variables.lastWorldJoinTime) / (1000f*60*60);
        Achievement.crystal_hollows_time_1.setProgress(hours);
        Achievement.crystal_hollows_time_2.setProgress(hours);
        Achievement.crystal_hollows_time_3.setProgress(hours);
        
        
        // Heat check
        
        heatCheckTickTimer ++;
        if (heatCheckTickTimer > 3*20)
        {
            heatCheckTickTimer = 0;
            
            if (scathaPro.getConfig().getBoolean(Config.Key.highHeatAlert))
            {
                int newHeat = ScoreboardParser.parseHeat();
                
                if (newHeat > 0)
                {
                    int triggerValue = scathaPro.getConfig().getInt(Config.Key.highHeatAlertTriggerValue);
                    if (newHeat >= triggerValue && scathaPro.variables.lastHeat >= 0 && scathaPro.variables.lastHeat < triggerValue)
                    {
                        Alert.highHeat.play();
                    }
                }
                
                scathaPro.variables.lastHeat = newHeat;
            }
            else scathaPro.variables.lastHeat = -1;
        }
    }
}
