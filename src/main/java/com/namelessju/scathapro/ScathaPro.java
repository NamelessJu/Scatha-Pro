package com.namelessju.scathapro;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.achievements.AchievementManager;
import com.namelessju.scathapro.alerts.alertmodes.AlertModeManager;
import com.namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import com.namelessju.scathapro.commands.CommandRegistry;
import com.namelessju.scathapro.eventlisteners.GuiListeners;
import com.namelessju.scathapro.eventlisteners.LoopListeners;
import com.namelessju.scathapro.eventlisteners.MiscListeners;
import com.namelessju.scathapro.eventlisteners.ScathaProGameplayListeners;
import com.namelessju.scathapro.eventlisteners.ScathaProMiscListeners;
import com.namelessju.scathapro.eventlisteners.ScathaProTickListeners;
import com.namelessju.scathapro.events.ModUpdateEvent;
import com.namelessju.scathapro.managers.AchievementLogicManager;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.PersistentData;
import com.namelessju.scathapro.managers.SaveManager;
import com.namelessju.scathapro.managers.UpdateChecker;
import com.namelessju.scathapro.miscellaneous.enums.SkyblockArea;
import com.namelessju.scathapro.overlay.AlertTitleOverlay;
import com.namelessju.scathapro.overlay.Overlay;
import com.namelessju.scathapro.parsing.chestguiparsing.ChestGuiParsingManager;
import com.namelessju.scathapro.managers.InputManager;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.TimeUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod(modid = ScathaPro.MODID, version = ScathaPro.VERSION, name = ScathaPro.TRUE_MODNAME, clientSideOnly = true)
public class ScathaPro
{
    public static final String TRUE_MODNAME = "Scatha-Pro";
    public static final String MODID = "scathapro";
    public static final String VERSION = "1.3.1.1";
    
    public static final String DYNAMIC_MODNAME = TimeUtil.isAprilFools ? "Schata-Por" : TRUE_MODNAME;
    
    
    private static ScathaPro instance;
    
    public final GlobalVariables variables;
    
    private Logger logger;
    private Minecraft minecraft;
    
    private Config config;
    private PersistentData persistentData;
    private Overlay overlay;
    private AlertTitleOverlay alertTitleOverlay;
    private AlertModeManager alertModeManager;
    private CustomAlertModeManager customAlertModeManager;
    private AchievementManager achievementManager;
    private AchievementLogicManager achievementLogicManager;
    private InputManager inputManager;
    private ChestGuiParsingManager chestGuiParsingManager;
    
    public final CommandRegistry commandRegistry;
    
    
    public static ScathaPro getInstance()
    {
        return instance;
    }
    
    public Minecraft getMinecraft() { return minecraft; }
    
    public Config getConfig() { return config; }
    public PersistentData getPersistentData() { return persistentData; }
    public Overlay getOverlay() { return overlay; }
    public AlertTitleOverlay getAlertTitleOverlay() { return alertTitleOverlay; }
    public AlertModeManager getAlertModeManager() { return alertModeManager; }
    public CustomAlertModeManager getCustomAlertModeManager() { return customAlertModeManager; }
    public AchievementManager getAchievementManager() { return achievementManager; }
    public AchievementLogicManager getAchievementLogicManager() { return achievementLogicManager; }
    public InputManager getInputManager() { return inputManager; }
    public ChestGuiParsingManager getChestGuiParsingManager() { return chestGuiParsingManager; }
    
    
    public ScathaPro()
    {
        instance = this;
        
        variables = new GlobalVariables();
        
        logger = LogManager.getLogger(MODID);
        minecraft = Minecraft.getMinecraft();
        
        
        SaveManager.updateOldSaveLocations();
        
        config = new Config();
        config.init();
        
        achievementManager = new AchievementManager(this);
        achievementLogicManager = new AchievementLogicManager(this);
        overlay = new Overlay(this);
        alertTitleOverlay = new AlertTitleOverlay(config);
        alertModeManager = new AlertModeManager(config);
        customAlertModeManager = new CustomAlertModeManager(this);
        inputManager = new InputManager(this);
        chestGuiParsingManager = new ChestGuiParsingManager(this);
        
        
        persistentData = new PersistentData(this);
        persistentData.loadFile();
        
        
        commandRegistry = new CommandRegistry(this);
    }
    
    @EventHandler
    private void preInit(FMLPreInitializationEvent event)
    {
        if (TimeUtil.isAprilFools)
        {
            event.getModMetadata().name = DYNAMIC_MODNAME;
            event.getModMetadata().logoFile = "icon_aprilfools.png";
        }
    }
    
    @EventHandler
    private void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new LoopListeners(this));
        MinecraftForge.EVENT_BUS.register(new GuiListeners(this));
        MinecraftForge.EVENT_BUS.register(new MiscListeners(this));
        MinecraftForge.EVENT_BUS.register(new ScathaProGameplayListeners(this));
        MinecraftForge.EVENT_BUS.register(new ScathaProTickListeners(this));
        MinecraftForge.EVENT_BUS.register(new ScathaProMiscListeners(this));
        
        this.commandRegistry.registerCommands();
        
        inputManager.register();
        

        List<IResourcePack> defaultResourcePacks = ReflectionHelper.getPrivateValue(Minecraft.class, minecraft, "field_110449_ao", "defaultResourcePacks");
        if (defaultResourcePacks != null)
        {
            defaultResourcePacks.add(customAlertModeManager.resourcePack);
            log("Custom alert mode resource pack injected as a default resource pack");
        }
        else
        {
            logError("Custom alert mode resource pack injection failed");
            TextUtil.sendModErrorMessage("Failed to set up custom alert mode resource pack - vanilla sounds will play instead!");
        }
        
        
        boolean saveCurrentVersion = false;
        
        String lastUsedVersion = JsonUtil.getString(persistentData.getData(), "global/lastUsedVersion");
        if (lastUsedVersion == null) saveCurrentVersion = true;
        else if (UpdateChecker.compareVersions(lastUsedVersion, ScathaPro.VERSION) != 0)
        {
            logDebug("Mod update detected");
            MinecraftForge.EVENT_BUS.post(new ModUpdateEvent(lastUsedVersion, ScathaPro.VERSION));
            saveCurrentVersion = true;
        }
        
        if (saveCurrentVersion) 
        {
            JsonUtil.set(persistentData.getData(), "global/lastUsedVersion", new JsonPrimitive(ScathaPro.VERSION));
            persistentData.saveData();
        }
        
        
        overlay.updateOverlayFull();
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
        logger.log(level, "[" + TRUE_MODNAME + "] " + message);
    }
    
    
    public boolean isInCrystalHollows()
    {
        return variables.currentArea == SkyblockArea.CRYSTAL_HOLLOWS;
    }
    
    public boolean isScappaModeActive()
    {
        return variables.scappaModeUnlocked && (variables.scappaModeActiveTemp || getConfig().getBoolean(Config.Key.scappaMode));
    }
    
}
