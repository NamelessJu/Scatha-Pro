package namelessju.scathapro.gui.overlay;

import com.google.common.collect.Lists;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.files.Config;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.gui.menus.screens.settings.overlay.OverlaySettingsScreen;
import namelessju.scathapro.gui.overlay.elements.*;
import namelessju.scathapro.gui.overlay.elements.OverlayDynamicContainer.Direction;
import namelessju.scathapro.gui.overlay.elements.OverlayElement.Alignment;
import namelessju.scathapro.managers.SecondaryStatsManager;
import namelessju.scathapro.miscellaneous.data.OverlayIconEyePositions;
import namelessju.scathapro.miscellaneous.data.Texture;
import namelessju.scathapro.mixin.PlayerTabOverlayAccessor;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import namelessju.scathapro.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class MainOverlay
{
    private static final Texture STONEWORM_RENDER = Texture.scathaPro("overlay/worm_renders/stoneworm.png", 512, 256);
    private static final Texture STONEWORM_RENDER_OLD = Texture.scathaPro("overlay/worm_renders/stoneworm_old.png", 512, 256);


    private final ScathaPro scathaPro;
    private final Minecraft minecraft;


    private OverlayDynamicContainer mainContainer;

    private OverlayText titleText;
    private OverlayAnimatedImage scathaIcon;
    private OverlayImage scathaIconOverlay;
    private OverlayImage wormImage;
    private OverlayText regularWormKillsText;
    private OverlayText secondaryRegularWormKillsText;
    private OverlayText scathaKillsTitleText;
    private OverlayText scathaKillsText;
    private OverlayText secondaryScathaKillsText;
    private OverlayProgressBar spawnCooldownProgressBar;
    private TunnelVisionEffectProgressBar tunnelVisionEffectProgressBar;
    private OverlayText totalKillsText;
    private OverlayText secondaryTotalKillsText;
    private OverlayText wormStreakText;
    private OverlayText blockBransText;
    private OverlayText coordsText;
    private OverlayText lobbyTimeText;
    private OverlayText rarePetDropsText;
    private OverlayText epicPetDropsText;
    private OverlayText legendaryPetDropsText;
    private OverlayText scathaKillsSinceLastDropText;
    private OverlayText spawnCooldownTimerText;
    private OverlayText tunnelVisionStatusText;
    private OverlayText wormSpawnTimerText;
    private OverlayText profileStatsText;
    private OverlayText realTimeClockText;

    private OverlayContainer googlyEyeLeftContainer;
    private OverlayImage googlyEyeLeftInnerImage;
    private OverlayContainer googlyEyeRightContainer;
    private OverlayImage googlyEyeRightInnerImage;


    public final List<ToggleableOverlayElement> toggleableElements = Lists.newArrayList();


    private boolean isShown = false;
    private Alignment contentAlignment = null;
    private SecondaryStatsManager.SecondaryStats secondaryStats;

    private boolean updateNextTick = false;
    private boolean updatedLastTick = false;


    public MainOverlay(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        minecraft = scathaPro.minecraft;
    }

    public void init()
    {
        initializeElements();
        addDependencyEventListeners();
        updateAll();
        ScathaProEvents.overlayInitEvent.trigger(new ScathaProEvents.OverlayInitEventData(scathaPro, mainContainer));
    }

    private void initializeElements()
    {
        Config.OverlaySettings.ToggleableElementStates elementStatesConfig = scathaPro.config.overlay.elementStates;


        mainContainer = new OverlayDynamicContainer(0, 0, 1f, Direction.VERTICAL);
        mainContainer.padding = 5;

        OverlayContainer headerContainer = new OverlayContainer(0, 0, 1f).setMargin(0, 5);

        OverlayContainer iconContainer = new OverlayContainer(0, 0, 0.25f);

        iconContainer.add(scathaIcon = new OverlayAnimatedImage(0, 0, 64, 64, 0.688f));
        iconContainer.add(scathaIconOverlay = new OverlayImage(0, 0, 64, 64, 0.688f));

        googlyEyeRightContainer = new OverlayContainer(0, 0, 0.4f);
        googlyEyeRightContainer.expandsContainerSize = false;
        googlyEyeRightContainer.add(new OverlayImage(
            Texture.scathaPro("overlay/googly_eye_outer.png", 32, 32), 0, 0, 32, 32, 1f
        ));
        googlyEyeRightContainer.add(googlyEyeRightInnerImage = new OverlayImage(
            Texture.scathaPro("overlay/googly_eye_inner.png", 32, 32), 0, 0, 32, 32, 1f
        ));
        googlyEyeRightInnerImage.expandsContainerSize = false;
        iconContainer.add(googlyEyeRightContainer);

        googlyEyeLeftContainer = new OverlayContainer(0, 0, 0.44f);
        googlyEyeLeftContainer.expandsContainerSize = false;
        googlyEyeLeftContainer.add(new OverlayImage(
            Texture.scathaPro("overlay/googly_eye_outer.png", 32, 32), 0, 0, 32, 32, 1f
        ));
        googlyEyeLeftContainer.add(googlyEyeLeftInnerImage = new OverlayImage(
            Texture.scathaPro("overlay/googly_eye_inner.png", 32, 32), 0, 0, 32, 32, 1f
        ));
        googlyEyeLeftInnerImage.expandsContainerSize = false;
        iconContainer.add(googlyEyeLeftContainer);

        headerContainer.add(iconContainer);

        headerContainer.add(titleText = new OverlayText(minecraft.font, Util.Color.GOLD, 16, 0, 1.3f));
        mainContainer.add(headerContainer);
        addToggleableElement("Title", headerContainer, elementStatesConfig.headerShown);


        OverlayDynamicContainer countersContainer = new OverlayDynamicContainer(0, 0, 1f, Direction.HORIZONTAL)
            .setMargin(0, 4);


        OverlayContainer petDropsContainer = new OverlayContainer(0, 0, 1f);
        petDropsContainer.add(new OverlayText("Pets", minecraft.font, Util.Color.GREEN, 0, 0, 1f));
        petDropsContainer.add(new OverlayImage(
            Texture.scathaPro("generic/scatha_pet_rare.png", 256, 256), 0, 10, 16, 16, 0.58f
        ));
        petDropsContainer.add(rarePetDropsText = new OverlayText(minecraft.font, Util.Color.BLUE, 12, 11, 1f));
        petDropsContainer.add(new OverlayImage(
            Texture.scathaPro("generic/scatha_pet_epic.png", 256, 256), 0, 21, 16, 16, 0.58f
        ));
        petDropsContainer.add(epicPetDropsText = new OverlayText(minecraft.font, Util.Color.DARK_PURPLE, 12, 22, 1f));
        petDropsContainer.add(new OverlayImage(
            Texture.scathaPro("generic/scatha_pet_legendary.png", 256, 256), 0, 32, 16, 16, 0.58f
        ));
        petDropsContainer.add(legendaryPetDropsText = new OverlayText(minecraft.font, Util.Color.GOLD, 12, 33, 1f));
        countersContainer.add(petDropsContainer);
        addToggleableElement("Pet Drop Counters", petDropsContainer, elementStatesConfig.petDropCountersShown);


        OverlayContainer killsContainer = new OverlayContainer(8, 0, 1f);

        killsContainer.add(tunnelVisionEffectProgressBar = new TunnelVisionEffectProgressBar(0, 10, 77, 21, 1f));
        killsContainer.add(spawnCooldownProgressBar = new OverlayProgressBar(0, 10, 77, 21, 1f, 0x50FFFFFF, -1));

        killsContainer.add(new OverlayText("Worms", minecraft.font, Util.Color.YELLOW, 15, 0, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(wormImage = new OverlayImage(-4, 11, 64, 32, 0.6f));
        killsContainer.add(regularWormKillsText = new OverlayText(minecraft.font, Util.Color.WHITE, 15, 11, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(secondaryRegularWormKillsText = new OverlayText(minecraft.font, Util.Color.GRAY, 15, 22, 1f).setAlignment(Alignment.CENTER));

        killsContainer.add(scathaKillsTitleText = new OverlayText(minecraft.font, Util.Color.YELLOW, 58, 0, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(new OverlayImage(Texture.scathaPro("overlay/worm_renders/scatha.png", 512, 256), 39, 11, 64, 32, 0.6f));
        killsContainer.add(scathaKillsText = new OverlayText(minecraft.font, Util.Color.WHITE, 58, 11, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(secondaryScathaKillsText = new OverlayText(minecraft.font, Util.Color.GRAY, 58, 22, 1f).setAlignment(Alignment.CENTER));

        killsContainer.add(new OverlayText("Total", minecraft.font, Util.Color.WHITE, 86, 0, 1f));
        killsContainer.add(totalKillsText = new OverlayText(minecraft.font, Util.Color.WHITE, 86, 11, 1f));
        killsContainer.add(secondaryTotalKillsText = new OverlayText(minecraft.font, Util.Color.GRAY, 86, 22, 1f));

        killsContainer.add(wormStreakText = new OverlayText(minecraft.font, Util.Color.GRAY, 0, 33, 1f));

        addToggleableElement("Worm Stats", killsContainer, elementStatesConfig.wormStatsShown);
        countersContainer.add(killsContainer);


        mainContainer.add(countersContainer);


        mainContainer.add(scathaKillsSinceLastDropText = new OverlayText(minecraft.font, Util.Color.WHITE, 0, 2, 1f));
        addToggleableElement("Scathas Since Pet Drop", scathaKillsSinceLastDropText, elementStatesConfig.scathaKillsSinceLastPetDropShown);

        mainContainer.add(blockBransText = new OverlayText(minecraft.font, Util.Color.WHITE, 0, 2, 1f));
        addToggleableElement("Dwarven O's Block Brans Counter", blockBransText, elementStatesConfig.blockBransCounterShown);

        mainContainer.add(spawnCooldownTimerText = new OverlayText(minecraft.font, Util.Color.WHITE, 0, 2, 1f));
        addToggleableElement("Spawn Cooldown Status", spawnCooldownTimerText, elementStatesConfig.wormSpawnCooldownTimerShown);

        mainContainer.add(tunnelVisionStatusText = new OverlayText(minecraft.font, Util.Color.WHITE, 0, 2, 1f));
        addToggleableElement("Tunnel Vision Status", tunnelVisionStatusText, elementStatesConfig.tunnelVisionStatusTextShown);

        mainContainer.add(wormSpawnTimerText = new OverlayText(minecraft.font, Util.Color.GRAY, 0, 2, 1f));
        addToggleableElement("Time Since Last Spawn", wormSpawnTimerText, elementStatesConfig.timeSinceWormSpawnShown);

        mainContainer.add(lobbyTimeText = new OverlayText(minecraft.font, Util.Color.WHITE, 0, 2, 1f));
        addToggleableElement("Lobby Time", lobbyTimeText, elementStatesConfig.lobbyTimeShown);

        mainContainer.add(coordsText = new OverlayText(minecraft.font, Util.Color.WHITE, 0, 2, 1f));
        addToggleableElement("Coordinates/Orientation", coordsText, elementStatesConfig.coordinatesShown);

        mainContainer.add(profileStatsText = new OverlayText(minecraft.font, Util.Color.WHITE, 0, 2, 1f));
        addToggleableElement("Scatha Farming Profile Stats", profileStatsText, elementStatesConfig.profileStatsShown,
            Component.literal("\"/" + scathaPro.mainCommand.getCommandName() + " profileStats\"\nto update values")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(true)));

        mainContainer.add(realTimeClockText = new OverlayText(minecraft.font, Util.Color.WHITE, 0, 2, 1f));
        addToggleableElement("Real Time Clock", realTimeClockText, elementStatesConfig.realTimeClockShown);
    }

    private void addDependencyEventListeners()
    {
        Config config = scathaPro.config;
        config.overlay.scale.onValueChanged(_ -> {
            updateScale();
            updatePosition();
        });
        config.overlay.alignmentOverride.onValueChanged(_ -> updateContentAlignment());
        config.overlay.backgroundOpacity.onValueChanged(_ -> updateBackground());
        config.overlay.iconsEnabled.onValueChanged(_ -> updateAll());
        config.accessibility.useHighContrastColors.onValueChanged(_ -> updateContrast());
        config.worms.revertRegularWormTexture.onValueChanged(_ -> updateWormImage());
        config.alerts.mode.onValueChanged(_ -> updateScathaPetImage());
        config.unlockables.overlayIconGooglyEyesEnabled.onValueChanged(_ -> updateGooglyEyesEnabled());
        config.overlay.statsType.onValueChanged(_ -> updateStatsType());
        config.overlay.scathaPercentageDecimalPlaces.onValueChanged(_ -> {
            if (config.overlay.scathaPercentageAlternativePositionEnabled.get()) updateTotalKills();
            else updateScathaKills();
        });
        config.overlay.scathaPercentageAlternativePositionEnabled.onValueChanged(_ -> {
            updateScathaKills();
            updateTotalKills();
        });


        ScathaProEvents.scathaPetDropEvent.addListener(_ -> updatePetDrops());
        ScathaProEvents.scathaExtraItemDropEvent.addListener(data -> {
            if (data.itemType() == ScathaProEvents.ScathaExtraDropEventData.ItemType.DWARVEN_OS_BLOCK_BRAN)
            {
                updateBlockBrans();
            }
        });


        scathaPro.persistentDataProfileManager.onProfileChanged(_ -> updateAll());
    }

    public void toggleEnabled()
    {
        scathaPro.config.overlay.enabled.set(!isEnabled());
        scathaPro.config.save();
        scathaPro.chatManager.sendChatMessage("Overlay " + (scathaPro.mainOverlay.isEnabled() ? "enabled" : "disabled"));
    }

    private boolean isEnabled()
    {
        return scathaPro.config.overlay.enabled.get();
    }

    public void toggleShown()
    {
        if (!scathaPro.coreManager.isInCrystalHollows())
        {
            scathaPro.chatManager.sendChatErrorMessage("Overlay visibility cannot be changed outside of Crystal Hollows");
            return;
        }
        if (!isEnabled())
        {
            scathaPro.chatManager.sendChatErrorMessage("Overlay isn't enabled");
            return;
        }
        isShown = !isShown;
        scathaPro.chatManager.sendChatMessage("Overlay is now " + (isShown ? "shown" : "hidden"));
    }

    public void setShown(boolean value)
    {
        this.isShown = value;
    }

    public boolean isVisible()
    {
        return scathaPro.coreManager.isInCrystalHollows() && isShown && !(minecraft.screen instanceof OverlaySettingsScreen)
            && !minecraft.debugEntries.isOverlayVisible() && !((PlayerTabOverlayAccessor) minecraft.gui.getTabList()).isVisible();
    }

    public OverlayElement getMainElement()
    {
        return mainContainer;
    }

    public void tick()
    {
        if (mainContainer.isVisible() && isVisible())
        {
            requestUpdateTick();
        }

        if (updateNextTick)
        {
            updateTick();
            updatedLastTick = true;
        }
        else updatedLastTick = false;
    }

    public void requestUpdateTick()
    {
        updateNextTick = true;
        if (!updatedLastTick) updateTick();
    }

    public void extractRenderStateIfVisible(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        mainContainer.setVisible(isEnabled() && isVisible());
        mainContainer.extractRenderStateIfVisible(guiGraphics, deltaTracker);
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        mainContainer.setVisible(isEnabled());
        mainContainer.extractRenderStateIfVisible(guiGraphics, deltaTracker);
    }

    private String icon(char icon)
    {
        return icon(String.valueOf(icon));
    }

    private String icon(String icon)
    {
        return scathaPro.config.overlay.iconsEnabled.get() ? icon + " " : "";
    }

    private void addToggleableElement(String name, OverlayElement element, JsonFile.BooleanValue configValue)
    {
        addToggleableElement(name, element, configValue, null);
    }

    private void addToggleableElement(String name, OverlayElement element, JsonFile.BooleanValue configValue, Component description)
    {
        ToggleableOverlayElement toggleableElement = new ToggleableOverlayElement(name, element, configValue, description);
        toggleableElements.add(toggleableElement);
        configValue.onValueChanged(_ -> toggleableElement.updateVisibility());
    }

    public void updateAll()
    {
        updateToggleableElementStates();
        updateContentAlignment();

        updateStatsTypeValue();

        updateBackground();
        updateScathaName();
        updateWormImage();
        updateScathaPetImage();
        updateScathaPetImageColor();
        updateGooglyEyeInnerPositions();

        updatePetDrops();

        updateWormKills();
        updateScathaKills();

        updateBlockBrans();

        updateWormStreak();

        updateSpawnCooldown();
        updateTunnelVision();
        updateTimeSinceLastWormSpawn();

        updateCoords();
        updateLobbyTime();

        updateProfileStats();

        updateScale();
        updatePosition();
        updateContrast();
    }

    private void updateTick()
    {
        updateScathaPetImageColor();
        updateGooglyEyeInnerPositions();
        updateTimeSinceLastWormSpawn();
        updateSpawnCooldown();
        updateTunnelVision();
        updateCoords();
        updateLobbyTime();
        updateRealTimeClock();
        updatePosition();

        if (scathaPro.config.overlay.scathaPercentageAlternativePositionEnabled.get())
        {
            updateTotalKills();
        }
        else updateScathaKills();
    }

    private void updateToggleableElementStates()
    {
        for (ToggleableOverlayElement toggleableElement : toggleableElements)
        {
            toggleableElement.updateVisibility();
        }
    }

    private void updatePosition()
    {
        mainContainer.setResponsivePosition(minecraft.getWindow(),
            scathaPro.config.overlay.positionX.get(),
            scathaPro.config.overlay.positionY.get(),
            10, 10, contentAlignment
        );
    }

    private void updateScale()
    {
        mainContainer.setScale(scathaPro.config.overlay.scale.get());
    }

    private void updateBackground()
    {
        mainContainer.backgroundColor = ARGB.black(Mth.clamp(scathaPro.config.overlay.backgroundOpacity.get(), 0f, 1f));
    }

    private void updateContentAlignment()
    {
        this.contentAlignment = scathaPro.config.overlay.alignmentOverride.get();
    }

    private void updateContrast()
    {
        int color = (scathaPro.config.accessibility.useHighContrastColors.get() ? Util.Color.WHITE : Util.Color.GRAY);

        secondaryRegularWormKillsText.setColor(color);
        secondaryScathaKillsText.setColor(color);
        secondaryTotalKillsText.setColor(color);
        wormStreakText.setColor(color);
        scathaKillsSinceLastDropText.setColor(color);
        wormSpawnTimerText.setColor(color);

        updateTotalKills();
        updateLobbyTime();
        updateCoords();
        updateRealTimeClock();
        updateProfileStats();
    }

    private void updateScathaName()
    {
        if (scathaPro.coreManager.isScappaModeActive())
        {
            titleText.setText(TextUtil.getRainbowText("Scappa Farming:"));
            scathaKillsTitleText.setText("Scappas");
        }
        else
        {
            titleText.setText("Scatha Farming:");
            scathaKillsTitleText.setText("Scathas");
        }
    }

    private void updateStatsTypeValue()
    {
        secondaryStats = scathaPro.secondaryStatsManager.getStatsByType(scathaPro.config.overlay.statsType.get());
    }

    private void updateStatsType()
    {
        updateStatsTypeValue();
        updateWormKills();
        updateScathaKills();
        updateTotalKills();
        updateWormStreak();
        updateBlockBrans();
    }

    private void updateScathaPetImage()
    {
        if (scathaPro.coreManager.isScappaModeActive())
        {
            scathaIcon.setImage(Texture.scathaPro("overlay/scatha_icons/scatha_spin.png", 64, 64), 20, 42);
            scathaIconOverlay.setVisible(false);
        }
        else
        {
            scathaIcon.setImage(scathaPro.config.alerts.mode.get().getIconTexture());

            Texture iconOverlayTexture = scathaPro.config.alerts.mode.get().getIconOverlayTexture();
            if (iconOverlayTexture != null)
            {
                scathaIconOverlay.setVisible(true);
                scathaIconOverlay.setImage(iconOverlayTexture);
            }
            else
            {
                scathaIconOverlay.setVisible(false);
            }
        }

        if (!scathaIconOverlay.isVisible()) scathaIconOverlay.clearImage();

        updateScathaPetImageColor();
        updateGooglyEyesEnabled();
    }

    private void updateScathaPetImageColor()
    {
        if (scathaPro.coreManager.isScappaModeActive())
        {
            scathaIcon.setColor(-1);
            return;
        }

        scathaIcon.setColor(scathaPro.config.alerts.mode.get().getIconColor());
    }

    private void updateGooglyEyesEnabled()
    {
        googlyEyeLeftContainer.setVisible(false);
        googlyEyeRightContainer.setVisible(false);

        if (!TimeUtil.isAprilFools() && !scathaPro.config.unlockables.overlayIconGooglyEyesEnabled.get()) return;
        if (scathaPro.coreManager.isScappaModeActive()) return;

        OverlayIconEyePositions eyePositions = scathaPro.config.alerts.mode.get().eyePositions;

        int iconWidth = scathaIcon.getScaledWidth();
        int iconHeight = scathaIcon.getScaledHeight();

        if (eyePositions.leftEyePosition != null)
        {
            googlyEyeLeftContainer.setVisible(true);
            googlyEyeLeftContainer.setPosition(
                Math.round(iconWidth * eyePositions.leftEyePosition.x()) - googlyEyeLeftContainer.getScaledWidth() / 2,
                Math.round(iconHeight * eyePositions.leftEyePosition.y()) - googlyEyeLeftContainer.getScaledHeight() / 2
            );
        }

        if (eyePositions.rightEyePosition != null)
        {
            googlyEyeRightContainer.setVisible(true);
            googlyEyeRightContainer.setPosition(
                Math.round(iconWidth * eyePositions.rightEyePosition.x()) - googlyEyeRightContainer.getScaledWidth() / 2,
                Math.round(iconHeight * eyePositions.rightEyePosition.y()) - googlyEyeRightContainer.getScaledHeight() / 2
            );
        }

        updateGooglyEyeInnerPositions();
    }

    private void updateGooglyEyeInnerPositions()
    {
        if (googlyEyeLeftContainer.isVisible())
        {
            int eyeWidth = googlyEyeLeftContainer.getWidth();
            int eyeHeight = googlyEyeLeftContainer.getHeight();

            float angle = - (float) ((TimeUtil.getEpochMilliseconds() % 876L) / 876D * 2 * Math.PI);
            float unitX = (float) Math.sin(angle);
            float unitY = - (float) Math.cos(angle);

            googlyEyeLeftInnerImage.setPosition(
                eyeWidth / 2 - googlyEyeLeftInnerImage.getScaledWidth() / 2 + Math.round(eyeWidth * 0.2f * unitX),
                eyeHeight / 2 - googlyEyeLeftInnerImage.getScaledHeight() / 2 + Math.round(eyeHeight * 0.2f * unitY)
            );
        }

        if (googlyEyeRightContainer.isVisible())
        {
            int eyeWidth = googlyEyeRightContainer.getWidth();
            int eyeHeight = googlyEyeRightContainer.getHeight();

            float angle = (float) ((TimeUtil.getEpochMilliseconds() % 1000L) / 1000D * 2 * Math.PI);
            float unitX = (float) Math.sin(angle);
            float unitY = - (float) Math.cos(angle);

            googlyEyeRightInnerImage.setPosition(
                eyeWidth / 2 - googlyEyeRightInnerImage.getScaledWidth() / 2 + Math.round(eyeWidth * 0.2f * unitX),
                eyeHeight / 2 - googlyEyeRightInnerImage.getScaledHeight() / 2 + Math.round(eyeHeight * 0.2f * unitY)
            );
        }
    }

    public void updateScappaMode()
    {
        updateScathaName();
        updateScathaPetImage();
        updateScathaKillsSinceLastDrop();
        updateWormStreak();
    }

    private void updatePetDrops()
    {
        rarePetDropsText.setText(Integer.toString(getProfileData().rarePetDrops.get()));
        epicPetDropsText.setText(Integer.toString(getProfileData().epicPetDrops.get()));
        legendaryPetDropsText.setText(Integer.toString(getProfileData().legendaryPetDrops.get()));
    }

    private void updateWormImage()
    {
        Texture wormTexture = STONEWORM_RENDER;
        if (scathaPro.config.worms.revertRegularWormTexture.get())
        {
            wormTexture = STONEWORM_RENDER_OLD;
        }
        wormImage.setImage(wormTexture);
    }

    public void updateWormKills()
    {
        regularWormKillsText.setText(TextUtil.numberToComponentOrObf(getProfileData().regularWormKills.get()));
        secondaryRegularWormKillsText.setText(TextUtil.numberToString(secondaryStats.getRegularWormKills()));

        updateTotalKills();
    }

    public void updateScathaKills()
    {
        int cycleAmountDuration = Math.max(scathaPro.config.overlay.scathaPercentageCycleAmountDuration.get(), 1) * 1000;
        int cyclePercentageDuration = Math.max(scathaPro.config.overlay.scathaPercentageCyclePercentageDuration.get(), 0) * 1000;

        if (scathaPro.config.overlay.scathaPercentageAlternativePositionEnabled.get()
            || cyclePercentageDuration == 0 || TimeUtil.getAnimationState(cycleAmountDuration, cyclePercentageDuration))
        {
            // Regular kill amounts

            scathaKillsText.setText(TextUtil.numberToComponentOrObf(getProfileData().scathaKills.get()));
            secondaryScathaKillsText.setText(TextUtil.numberToString(secondaryStats.getScathaKills()));
        }
        else
        {
            // Scatha percentages

            int totalKills = getProfileData().regularWormKills.get() >= 0 && getProfileData().scathaKills.get() >= 0
                ? getProfileData().regularWormKills.get() + getProfileData().scathaKills.get()
                : -1;
            int secondaryTotalKills = secondaryStats.getRegularWormKills() + secondaryStats.getScathaKills();

            float scathaPercentage = totalKills > 0 ? ((float) getProfileData().scathaKills.get() / totalKills) * 100 : -1f;
            float secondaryScathaPercentage = secondaryTotalKills > 0 ? ((float) secondaryStats.getScathaKills() / secondaryTotalKills) * 100 : -1f;

            int scathaPercentageDecimalPlaces = scathaPro.config.overlay.scathaPercentageDecimalPlaces.get();

            String scathaPercentageString = scathaPercentage >= 100f ? "100" : (scathaPercentage <= 0f ? "0" : TextUtil.numberToString(scathaPercentage, scathaPercentageDecimalPlaces, true));
            String secondaryScathaPercentageString = secondaryScathaPercentage >= 100f ? "100" : (secondaryScathaPercentage <= 0f ? "0" : TextUtil.numberToString(secondaryScathaPercentage, scathaPercentageDecimalPlaces, true));
            scathaKillsText.setText(Component.empty()
                .append(scathaPercentage >= 0
                    ? Component.literal(scathaPercentageString)
                    : Component.literal("?").withStyle(Style.EMPTY.withObfuscated(true)))
                .append("%"));
            secondaryScathaKillsText.setText(Component.empty().withStyle(TextUtil.contrastableGray(scathaPro))
                .append(secondaryScathaPercentage >= 0
                    ? Component.literal(secondaryScathaPercentageString)
                    : Component.literal("?").withStyle(Style.EMPTY.withObfuscated(true)))
                .append("%"));
        }

        updateTotalKills();
        updateScathaKillsSinceLastDrop();
    }

    private void updateTotalKills()
    {
        int secondaryTotalKills = secondaryStats.getRegularWormKills() + secondaryStats.getScathaKills();
        int totalKills = getProfileData().regularWormKills.get() >= 0 && getProfileData().scathaKills.get() >= 0
            ? getProfileData().regularWormKills.get() + getProfileData().scathaKills.get()
            : -1;

        ChatFormatting contrastableGray = TextUtil.contrastableGray(scathaPro);

        Component scathaPercentageComponent = null;
        Component secondaryScathaPercentageComponent = null;
        if (scathaPro.config.overlay.scathaPercentageAlternativePositionEnabled.get())
        {
            float scathaPercentage = totalKills > 0 ? ((float) getProfileData().scathaKills.get() / totalKills) * 100 : -1f;
            float secondaryScathaPercentage = secondaryTotalKills > 0 ? ((float) secondaryStats.getScathaKills() / secondaryTotalKills) * 100 : -1f;

            int scathaPercentageDecimalPlaces = scathaPro.config.overlay.scathaPercentageDecimalPlaces.get();

            String scathaPercentageString = scathaPercentage >= 100f ? "100" : (scathaPercentage <= 0f ? "0" : TextUtil.numberToString(scathaPercentage, scathaPercentageDecimalPlaces, true));
            String secondaryScathaPercentageString = secondaryScathaPercentage >= 100f ? "100" : (secondaryScathaPercentage <= 0f ? "0" : TextUtil.numberToString(secondaryScathaPercentage, scathaPercentageDecimalPlaces, true));
            if (scathaPercentage >= 0) scathaPercentageComponent = Component.literal(" (" + scathaPercentageString + "%)").withStyle(contrastableGray);
            if (secondaryScathaPercentage >= 0) secondaryScathaPercentageComponent = Component.literal(" (" + secondaryScathaPercentageString + "%)").withStyle(contrastableGray);
        }

        if (totalKills >= 0)
        {
            MutableComponent component = Component.empty().append(TextUtil.numberToString(totalKills));
            if (scathaPercentageComponent != null) component.append(scathaPercentageComponent);
            totalKillsText.setText(component);
        }
        else totalKillsText.setText(Component.literal("?").withStyle(Style.EMPTY.withObfuscated(true)));

        MutableComponent secondaryComponent = Component.empty().append(TextUtil.numberToString(secondaryTotalKills));
        if (secondaryScathaPercentageComponent != null) secondaryComponent.append(secondaryScathaPercentageComponent);
        secondaryTotalKillsText.setText(secondaryComponent);
    }

    public void updateWormStreak()
    {
        int scathaSpawnStreak = secondaryStats.getScathaSpawnStreak();
        String scathaString = scathaPro.coreManager.isScappaModeActive() ? "Scappa" : "Scatha";
        wormStreakText.setText(
            scathaSpawnStreak != 0
            ? (
                scathaSpawnStreak > 0
                ? scathaString + " spawn streak: " + TextUtil.numberToString(scathaSpawnStreak)
                : "No " + scathaString + " for " + TextUtil.numberToString(-scathaSpawnStreak) + " " + (-scathaSpawnStreak == 1 ? "spawn" : "spawns")
            )
            : "No worms spawned yet"
        );
    }

    private void updateLobbyTime()
    {
        Level level = minecraft.level;

        long worldTime = level != null ? level.getDefaultClockTime() : -1L;
        int worldDay = worldTime >= 0L ? (int) Math.floor(worldTime / 24000f) : 0;
        float worldDayProgress = worldTime >= 0L ? (worldTime % 24000f) / 24000f : 0f;

        long lobbyTime = level != null && scathaPro.coreManager.isInCrystalHollows() ? TimeUtil.getEpochMilliseconds() - scathaPro.coreManager.lastWorldJoinTime : 0L;
        SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
        timerFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        lobbyTimeText.setText(Component.empty()
            .append(icon(UnicodeSymbol.sun) + "Day " + worldDay)
            .append(Component.literal(
                    " ("
                    + TextUtil.numberToString(worldDayProgress * 100f, 0, false, RoundingMode.DOWN)
                    + "%) / " + timerFormat.format(lobbyTime)
                ).withStyle(TextUtil.contrastableGray(scathaPro)))
        );
    }

    private void updateCoords()
    {
        // min has to be extended to start counting from the right side of the wall,
        // due to how block positions vs decimal number positions work
        int boundsMinExtended = Constants.crystalHollowsBoundsMin + 1;
        double wallLength = Constants.crystalHollowsBoundsMax - boundsMinExtended;

        String coordinatesString;
        String facingAxisString;
        double wallProgress;

        LocalPlayer player = minecraft.player;
        if (player != null)
        {
            BlockPos blockPos = player.blockPosition();
            coordinatesString = blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();

            net.minecraft.core.Direction direction = player.getDirection();
            facingAxisString = (direction.getAxisDirection() == net.minecraft.core.Direction.AxisDirection.POSITIVE ? "+" : "-")
                + direction.getAxis().getName().toUpperCase();
            double wallDistance = switch (direction)
            {
                case NORTH -> Constants.crystalHollowsBoundsMax - player.getZ();
                case EAST -> player.getX() - boundsMinExtended;
                case SOUTH -> player.getZ() - boundsMinExtended;
                case WEST -> Constants.crystalHollowsBoundsMax - player.getX();
                default -> 0;
            };

            wallProgress = Mth.clamp((wallDistance - 1D) / (wallLength - 2D), 0D, 1D);
        }
        else
        {
            coordinatesString = "0 0 0";
            facingAxisString = "+Z";
            wallProgress = 0D;
        }

        float roundedWallProgress = (float) (Math.floor(wallProgress * 100 * 10) / 10f);
        String wallProgressString = roundedWallProgress >= 100f ? "100" : (roundedWallProgress <= 0f ? "0" : TextUtil.numberToString(roundedWallProgress, 1, true));

        ChatFormatting contrastableGray = TextUtil.contrastableGray(scathaPro);
        coordsText.setText(Component.empty()
            .append(Component.literal(coordinatesString + " " + facingAxisString + ": ").withStyle(contrastableGray))
            .append(wallProgressString + "% to wall"));
    }

    public void updateScathaKillsSinceLastDrop()
    {
        int dryStreak = -1;
        if (!getProfileData().isPetDropDryStreakInvalidated.get())
        {
            int scathaKillsAtLastDrop = getProfileData().scathaKillsAtLastDrop.getOr(-1);
            if (scathaKillsAtLastDrop < 0) dryStreak = getProfileData().scathaKills.get();
            else dryStreak = getProfileData().scathaKills.get() - scathaKillsAtLastDrop;
        }

        scathaKillsSinceLastDropText.setText(Component.empty()
            .append(icon(UnicodeSymbol.sword) + (scathaPro.coreManager.isScappaModeActive() ? "Scappas" : "Scathas") + " since last pet: ")
            .append(TextUtil.numberToComponentOrObf(dryStreak)));
    }

    public void updateBlockBrans()
    {
        blockBransText.setText(Component.empty().withStyle(TextUtil.contrastableGray(scathaPro))
            .append(Component.literal("Block Brans").withStyle(ChatFormatting.BLUE)).append(" dropped: ")
            .append(TextUtil.numberToComponentOrObf(getProfileData().blockBransDropped.get()))
            .append(" / ").append(TextUtil.numberToComponentOrObf(secondaryStats.getBlockBransDropped()))
        );
    }

    private void updateSpawnCooldown()
    {
        long cooldownTimer = TimeUtil.getEpochMilliseconds() - scathaPro.coreManager.wormSpawnCooldownStartTime;
        float progress;

        if (scathaPro.coreManager.wormSpawnCooldownStartTime >= 0f
            && (
                progress = 1f - (cooldownTimer / (float) Constants.wormSpawnCooldown)
            ) > 0f
        ) {
            spawnCooldownProgressBar.setVisible(true);
            spawnCooldownProgressBar.setProgress(progress);
            spawnCooldownTimerText.setText(Component.literal(
                icon(UnicodeSymbol.hourglass) + "Worm spawn cooldown: " + TimeUtil.getHMSTimeString(Constants.wormSpawnCooldown - cooldownTimer, true)
            ).withStyle(ChatFormatting.RED));
        }
        else
        {
            spawnCooldownProgressBar.setVisible(false);
            spawnCooldownTimerText.setText(Component.literal(icon(UnicodeSymbol.heavyCheckMark) + "Worms ready to spawn").withStyle(ChatFormatting.GREEN));
        }
    }

    private void updateTunnelVision()
    {
        final Component abilityName = Component.literal("Tunnel Vision").withStyle(ChatFormatting.GOLD);

        long now = TimeUtil.getEpochMilliseconds();
        long tunnelVisionElapsedTime;

        boolean showProgressBar = false;
        float progress = 0f;
        if (
            scathaPro.coreManager.tunnelVisionStartTime >= 0L
            && (
                progress = 1f - (
                    (tunnelVisionElapsedTime = now - scathaPro.coreManager.tunnelVisionStartTime)
                    / (float) Constants.tunnelVisionEffectDuration
                )
            ) > 0f
        ) {
            if (scathaPro.coreManager.wormSpawnCooldownStartTime < 0L
                || now - scathaPro.coreManager.wormSpawnCooldownStartTime >= Constants.wormSpawnCooldown)
            {
                showProgressBar = true;
            }

            tunnelVisionStatusText.setText(Component.empty()
                .append(Component.literal(icon(UnicodeSymbol.lightingBolt)).withStyle(ChatFormatting.YELLOW))
                .append(abilityName)
                .append(Component.literal(" active: "
                        + TimeUtil.getHMSTimeString(Constants.tunnelVisionEffectDuration - tunnelVisionElapsedTime, true))
                    .withStyle(ChatFormatting.YELLOW)
                )
            );
        }
        else
        {
            if (scathaPro.coreManager.tunnelVisionCooldownEndTime >= 0L && now < scathaPro.coreManager.tunnelVisionCooldownEndTime)
            {
                tunnelVisionStatusText.setText(Component.empty()
                    .append(Component.literal(icon(UnicodeSymbol.hourglass)).withStyle(ChatFormatting.RED))
                    .append(abilityName)
                    .append(Component.literal(" cooldown: "
                            + TimeUtil.getHMSTimeString(scathaPro.coreManager.tunnelVisionCooldownEndTime - now, true))
                        .withStyle(ChatFormatting.RED)
                    )
                );
            }
            else
            {
                tunnelVisionStatusText.setText(Component.empty()
                    .append(Component.literal(icon(UnicodeSymbol.heavyCheckMark)).withStyle(ChatFormatting.GREEN))
                    .append(abilityName).append(Component.literal(" available").withStyle(ChatFormatting.GREEN))
                );
            }
        }

        if (showProgressBar)
        {
            tunnelVisionEffectProgressBar.setVisible(true);
            tunnelVisionEffectProgressBar.setProgress(progress);
        }
        else tunnelVisionEffectProgressBar.setVisible(false);
    }

    private void updateTimeSinceLastWormSpawn()
    {
        Component timeComponent;
        if (scathaPro.coreManager.lastWormSpawnTime >= 0L && minecraft.level != null)
        {
            timeComponent = Component.literal(
                TimeUtil.getHMSTimeString(TimeUtil.getEpochMilliseconds() - scathaPro.coreManager.lastWormSpawnTime, false)
            );
        }
        else timeComponent = Component.literal("?").withStyle(Style.EMPTY.withObfuscated(true));
        wormSpawnTimerText.setText(Component.empty()
            .append("Time since last spawn: ")
            .append(timeComponent));
    }

    public void updateProfileStats()
    {
        ChatFormatting contrastableGray = TextUtil.contrastableGray(scathaPro);
        profileStatsText.setText(Component.empty()
            .append(scathaPro.persistentDataProfileManager.getTotalMagicFindComponent(false, true))
            .append(Component.literal(" ").withStyle(contrastableGray))
            .append(scathaPro.persistentDataProfileManager.getPetLuckComponent(true))
            .append(Component.literal(" / ").withStyle(contrastableGray))
            .append(scathaPro.persistentDataProfileManager.getEffectiveMagicFindComponent(false).append(" EMF"))
        );
    }

    private void updateRealTimeClock()
    {
        LocalDateTime now = LocalDateTime.now();
        String timeStringMain = scathaPro.config.accessibility.timeFormat.get().formatHoursMinutes(now);
        String timeStringSecondary
                = scathaPro.config.accessibility.timeFormat.get().formatSeconds(now)
                + Objects.requireNonNullElse(scathaPro.config.accessibility.timeFormat.get().formatSuffix(now), "");
        String dateStringDay = DateTimeFormatter.ofPattern("EEE.", Locale.ENGLISH).format(now);
        String dateStringDate = scathaPro.config.accessibility.dateFormat.get().format(now);

        ChatFormatting contrastableGray = TextUtil.contrastableGray(scathaPro);
        realTimeClockText.setText(Component.empty()
            .append(icon(UnicodeSymbol.clock) + timeStringMain)
            .append(
                Component.literal(
                    timeStringSecondary
                    + ", " + dateStringDay
                    + " " + dateStringDate
                ).withStyle(contrastableGray)
            )
        );
    }


    private PersistentData.ProfileData getProfileData()
    {
        return scathaPro.getProfileData();
    }

    public record ToggleableOverlayElement(String elementName, OverlayElement element, JsonFile.BooleanValue configValue, Component description)
    {
        public void updateVisibility()
        {
            element.setVisible(configValue.get());
        }
    }
}