package com.namelessju.scathapro;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.alerts.AlertModeManager;
import com.namelessju.scathapro.alerts.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.commands.ChancesCommand;
import com.namelessju.scathapro.commands.MainCommand;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedGoblin;
import com.namelessju.scathapro.eventlisteners.GuiListeners;
import com.namelessju.scathapro.eventlisteners.LoopListeners;
import com.namelessju.scathapro.eventlisteners.MiscListeners;
import com.namelessju.scathapro.eventlisteners.ScathaProListeners;
import com.namelessju.scathapro.events.GoblinSpawnEvent;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.OverlayManager;
import com.namelessju.scathapro.managers.PersistentData;
import com.namelessju.scathapro.managers.SaveManager;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.commands.DevCommand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = ScathaPro.MODID, version = ScathaPro.VERSION, name = ScathaPro.MODNAME, clientSideOnly = true)
public class ScathaPro
{
    public static final String MODNAME = "Scatha-Pro";
    public static final String MODID = "scathapro";
    public static final String VERSION = "1.3-DEV";
    
    public static final String CHATPREFIX = EnumChatFormatting.GRAY + MODNAME + ": " + EnumChatFormatting.RESET;
    
    
    private static ScathaPro instance;
    
    public final Variables variables;
    
    public final Minecraft minecraft;
    
    public final Logger logger;
    public final Config config;
    public final PersistentData persistentData;
    public final AchievementManager achievementManager;
    public final OverlayManager overlayManager;
    public final AlertModeManager alertModeManager;
    public final CustomAlertModeManager customAlertModeManager;
    
    
    public static ScathaPro getInstance()
    {
        return instance;
    }
    
    
    public ScathaPro()
    {
        instance = this;
        
        logger = LogManager.getLogger(MODID);
        minecraft = Minecraft.getMinecraft();
        
        variables = new Variables();

        config = new Config();
        SaveManager.updateOldSaveLocations();
        config.init();
        
        persistentData = new PersistentData(this);
        achievementManager = new AchievementManager(this);
        overlayManager = new OverlayManager(this);
        alertModeManager = new AlertModeManager(config);
        customAlertModeManager = new CustomAlertModeManager();
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new LoopListeners(this));
        MinecraftForge.EVENT_BUS.register(new GuiListeners(this));
        MinecraftForge.EVENT_BUS.register(new MiscListeners(this));
        MinecraftForge.EVENT_BUS.register(new ScathaProListeners(this));
        
        ClientCommandHandler.instance.registerCommand(new MainCommand(this));
        ClientCommandHandler.instance.registerCommand(new ChancesCommand());
        ClientCommandHandler.instance.registerCommand(new DevCommand(this));
        
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        if (resourceManager instanceof SimpleReloadableResourceManager)
        {
            SimpleReloadableResourceManager simpleReloadableResourceManager = (SimpleReloadableResourceManager) resourceManager;
            simpleReloadableResourceManager.registerReloadListener(customAlertModeManager);
        }
        else
        {
            logger.log(Level.ERROR, "Couldn't register resource reload listener - resource manager of unexpected type " + resourceManager.getClass().getCanonicalName() + " (expected " + SimpleReloadableResourceManager.class.getCanonicalName() + ")");
        }
    }
    
    
    public boolean devTrigger(String trigger, String[] arguments)
    {
        if (trigger.equalsIgnoreCase("goblin"))
        {
            if (arguments.length > 0)
            {
                try
                {
                    DetectedGoblin.Type type = DetectedGoblin.Type.valueOf(arguments[0]);
                    MinecraftForge.EVENT_BUS.post(new GoblinSpawnEvent(new DetectedGoblin(null, type)));
                    
                    MessageUtil.sendModChatMessage("Goblin spawn triggered");
                }
                catch (IllegalArgumentException e)
                {
                    MessageUtil.sendModErrorMessage("Invalid goblin type");
                }
            }
            else MessageUtil.sendModErrorMessage("Goblin type argument missing");
            
            return true;
        }
        
        return false;
    }
    
    
    // These should be moved into another class...
    
    public void updateKillAchievements()
    {
        int lobbyWormKills = variables.regularWormKills + variables.scathaKills;
        Achievement.lobby_kills_1.setProgress(lobbyWormKills);
        Achievement.lobby_kills_2.setProgress(lobbyWormKills);
        Achievement.lobby_kills_3.setProgress(lobbyWormKills);
        
        int highestRegularWormKills = Math.max(lobbyWormKills, variables.overallRegularWormKills + variables.overallScathaKills);
        Achievement.worm_kills_1.setProgress(highestRegularWormKills);
        Achievement.worm_kills_2.setProgress(highestRegularWormKills);
        Achievement.worm_kills_3.setProgress(highestRegularWormKills);
        Achievement.worm_kills_4.setProgress(highestRegularWormKills);
        Achievement.worm_kills_5.setProgress(highestRegularWormKills);
        Achievement.worm_kills_6.setProgress(highestRegularWormKills);
        
        int highestScathaKills = Math.max(variables.scathaKills, variables.overallScathaKills);
        Achievement.scatha_kills_1.setProgress(highestScathaKills);
        Achievement.scatha_kills_2.setProgress(highestScathaKills);
        Achievement.scatha_kills_3.setProgress(highestScathaKills);
        Achievement.scatha_kills_4.setProgress(highestScathaKills);
        Achievement.scatha_kills_5.setProgress(highestScathaKills);
        Achievement.scatha_kills_6.setProgress(highestScathaKills);
    }
    
    public void updateSpawnAchievements()
    {
        int scathaStreak = Math.max(0, variables.scathaStreak);
        Achievement.scatha_streak_1.setProgress(scathaStreak);
        Achievement.scatha_streak_2.setProgress(scathaStreak);
        Achievement.scatha_streak_3.setProgress(scathaStreak);
        Achievement.scatha_streak_4.setProgress(scathaStreak);
        
        int regularWormStreak = Math.max(0, -variables.scathaStreak);
        Achievement.regular_worm_streak_1.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_2.setProgress(regularWormStreak);
        Achievement.regular_worm_streak_3.setProgress(regularWormStreak);
    }
    
    public void updatePetDropAchievements()
    {
        Achievement.scatha_pet_drop_1_rare.setProgress(variables.rarePetDrops);
        Achievement.scatha_pet_drop_2_rare.setProgress(variables.rarePetDrops);
        Achievement.scatha_pet_drop_3_rare.setProgress(variables.rarePetDrops);
        
        Achievement.scatha_pet_drop_1_epic.setProgress(variables.epicPetDrops);
        Achievement.scatha_pet_drop_2_epic.setProgress(variables.epicPetDrops);
        Achievement.scatha_pet_drop_3_epic.setProgress(variables.epicPetDrops);
        
        Achievement.scatha_pet_drop_1_legendary.setProgress(variables.legendaryPetDrops);
        Achievement.scatha_pet_drop_2_legendary.setProgress(variables.legendaryPetDrops);
        Achievement.scatha_pet_drop_3_legendary.setProgress(variables.legendaryPetDrops);
        
        Achievement.scatha_pet_drop_each.setProgress(
                (variables.rarePetDrops > 0 ? 1 : 0)
                +
                (variables.epicPetDrops > 0 ? 1 : 0)
                +
                (variables.legendaryPetDrops > 0 ? 1 : 0)
        );
        
        int totalPetDrops = variables.rarePetDrops + variables.epicPetDrops + variables.legendaryPetDrops;
        Achievement.scatha_pet_drop_any_1.setProgress(totalPetDrops);
        Achievement.scatha_pet_drop_any_2.setProgress(totalPetDrops);
    }
    
    public void updateProgressAchievements()
    {
        int nonHiddenAchievements = 0;
        int unlockedNonHiddenAchievements = 0;
        
        Achievement[] achievements = AchievementManager.getAllAchievements();
        
        for (int i = 0; i < achievements.length; i ++) {
            Achievement a = achievements[i];
            if (a.type.visibility != Achievement.Type.Visibility.HIDDEN) {
                nonHiddenAchievements ++;
                if (achievementManager.isAchievementUnlocked(a)) unlockedNonHiddenAchievements ++;
            }
        }
        
        float unlockedNonHiddenAchievementsPercentage = (float) unlockedNonHiddenAchievements / nonHiddenAchievements;
        if (unlockedNonHiddenAchievementsPercentage >= 1f) Achievement.achievements_unlocked_all.setProgress(Achievement.achievements_unlocked_all.goal);
        else if (unlockedNonHiddenAchievementsPercentage >= 0.5f) Achievement.achievements_unlocked_half.setProgress(Achievement.achievements_unlocked_half.goal);
    }
    
}
