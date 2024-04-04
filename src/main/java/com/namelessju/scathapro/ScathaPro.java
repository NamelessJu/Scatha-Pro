package com.namelessju.scathapro;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.alerts.alertmodes.AlertModeManager;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.commands.ChancesCommand;
import com.namelessju.scathapro.commands.MainCommand;
import com.namelessju.scathapro.eventlisteners.GuiListeners;
import com.namelessju.scathapro.eventlisteners.LoopListeners;
import com.namelessju.scathapro.eventlisteners.MiscListeners;
import com.namelessju.scathapro.eventlisteners.ScathaProListeners;
import com.namelessju.scathapro.events.ModUpdateEvent;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.PersistentData;
import com.namelessju.scathapro.managers.UpdateChecker;
import com.namelessju.scathapro.miscellaneous.OverlayStats;
import com.namelessju.scathapro.miscellaneous.SkyblockArea;
import com.namelessju.scathapro.overlay.Overlay;
import com.namelessju.scathapro.managers.FileManager;
import com.namelessju.scathapro.managers.InputManager;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.commands.DevCommand;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod(modid = ScathaPro.MODID, version = ScathaPro.VERSION, name = ScathaPro.MODNAME, clientSideOnly = true)
public class ScathaPro
{
    public static final String MODNAME = "Scatha-Pro";
    public static final String MODID = "scathapro";
    public static final String VERSION = "1.3.dev.1";
    // public static final String VERSION = "1.3.pre-release.2";
    
    
    private static ScathaPro instance;
    
    public final GlobalVariables variables;

    private Logger logger;
    private Minecraft minecraft;
    
    private Config config;
    private PersistentData persistentData;
    private Overlay overlay;
    private AlertModeManager alertModeManager;
    private CustomAlertModeManager customAlertModeManager;
    private AchievementManager achievementManager;
    private InputManager inputManager;
    
    
    public static ScathaPro getInstance()
    {
        return instance;
    }
    
    public Minecraft getMinecraft() { return minecraft; }
    
    public Config getConfig() { return config; }
    public PersistentData getPersistentData() { return persistentData; }
    public Overlay getOverlay() { return overlay; }
    public AlertModeManager getAlertModeManager() { return alertModeManager; }
    public CustomAlertModeManager getCustomAlertModeManager() { return customAlertModeManager; }
    public AchievementManager getAchievementManager() { return achievementManager; }
    public InputManager getInputManager() { return inputManager; }
    
    
    public ScathaPro()
    {
        instance = this;
        
        variables = new GlobalVariables();
        
        logger = LogManager.getLogger(MODID);
        minecraft = Minecraft.getMinecraft();
        
        
        FileManager.updateOldSaveLocations();
        
        config = new Config();
        config.init();
        
        
        achievementManager = new AchievementManager(this);
        overlay = new Overlay(this);
        alertModeManager = new AlertModeManager(config);
        customAlertModeManager = new CustomAlertModeManager(this);
        inputManager = new InputManager(this);
        
        
        persistentData = new PersistentData(this);
        persistentData.loadData();
        
        
        String lastUsedVersion = JsonUtil.getString(persistentData.getData(), "global/lastUsedVersion");
        if (lastUsedVersion != null && UpdateChecker.compareVersions(lastUsedVersion, ScathaPro.VERSION) != 0)
        {
            MinecraftForge.EVENT_BUS.post(new ModUpdateEvent(lastUsedVersion, ScathaPro.VERSION));
            
            JsonUtil.set(persistentData.getData(), "global/lastUsedVersion", new JsonPrimitive(ScathaPro.VERSION));
            persistentData.saveData();
        }
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new LoopListeners(this));
        MinecraftForge.EVENT_BUS.register(new GuiListeners(this));
        MinecraftForge.EVENT_BUS.register(new MiscListeners(this));
        MinecraftForge.EVENT_BUS.register(new ScathaProListeners(this));
        
        ClientCommandHandler.instance.registerCommand(new MainCommand(this));
        ClientCommandHandler.instance.registerCommand(new ChancesCommand());
        ClientCommandHandler.instance.registerCommand(new DevCommand(this));
        
        inputManager.register();
        
        try
        {
            // I'm not a fan of using reflection but this is the best way to make sure this custom resource pack always gets loaded
            // TODO: try to replace this with mixins?
            List<IResourcePack> defaultResourcePacks = ReflectionHelper.getPrivateValue(Minecraft.class, minecraft, "defaultResourcePacks", "field_110449_ao");
            defaultResourcePacks.add(customAlertModeManager.resourcePack);
            
            customAlertModeManager.resourcePackInjected = true;
            log("Custom alert mode resource pack injected as a default resource pack");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            logError("Custom alert mode resource pack injection failed");
        }
    }
    
    
    public void log(String message)
    {
        log(Level.INFO, message);
    }
    
    public void logWarning(String message)
    {
        log(Level.WARN, message);
    }
    
    public void logError(String message)
    {
        log(Level.ERROR, message);
    }
    
    public void logDebug(String message)
    {
        if (config.getBoolean(Config.Key.debugLogs)) log(Level.INFO, "(DEBUG) " + message);
    }
    
    public void log(Level level, String message)
    {
        logger.log(level, "[" + MODNAME + "] " + message);
    }
    
    
    public boolean isInCrystalHollows()
    {
        return variables.currentArea == SkyblockArea.CRYSTAL_HOLLOWS;
    }
    
    
    // These should probably be moved into another class...
    
    public void updateKillAchievements()
    {
        int lobbyWormKills = OverlayStats.PER_LOBBY.regularWormKills + OverlayStats.PER_LOBBY.scathaKills;
        Achievement.lobby_kills_1.setProgress(lobbyWormKills);
        Achievement.lobby_kills_2.setProgress(lobbyWormKills);
        Achievement.lobby_kills_3.setProgress(lobbyWormKills);
        
        int highestWormKills = Math.max(lobbyWormKills, variables.regularWormKills + variables.scathaKills);
        Achievement.worm_bestiary_max.setProgress(highestWormKills);
        Achievement.worm_kills_1.setProgress(highestWormKills);
        Achievement.worm_kills_2.setProgress(highestWormKills);
        Achievement.worm_kills_3.setProgress(highestWormKills);
        Achievement.worm_kills_4.setProgress(highestWormKills);
        Achievement.worm_kills_5.setProgress(highestWormKills);
        Achievement.worm_kills_6.setProgress(highestWormKills);
        
        int highestScathaKills = Math.max(OverlayStats.PER_LOBBY.regularWormKills, variables.scathaKills);
        Achievement.scatha_kills_1.setProgress(highestScathaKills);
        Achievement.scatha_kills_2.setProgress(highestScathaKills);
        Achievement.scatha_kills_3.setProgress(highestScathaKills);
        Achievement.scatha_kills_4.setProgress(highestScathaKills);
        Achievement.scatha_kills_5.setProgress(highestScathaKills);
    }
    
    public void updateSpawnAchievements()
    {
        int scathaStreak = Math.max(0, OverlayStats.PER_LOBBY.scathaSpawnStreak);
        Achievement.scatha_streak_1.setProgress(scathaStreak);
        Achievement.scatha_streak_2.setProgress(scathaStreak);
        Achievement.scatha_streak_3.setProgress(scathaStreak);
        Achievement.scatha_streak_4.setProgress(scathaStreak);
        
        int regularWormStreak = Math.max(0, -OverlayStats.PER_LOBBY.scathaSpawnStreak);
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
        Achievement.scatha_pet_drop_any_3.setProgress(totalPetDrops);
        Achievement.scatha_pet_drop_any_4.setProgress(totalPetDrops);
    }
    
    public void updateProgressAchievements()
    {
        int achievementsCount = 0;
        int unlockedAchievementsCount = 0;
        
        Achievement[] achievements = AchievementManager.getAllAchievements();
        
        for (int i = 0; i < achievements.length; i ++)
        {
            Achievement a = achievements[i];
            if (a.type.visibility == Achievement.Type.Visibility.VISIBLE)
            {
                achievementsCount ++;
                if (achievementManager.isAchievementUnlocked(a)) unlockedAchievementsCount ++;
            }
        }
        
        float unlockedAchievementsPercentage = (float) unlockedAchievementsCount / achievementsCount;
        if (unlockedAchievementsPercentage >= 1f) Achievement.achievements_unlocked_all.setProgress(Achievement.achievements_unlocked_all.goal);
        else if (unlockedAchievementsPercentage >= 0.5f) Achievement.achievements_unlocked_half.setProgress(Achievement.achievements_unlocked_half.goal);
    }
    
}
