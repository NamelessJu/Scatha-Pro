package namelessju.scathapro;

import com.mojang.brigadier.CommandDispatcher;
import namelessju.scathapro.achievements.AchievementLogicManager;
import namelessju.scathapro.achievements.AchievementManager;
import namelessju.scathapro.alerts.AlertManager;
import namelessju.scathapro.alerts.alertmodes.AlertModeManager;
import namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertModeManager;
import namelessju.scathapro.apis.HypixelModApiImplementation;
import namelessju.scathapro.commands.AverageMoneyCommand;
import namelessju.scathapro.commands.DevCommand;
import namelessju.scathapro.commands.MainCommand;
import namelessju.scathapro.commands.ScathaChancesCommand;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.events.listeners.MinecraftLogicListeners;
import namelessju.scathapro.events.listeners.ScathaProGameplayListeners;
import namelessju.scathapro.events.listeners.ScathaProMiscListeners;
import namelessju.scathapro.events.listeners.ScathaProTickListeners;
import namelessju.scathapro.files.*;
import namelessju.scathapro.files.customalertmode.CustomAlertModeMetaUpdater;
import namelessju.scathapro.files.customalertmode.CustomAlertModePropertiesUpdater;
import namelessju.scathapro.files.legacy.LegacyConfig;
import namelessju.scathapro.files.legacy.LegacyPersistentData;
import namelessju.scathapro.gui.overlay.AlertTitleOverlay;
import namelessju.scathapro.gui.overlay.CrosshairOverlay;
import namelessju.scathapro.gui.overlay.MainOverlay;
import namelessju.scathapro.managers.*;
import namelessju.scathapro.miscellaneous.ItemPopupRenderer;
import namelessju.scathapro.parsing.ChatParser;
import namelessju.scathapro.parsing.SoundParser;
import namelessju.scathapro.parsing.containerscreenparsing.ContainerScreenParsingManager;
import namelessju.scathapro.sounds.SoundManager;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;

public abstract class ScathaPro
{
    public static final String MOD_ID = "scathapro";
    public static final String MOD_VERSION = "2.2.1";

    /** The true mod name, not influenced by certain features */
    public static final String MOD_NAME = "Scatha-Pro";
    private static final String MOD_NAME_SCAPPA = "Scappa-Pro";
    private static final String MOD_NAME_APRIL_FOOLS = "Schata-Por";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    public static Identifier getIdentifier(String path)
    {
        return Identifier.fromNamespaceAndPath(ScathaPro.MOD_ID, path);
    }

    private static ScathaPro instance = null;
    public static ScathaPro getInstance()
    {
        return instance;
    }


    public final Minecraft minecraft = Minecraft.getInstance();

    // Files
    public final Config config = new Config(this);
    public final PersistentData persistentData = new PersistentData(this);

    // Managers
    public final BackupManager backupManager = new BackupManager(this);
    public final ChatManager chatManager = new ChatManager(this);
    public final SoundManager soundManager = new SoundManager(this);
    public final InputManager inputManager = new InputManager(this);
    public final CoreManager coreManager = new CoreManager(this);
    public final PersistentDataProfileManager persistentDataProfileManager = new PersistentDataProfileManager(this);
    public final SecondaryStatsManager secondaryStatsManager = new SecondaryStatsManager(persistentDataProfileManager);
    public final AlertManager alertManager = new AlertManager(this);
    public final AlertModeManager alertModeManager = new AlertModeManager(this);
    public final CustomAlertModeManager customAlertModeManager = new CustomAlertModeManager(this);
    public final AchievementManager achievementManager = new AchievementManager(this);
    public final AchievementLogicManager achievementLogicManager = new AchievementLogicManager(this);
    public final PlayerHeadRenderingReplacementManager playerHeadRenderingReplacementManager
        = new PlayerHeadRenderingReplacementManager(this);
    public final ScathaDropsSlotMachineManager scathaDropsSlotMachineManager = new ScathaDropsSlotMachineManager(this);
    public final FFmpegManager ffmpegManager = new FFmpegManager(this);

    // Parsers
    public final ContainerScreenParsingManager containerScreenParsingManager = new ContainerScreenParsingManager(this);
    public final ChatParser chatParser = new ChatParser(this);
    public final SoundParser soundParser = new SoundParser(this);

    // Commands
    public final MainCommand mainCommand = new MainCommand(this);
    public final DevCommand devCommand = new DevCommand(this);
    public final ScathaChancesCommand scathaChancesCommand = new ScathaChancesCommand(this);
    public final AverageMoneyCommand averageMoneyCommand = new AverageMoneyCommand(this);

    // Overlays
    public final MainOverlay mainOverlay = new MainOverlay(this);
    public final AlertTitleOverlay alertTitleOverlay = new AlertTitleOverlay(this);
    public final CrosshairOverlay crosshairOverlay = new CrosshairOverlay(this);

    // Rendering
    public final ItemPopupRenderer itemPopupRenderer = new ItemPopupRenderer(minecraft);


    private boolean isGameLoaded = false;

    private final Queue<Runnable> runNextTick = new LinkedList<>();


    public ScathaPro()
    {
        instance = this;
    }

    /**
     * Gets the mod name to display in most places - may be altered by some features<br>
     * @see #MOD_NAME
     */
    public String getModDisplayName()
    {
        if (TimeUtil.isAprilFools()) return MOD_NAME_APRIL_FOOLS;
        if (coreManager.isScappaModeActive()) return MOD_NAME_SCAPPA;
        return MOD_NAME;
    }

    /**
     * Called during client mod loading.<br>
     * Note that {@link net.minecraft.client.Minecraft} isn't fully initialized at this point.
     */
    protected final void onInitialization()
    {
        MinecraftLogicListeners.register();
        ScathaProTickListeners.register();
        ScathaProGameplayListeners.register();
        ScathaProMiscListeners.register();

        boolean legacyConfigLoaded = false;
        if (!config.getFile().exists())
        {
            LegacyConfig legacyConfig = new LegacyConfig(this);
            if (legacyConfig.getFile().exists())
            {
                legacyConfig.load();
                config.save();
                legacyConfigLoaded = true;
                LOGGER.info("Legacy config loaded and saved into new config");
            }
        }
        if (!legacyConfigLoaded)
        {
            config.load();
            LOGGER.info("Config loaded");
        }
        config.version.set(ConfigUpdater.TARGET_VERSION);
        // Save to initialize missing values & save version
        config.save();

        if (!persistentData.getFile().exists())
        {
            LegacyPersistentData legacyPersistentData = new LegacyPersistentData(this);
            if (legacyPersistentData.getFile().exists())
            {
                legacyPersistentData.load();
                LOGGER.info("Legacy persistent data loaded and converted into new persistent data file");
            }
        }
        persistentData.load();
        LOGGER.info("Persistent data loaded");

        PersistentDataUpdater.updateAfterLoading(persistentData);

        persistentDataProfileManager.init();

        checkLastUsedVersion();

        customAlertModeManager.init();
        for (String subModeId : customAlertModeManager.findAllSubModeIds())
        {
            new CustomAlertModeMetaUpdater(this, subModeId).load();
            new CustomAlertModePropertiesUpdater(this, subModeId).load();
        }

        HypixelModApiImplementation.init(this);

        LOGGER.info("Scatha-Pro initialized");
    }

    /**
     * Called after Minecraft has fully loaded.
     */
    public void onMinecraftLoaded()
    {
        chatManager.init();
        mainOverlay.init();
        alertTitleOverlay.init();
        crosshairOverlay.init();

        isGameLoaded = true;
        LOGGER.info("Scatha-Pro fully loaded");
    }

    protected final <T> void registerCommands(CommandDispatcher<T> dispatcher, CommandBuildContext context)
    {
        mainCommand.register(dispatcher, context);
        devCommand.register(dispatcher, context);
        scathaChancesCommand.register(dispatcher, context);
        averageMoneyCommand.register(dispatcher, context);
    }

    public void tick()
    {
        if (!isGameLoaded) return;

        while (!runNextTick.isEmpty())
        {
            runNextTick.poll().run();
        }

        inputManager.tick();
        coreManager.tick();
        containerScreenParsingManager.tick();
        achievementManager.tick();
        achievementLogicManager.tick();
        alertTitleOverlay.tick();
        mainOverlay.tick();
        itemPopupRenderer.tick();
        scathaDropsSlotMachineManager.tick();
    }

    public void runNextTick(Runnable runnable)
    {
        runNextTick.add(runnable);
    }

    public PersistentData.ProfileData getProfileData()
    {
        return persistentDataProfileManager.getCurrentProfileData();
    }

    private void checkLastUsedVersion()
    {
        String lastUsedVersion = persistentData.lastUsedModVersion.get();
        if (lastUsedVersion == null || UpdateChecker.compareVersions(lastUsedVersion, MOD_VERSION) != 0)
        {
            if (config.miscellaneous.automaticBackupsEnabled.get())
            {
                backupManager.backup("update_" + (lastUsedVersion != null ? lastUsedVersion : "unknown") + "_to_" + MOD_VERSION);
            }

            persistentData.lastUsedModVersion.set(MOD_VERSION);
            persistentData.save();

            LOGGER.info("New mod version detected!");

            ScathaProEvents.newModVersionUsedEvent.trigger(
                new ScathaProEvents.NewModVersionUsedEventData(this, lastUsedVersion, MOD_VERSION)
            );
        }
    }

    /** The base config directory */
    public abstract Path getConfigDirectoryPath();

    /** The Scatha-Pro save files directory */
    public Path getSaveDirectoryPath()
    {
        return getConfigDirectoryPath().resolve(ScathaPro.MOD_ID);
    }
}