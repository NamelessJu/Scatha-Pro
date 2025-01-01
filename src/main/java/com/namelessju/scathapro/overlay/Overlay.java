package com.namelessju.scathapro.overlay;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.events.OverlayInitEvent;
import com.namelessju.scathapro.gui.menus.overlay.OverlaySettingsGui;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.enums.WormStatsType;
import com.namelessju.scathapro.overlay.elements.AnimatedOverlayImage;
import com.namelessju.scathapro.overlay.elements.AnomalousDesireEffectProgressBar;
import com.namelessju.scathapro.overlay.elements.DynamicOverlayContainer;
import com.namelessju.scathapro.overlay.elements.OverlayContainer;
import com.namelessju.scathapro.overlay.elements.OverlayElement;
import com.namelessju.scathapro.overlay.elements.OverlayImage;
import com.namelessju.scathapro.overlay.elements.OverlayProgressBar;
import com.namelessju.scathapro.overlay.elements.OverlayText;
import com.namelessju.scathapro.overlay.elements.DynamicOverlayContainer.Direction;
import com.namelessju.scathapro.overlay.elements.OverlayElement.Alignment;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.TimeUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class Overlay
{
    public static class ToggleableOverlayElement
    {
        public final String id;
        public final String elementName;
        public final OverlayElement element;
        public final boolean defaultVisibility;
        
        public ToggleableOverlayElement(String id, String elementName, OverlayElement element, boolean defaultVisibility)
        {
            this.id = id;
            this.elementName = elementName;
            this.element = element;
            this.defaultVisibility = defaultVisibility;
            
            element.setVisible(defaultVisibility);
        }
        
        public void toggle()
        {
            setVisible(!element.isVisible());
        }
        
        public void setVisible(boolean visible)
        {
            element.setVisible(visible);
        }
        
        public boolean isVisible()
        {
            return element.isVisible();
        }
    }
    
    private final ScathaPro scathaPro;
    private final Minecraft mc;
    
    
    private static final int BACKGROUND_COLOR = 0x50000000;
    
    private final DynamicOverlayContainer mainContainer;
    
    private final OverlayText titleText;
    private final AnimatedOverlayImage scathaIcon;
    private final OverlayImage scathaIconOverlay;
    private final OverlayText regularWormKillsText;
    private final OverlayText secondaryRegularWormKillsText;
    private final OverlayText scathaKillsTitleText;
    private final OverlayText scathaKillsText;
    private final OverlayText secondaryScathaKillsText;
    private final OverlayProgressBar spawnCooldownProgressBar;
    private final AnomalousDesireEffectProgressBar anomalousDesireEffectProgressBar;
    private final OverlayText totalKillsText;
    private final OverlayText secondaryTotalKillsText;
    private final OverlayText wormStreakText;
    private final OverlayText coordsText;
    private final OverlayText timeText;
    private final OverlayText rarePetDropsText;
    private final OverlayText epicPetDropsText;
    private final OverlayText legendaryPetDropsText;
    private final OverlayText scathaKillsSinceLastDropText;
    private final OverlayText spawnCooldownTimerText;
    private final OverlayText anomalousDesireStatusText;
    private final OverlayText wormSpawnTimerText;
    private final OverlayText realTimeClockText;
    
    
    public final List<ToggleableOverlayElement> toggleableOverlayElements = Lists.<ToggleableOverlayElement>newArrayList();
    
    
    private WormStatsType statsType = WormStatsType.PER_LOBBY;
    
    
    public Overlay(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.getMinecraft();
        
        statsType = scathaPro.getConfig().getEnum(Config.Key.statsType, WormStatsType.class);
        if (statsType == null) statsType = WormStatsType.values()[0];
        
        mainContainer = new DynamicOverlayContainer(0, 0, 1f, Direction.VERTICAL);
        mainContainer.padding = 5;
        
        
        OverlayContainer headerContainer = new OverlayContainer(0, 0, 1f).setMargin(0, 5);
        headerContainer.add(scathaIcon = new AnimatedOverlayImage(0, 0, 0.172f));
        headerContainer.add(scathaIconOverlay = new OverlayImage(0, 0, 0.172f));
        headerContainer.add(titleText = new OverlayText(null, Util.Color.GOLD.getValue(), 16, 0, 1.3f));
        mainContainer.add(headerContainer);
        addToggleableElement("header", "Title", headerContainer);
        
        
        DynamicOverlayContainer countersContainer = new DynamicOverlayContainer(0, 0, 1f, Direction.HORIZONTAL).setMargin(0, 4);
        
        
        OverlayContainer petDropsContainer = new OverlayContainer(0, 0, 1f);
        petDropsContainer.add(new OverlayText("Pets", Util.Color.GREEN.getValue(), 0, 0, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet_rare.png", 64, 64, 0, 10, 0.145f));
        petDropsContainer.add(rarePetDropsText = new OverlayText(null, Util.Color.BLUE.getValue(), 12, 11, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet_epic.png", 64, 64, 0, 21, 0.145f));
        petDropsContainer.add(epicPetDropsText = new OverlayText(null, Util.Color.DARK_PURPLE.getValue(), 12, 22, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet_legendary.png", 64, 64, 0, 32, 0.145f));
        petDropsContainer.add(legendaryPetDropsText = new OverlayText(null, Util.Color.GOLD.getValue(), 12, 33, 1f));
        countersContainer.add(petDropsContainer);
        addToggleableElement("petDrops", "Pet Drop Counters", petDropsContainer);
        
        
        OverlayContainer killsContainer = new OverlayContainer(8, 0, 1f);

        killsContainer.add(anomalousDesireEffectProgressBar = new AnomalousDesireEffectProgressBar(0, 10, 77, 21, 1f));
        killsContainer.add(spawnCooldownProgressBar = new OverlayProgressBar(0, 10, 77, 21, 1f, 0x50FFFFFF, -1));
        
        killsContainer.add(new OverlayText("Worms", Util.Color.YELLOW.getValue(), 15, 0, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(new OverlayImage("overlay/worm.png", 512, 256, -5, 10, 0.08f));
        killsContainer.add(regularWormKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 15, 11, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(secondaryRegularWormKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 15, 22, 1f).setAlignment(Alignment.CENTER));
        
        killsContainer.add(scathaKillsTitleText = new OverlayText(null, Util.Color.YELLOW.getValue(), 58, 0, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(new OverlayImage("overlay/scatha.png", 512, 256, 38, 10, 0.08f));
        killsContainer.add(scathaKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 58, 11, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(secondaryScathaKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 58, 22, 1f).setAlignment(Alignment.CENTER));
        
        killsContainer.add(new OverlayText("Total", Util.Color.WHITE.getValue(), 86, 0, 1f));
        killsContainer.add(totalKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 86, 11, 1f));
        killsContainer.add(secondaryTotalKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 86, 22, 1f));
        
        killsContainer.add(wormStreakText = new OverlayText(null, Util.Color.GRAY.getValue(), 0, 33, 1f));
        
        addToggleableElement("wormStats", "Worm Stats", killsContainer);
        countersContainer.add(killsContainer);
        
        
        mainContainer.add(countersContainer);

        
        mainContainer.add(scathaKillsSinceLastDropText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 2, 1f));
        addToggleableElement("scathaKillsSinceLastPetDrop", "Scathas Since Pet Drop", scathaKillsSinceLastDropText);
        
        mainContainer.add(spawnCooldownTimerText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 2, 1f));
        addToggleableElement("spawnCooldownTimer", "Spawn Cooldown Status", spawnCooldownTimerText, false);
        
        mainContainer.add(anomalousDesireStatusText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 2, 1f));
        addToggleableElement("anomalousDesireStatusText", "Anomalous Desire Status", anomalousDesireStatusText, false);
        
        mainContainer.add(wormSpawnTimerText = new OverlayText(null, Util.Color.GRAY.getValue(), 0, 2, 1f));
        addToggleableElement("timeSinceWormSpawn", "Time Since Last Spawn", wormSpawnTimerText, false);
        
        mainContainer.add(timeText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 2, 1f));
        addToggleableElement("time", "Lobby Time", timeText);
        
        mainContainer.add(coordsText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 2, 1f));
        addToggleableElement("coords", "Coordinates/Orientation", coordsText);

        mainContainer.add(realTimeClockText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 2, 1f));
        addToggleableElement("realTimeClock", "Real Time Clock", realTimeClockText, false);
        
        
        MinecraftForge.EVENT_BUS.post(new OverlayInitEvent(mainContainer));
        
        
        loadToggleableElementStates();
    }
    
    private void addToggleableElement(String id, String name, OverlayElement element)
    {
        addToggleableElement(id, name, element, true);
    }
    
    private void addToggleableElement(String id, String name, OverlayElement element, boolean defaultVisibility)
    {
        toggleableOverlayElements.add(new ToggleableOverlayElement(id, name, element, defaultVisibility));
    }
    
    public void loadToggleableElementStates()
    {
        String jsonString = scathaPro.getConfig().getString(Config.Key.overlayElementStates);
        JsonObject json = JsonUtil.parseObject(jsonString);
        
        for (ToggleableOverlayElement toggleableElement : toggleableOverlayElements)
        {
            Boolean visible = JsonUtil.getBoolean(json, toggleableElement.id);
            toggleableElement.setVisible(visible != null ? visible : toggleableElement.defaultVisibility);
        }
    }
    
    public void saveToggleableElementStates()
    {
        JsonObject json = new JsonObject();
        
        for (ToggleableOverlayElement toggleableElement : toggleableOverlayElements)
        {
            boolean visible = toggleableElement.isVisible();
            if (visible != toggleableElement.defaultVisibility) JsonUtil.set(json, toggleableElement.id, new JsonPrimitive(visible));
        }
        
        scathaPro.getConfig().set(Config.Key.overlayElementStates, json.toString());
        scathaPro.getConfig().save();
    }
    
    public boolean toggleVisibility()
    {
        boolean overlayVisible = scathaPro.getConfig().getBoolean(Config.Key.overlayEnabled);
        scathaPro.getConfig().set(Config.Key.overlayEnabled, !overlayVisible);
        scathaPro.getConfig().save();
        updateVisibility();
        return !overlayVisible;
    }
    
    public void updateStatsType()
    {
        WormStatsType statsType = scathaPro.getConfig().getEnum(Config.Key.statsType, WormStatsType.class);
        if (statsType == null) statsType = WormStatsType.values()[0];
        
        this.statsType = statsType;
        
        updateWormKills();
        updateScathaKills();
        updateTotalKills();
        updateWormStreak();
    }
    
    public boolean isOverlayDrawAllowed()
    {
        return scathaPro.isInCrystalHollows() && !mc.gameSettings.showDebugInfo && !Util.isPlayerListOpened() && !(mc.currentScreen instanceof OverlaySettingsGui);
    }
    
    public void drawOverlayIfAllowed()
    {
        if (isOverlayDrawAllowed()) drawOverlay();
    }

    public void drawOverlay()
    {
        mainContainer.draw();
    }
    
    public void drawOverlay(int x, int y)
    {
        if (!mainContainer.isVisible()) return;
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        mainContainer.drawUnaligned();
        GlStateManager.popMatrix();
    }
    
    public void updateContrast()
    {
        int color = (scathaPro.getConfig().getBoolean(Config.Key.highContrastColors) ? Util.Color.WHITE : Util.Color.GRAY).getValue();
        
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
    }
    
    public void updateRealtimeElements()
    {
        if (!mainContainer.isVisible()) return;
        
        mainContainer.backgroundColor = scathaPro.getConfig().getBoolean(Config.Key.overlayBackgroundEnabled) ? BACKGROUND_COLOR : -1;
        
        updateScathaPetImageColor();
        updateTimeSinceLastWormSpawn();
        updateSpawnCooldown();
        updateAnomalousDesire();
        updateCoords();
        updateLobbyTime();
        updateRealTimeClock();
        updatePosition();
        
        if (scathaPro.getConfig().getBoolean(Config.Key.scathaPercentageAlternativePosition)) updateTotalKills();
        else updateScathaKills();
    }
    
    public void updateOverlayFull()
    {
        updateTitles();
        updateScathaPetImage();
        updateScathaPetImageColor();

        updateWormKills();
        updateScathaKills();
        updateTotalKills();
        
        updateWormStreak();

        updateSpawnCooldown();
        updateAnomalousDesire();
        updateTimeSinceLastWormSpawn();
        
        updateCoords();
        updateLobbyTime();
        
        updatePetDrops();
        
        updatePosition();
        updateScale();
        updateVisibility();
        updateContrast();
    }

    public void updatePosition()
    {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
    
        final double[] overlayPositionPercentage = {scathaPro.getConfig().getDouble(Config.Key.overlayX), scathaPro.getConfig().getDouble(Config.Key.overlayY)};
        final int[] overlayPosition = {
                overlayPositionPercentage[0] >= 0 ? (int) Math.round(scaledResolution.getScaledWidth() * overlayPositionPercentage[0]) : 10,
                overlayPositionPercentage[1] >= 0 ? (int) Math.round(scaledResolution.getScaledHeight() * overlayPositionPercentage[1]) : 10,
        };
        final int[] translation = {
                overlayPositionPercentage[0] >= 0 ? (int) Math.round(-mainContainer.getScaledWidth() * overlayPositionPercentage[0]) : 0,
                overlayPositionPercentage[1] >= 0 ? (int) Math.round(-mainContainer.getScaledHeight() * overlayPositionPercentage[1]) : 0
        };
        mainContainer.setPosition(overlayPosition[0] + translation[0], overlayPosition[1] + translation[1]);
    
        if (overlayPositionPercentage[0] == 0.5) mainContainer.setContentAlignment(OverlayElement.Alignment.CENTER);
        else if (overlayPositionPercentage[0] > 0.5) mainContainer.setContentAlignment(OverlayElement.Alignment.RIGHT);
        else mainContainer.setContentAlignment(OverlayElement.Alignment.LEFT);
    }
    
    public Alignment getContentAlignment()
    {
        return mainContainer.getContentAlignment();
    }
    
    public int getWidth()
    {
        return mainContainer.getWidth();
    }
    
    public void updateScale()
    {
        mainContainer.setScale((float) scathaPro.getConfig().getDouble(Config.Key.overlayScale));
    }
    
    public float getScale()
    {
        return mainContainer.getScale();
    }
    
    public void updateVisibility()
    {
        mainContainer.setVisible(scathaPro.getConfig().getBoolean(Config.Key.overlayEnabled));
    }
    
    public void updateTitles()
    {
        if (scathaPro.isScappaModeActive())
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
    
    public void updateScathaPetImage()
    {
        if (scathaPro.isScappaModeActive())
        {
            scathaIcon.setImage("overlay/scatha_icons/scatha_spin.png", 64, 64, 20, 42);
            scathaIconOverlay.setImage(null, 64, 64);
            return;
        }
        
        scathaIcon.setImage(scathaPro.getAlertModeManager().getCurrentMode().getIconPath(), 64, 64);

        String overlayPath = scathaPro.getAlertModeManager().getCurrentMode().getIconOverlayPath();
        if (overlayPath != null)
        {
            scathaIconOverlay.setVisible(true);
            scathaIconOverlay.setImage(overlayPath, 64, 64);
        }
        else
        {
            scathaIconOverlay.setVisible(false);
        }
    }
    
    public void updateScathaPetImageColor()
    {
        if (scathaPro.isScappaModeActive())
        {
            scathaIcon.setColor(1f, 1f, 1f);
            return;
        }
        
        scathaIcon.setColor(scathaPro.getAlertModeManager().getCurrentMode().getIconColor());
    }
    
    public void updateScappaMode()
    {
        updateTitles();
        updateScathaPetImage();
        updateScathaPetImageColor();
        updateScathaKillsSinceLastDrop();
        updateWormStreak();
    }
    
    public void updatePetDrops()
    {
        rarePetDropsText.setText(Integer.toString(scathaPro.variables.rarePetDrops));
        epicPetDropsText.setText(Integer.toString(scathaPro.variables.epicPetDrops));
        legendaryPetDropsText.setText(Integer.toString(scathaPro.variables.legendaryPetDrops));
    }
    
    public void updateWormKills()
    {
        World world = mc.theWorld;
        
        secondaryRegularWormKillsText.setText(Util.numberToString(world != null ? statsType.regularWormKills : 0));
        regularWormKillsText.setText(scathaPro.variables.regularWormKills >= 0 ? Util.numberToString(scathaPro.variables.regularWormKills) : EnumChatFormatting.OBFUSCATED + "?");

        updateTotalKills();
    }
    
    public void updateScathaKills()
    {
        World world = mc.theWorld;
        
        int cycleAmountDuration = Math.max(scathaPro.getConfig().getInt(Config.Key.scathaPercentageCycleAmountDuration), 1) * 1000;
        int cyclePercentageDuration = Math.max(scathaPro.getConfig().getInt(Config.Key.scathaPercentageCyclePercentageDuration), 1) * 1000;
        
        if (scathaPro.getConfig().getBoolean(Config.Key.scathaPercentageAlternativePosition)
            || TimeUtil.getAnimationState(cycleAmountDuration, cyclePercentageDuration))
        {
            // Regular kill amounts
            
            scathaKillsText.setText(scathaPro.variables.scathaKills >= 0 ? Util.numberToString(scathaPro.variables.scathaKills) : EnumChatFormatting.OBFUSCATED + "?");
            secondaryScathaKillsText.setText(Util.numberToString(world != null ? statsType.scathaKills : 0));
        }
        else
        {
            // Scatha percentages
            
            int totalKills = scathaPro.variables.regularWormKills >= 0 && scathaPro.variables.scathaKills >= 0 ? scathaPro.variables.regularWormKills + scathaPro.variables.scathaKills : -1;
            int secondaryTotalKills = world != null ? statsType.regularWormKills + statsType.scathaKills : 0;

            float scathaPercentage = totalKills > 0 ? ((float) scathaPro.variables.scathaKills / totalKills) * 100 : -1f;
            float secondaryScathaPercentage = secondaryTotalKills > 0 ? ((float) statsType.scathaKills / secondaryTotalKills) * 100 : -1f;

            int scathaPercentageDecimalDigits = scathaPro.getConfig().getInt(Config.Key.scathaPercentageDecimalDigits);
            
            String scathaPercentageString = scathaPercentage >= 100f ? "100" : (scathaPercentage <= 0f ? "0" : Util.numberToString(scathaPercentage, scathaPercentageDecimalDigits, true));
            String secondaryScathaPercentageString = secondaryScathaPercentage >= 100f ? "100" : (secondaryScathaPercentage <= 0f ? "0" : Util.numberToString(secondaryScathaPercentage, scathaPercentageDecimalDigits, true));
            scathaKillsText.setText(totalKills >= 0 ? (scathaPercentage >= 0 ? scathaPercentageString : EnumChatFormatting.OBFUSCATED + "?" + EnumChatFormatting.RESET) + "%" : EnumChatFormatting.OBFUSCATED + "?");
            secondaryScathaKillsText.setText((secondaryScathaPercentage >= 0 ? TextUtil.contrastableGray() + secondaryScathaPercentageString : EnumChatFormatting.OBFUSCATED + "?" + EnumChatFormatting.RESET) + "%");
        }
        
        updateTotalKills();
        updateScathaKillsSinceLastDrop();
    }
    
    public void updateTotalKills()
    {
        World world = mc.theWorld;
        
        int secondaryTotalKills = world != null ? statsType.regularWormKills + statsType.scathaKills : 0;
        int totalKills = scathaPro.variables.regularWormKills >= 0 && scathaPro.variables.scathaKills >= 0 ? scathaPro.variables.regularWormKills + scathaPro.variables.scathaKills : -1;
        
        EnumChatFormatting contrastableGray = TextUtil.contrastableGray();

        String scathaPercentageText = "";
        String secondaryScathaPercentageText = "";
        if (scathaPro.getConfig().getBoolean(Config.Key.scathaPercentageAlternativePosition))
        {
            float scathaPercentage = totalKills > 0 ? ((float) scathaPro.variables.scathaKills / totalKills) * 100 : -1f;
            float secondaryScathaPercentage = secondaryTotalKills > 0 ? ((float) statsType.scathaKills / secondaryTotalKills) * 100 : -1f;
            
            int scathaPercentageDecimalDigits = scathaPro.getConfig().getInt(Config.Key.scathaPercentageDecimalDigits);
            
            String scathaPercentageString = scathaPercentage >= 100f ? "100" : (scathaPercentage <= 0f ? "0" : Util.numberToString(scathaPercentage, scathaPercentageDecimalDigits, true));
            String secondaryScathaPercentageString = secondaryScathaPercentage >= 100f ? "100" : (secondaryScathaPercentage <= 0f ? "0" : Util.numberToString(secondaryScathaPercentage, scathaPercentageDecimalDigits, true));
            if (scathaPercentage >= 0) scathaPercentageText = contrastableGray + " (" + scathaPercentageString + "%)";
            if (secondaryScathaPercentage >= 0) secondaryScathaPercentageText = contrastableGray + " (" + secondaryScathaPercentageString + "%)";
        }
        
        totalKillsText.setText(totalKills >= 0 ? EnumChatFormatting.RESET + Util.numberToString(totalKills) + scathaPercentageText : EnumChatFormatting.OBFUSCATED + "?");
        secondaryTotalKillsText.setText(EnumChatFormatting.RESET + Util.numberToString(secondaryTotalKills) + secondaryScathaPercentageText);
    }
    
    public void updateWormStreak()
    {
        int scathaSpawnStreak = statsType.scathaSpawnStreak;
        String scathaString = scathaPro.isScappaModeActive() ? "Scappa" : "Scatha";
        wormStreakText.setText(
            scathaSpawnStreak != 0
            ? (
                scathaSpawnStreak > 0
                ? scathaString + " spawn streak: " + Util.numberToString(scathaSpawnStreak)
                : "No " + scathaString + " for " + Util.numberToString(-scathaSpawnStreak) + " " + (-scathaSpawnStreak == 1 ? "spawn" : "spawns")
            )
            : "No worms spawned yet"
        );
    }
    
    public void updateLobbyTime()
    {
        World world = mc.theWorld;
        
        long worldTime = world != null ? world.getWorldTime() : -1L;
        int worldDay = worldTime >= 0L ? (int) Math.floor(worldTime / 24000f) : -1;
        float worldDayProgress = worldTime >= 0L ? (worldTime % 24000f) / 24000f : -1f;
        
        long lobbyTime = world != null && scathaPro.isInCrystalHollows() ? TimeUtil.now() - scathaPro.variables.lastWorldJoinTime : 0L;
        SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
        timerFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        EnumChatFormatting contrastableGray = TextUtil.contrastableGray();
        timeText.setText(EnumChatFormatting.RESET.toString() + "Day " + (worldDay >= 0 ? worldDay : EnumChatFormatting.OBFUSCATED + "0") + EnumChatFormatting.RESET + " " + contrastableGray + "(" + (worldDayProgress >= 0 ? Util.numberToString(worldDayProgress * 100f, 0, false, RoundingMode.DOWN) : EnumChatFormatting.OBFUSCATED + "0") + EnumChatFormatting.RESET + contrastableGray + "%) / " + timerFormat.format(lobbyTime));
    }
    
    public void updateCoords()
    {
        // min has to be extended to start counting from the right side of the wall, due to how block positions vs decimal number positions work
        int boundsMinExtended = Constants.crystalHollowsBoundsMin + 1;
        double wallLength = Constants.crystalHollowsBoundsMax - boundsMinExtended;
        
        String coordinatesString;
        String facingAxis;
        double wallProgress;
        
        EntityPlayer player = mc.thePlayer;
        if (player != null)
        {
            BlockPos blockPos = Util.entityBlockPos(player);
            coordinatesString = blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
            
            int facing = Util.getDirection(player);
            double wallDistance = 0D;
            
            switch (facing)
            {
                case 0:
                    facingAxis = "-Z";
                    wallDistance = Constants.crystalHollowsBoundsMax - player.posZ;
                    break;
                case 1:
                    facingAxis = "+X";
                    wallDistance = player.posX - boundsMinExtended;
                    break;
                case 2:
                    facingAxis = "+Z";
                    wallDistance = player.posZ - boundsMinExtended;
                    break;
                case 3:
                    facingAxis = "-X";
                    wallDistance = Constants.crystalHollowsBoundsMax - player.posX;
                    break;
                default:
                    facingAxis = "?";
                    wallDistance = 0;
            }
            
            wallProgress = Math.min(Math.max((wallDistance - 1D) / (wallLength - 2D), 0D), 1D);
        }
        else
        {
            coordinatesString = "0 0 0";
            facingAxis = "+Z";
            wallProgress = -1D;
        }
        
        String wallProgressString;
        if (wallProgress >= 0D)
        {
            float roundedWallProgress = (float) (Math.floor(wallProgress * 100 * 10) / 10f);
            wallProgressString = roundedWallProgress >= 100f ? "100" : (roundedWallProgress <= 0f ? "0" : Util.numberToString(roundedWallProgress, 1, true));
        }
        else wallProgressString = EnumChatFormatting.OBFUSCATED + "0";

        EnumChatFormatting contrastableGray = TextUtil.contrastableGray();
        coordsText.setText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE + coordinatesString + contrastableGray + " / " + EnumChatFormatting.WHITE + facingAxis + contrastableGray + " (" + wallProgressString + EnumChatFormatting.RESET + contrastableGray + "% to wall)");
    }
    
    public void updateScathaKillsSinceLastDrop()
    {
        String killsText;
        if (scathaPro.variables.scathaKills >= 0 && scathaPro.variables.scathaKillsAtLastDrop >= 0)
        {
            int scathaKillsSinceLastDrop = scathaPro.variables.scathaKills - scathaPro.variables.scathaKillsAtLastDrop;
            if (scathaKillsSinceLastDrop >= 0)
            {
                killsText = Integer.toString(scathaKillsSinceLastDrop);
            }
            else killsText = "-" + EnumChatFormatting.OBFUSCATED + "?";
        }
        else killsText = EnumChatFormatting.OBFUSCATED + "?";
        
        scathaKillsSinceLastDropText.setText(EnumChatFormatting.RESET + (scathaPro.isScappaModeActive() ? "Scappas" : "Scathas") + " since last pet drop: " + killsText);
    }
    
    public void updateSpawnCooldown()
    {
        long cooldownTimer = TimeUtil.now() - scathaPro.variables.wormSpawnCooldownStartTime;
        float progress;
        
        if (scathaPro.variables.wormSpawnCooldownStartTime >= 0f && (progress = 1f - (cooldownTimer / (float) Constants.wormSpawnCooldown)) > 0f)
        {
            spawnCooldownProgressBar.setVisible(true);
            spawnCooldownProgressBar.setProgress(progress);
            spawnCooldownTimerText.setText(EnumChatFormatting.YELLOW + "Spawn cooldown: " + TimeUtil.getHMSTimeString(Constants.wormSpawnCooldown - cooldownTimer, true));
        }
        else
        {
            spawnCooldownProgressBar.setVisible(false);
            spawnCooldownTimerText.setText(EnumChatFormatting.GREEN + "Worms ready to spawn");
        }
    }
    
    public void updateAnomalousDesire()
    {
        final String textPrefix = EnumChatFormatting.GOLD + "Anomalous Desire " + EnumChatFormatting.RESET;
        
        long now = TimeUtil.now();
        long anomalousDesireElapsedTime;
        
        boolean showProgressBar = false;
        float progress = 0f;
        if (
            scathaPro.variables.anomalousDesireStartTime >= 0f
            && (progress = 1f - ((anomalousDesireElapsedTime = now - scathaPro.variables.anomalousDesireStartTime) / (float) Constants.anomalousDesireEffectDuration)) > 0f
        )
        {
            if (scathaPro.variables.wormSpawnCooldownStartTime < 0L || now - scathaPro.variables.wormSpawnCooldownStartTime >= Constants.wormSpawnCooldown)
            {
                showProgressBar = true;
            }
            
            anomalousDesireStatusText.setText(textPrefix + EnumChatFormatting.YELLOW + "active: " + TimeUtil.getHMSTimeString(Constants.anomalousDesireEffectDuration - anomalousDesireElapsedTime, true));
        }
        else
        {
            if (scathaPro.variables.anomalousDesireCooldownEndTime >= 0L && now < scathaPro.variables.anomalousDesireCooldownEndTime)
            {
                anomalousDesireStatusText.setText(textPrefix + EnumChatFormatting.RED + "on c.d.: " + TimeUtil.getHMSTimeString(scathaPro.variables.anomalousDesireCooldownEndTime - now, true));
            }
            else
            {
                anomalousDesireStatusText.setText(textPrefix + EnumChatFormatting.GREEN + "ready");
            }
        }
        
        if (showProgressBar)
        {
            anomalousDesireEffectProgressBar.setVisible(true);
            anomalousDesireEffectProgressBar.setProgress(progress);
        }
        else anomalousDesireEffectProgressBar.setVisible(false);
    }
    
    public void updateTimeSinceLastWormSpawn()
    {
        String timeString;
        if (scathaPro.variables.lastWormSpawnTime >= 0L && mc.theWorld != null)
        {
            timeString = TimeUtil.getHMSTimeString(TimeUtil.now() - scathaPro.variables.lastWormSpawnTime, false);
        }
        else timeString = EnumChatFormatting.OBFUSCATED + "?";
        wormSpawnTimerText.setText(EnumChatFormatting.RESET + "Time since last spawn: " + timeString);
    }
    
    public void updateRealTimeClock()
    {
        EnumChatFormatting contrastableGray = TextUtil.contrastableGray();
        String clockString = DateTimeFormatter.ofPattern("HH:mm'" + contrastableGray + "':ss", Locale.ENGLISH).format(LocalDateTime.now());
        realTimeClockText.setText(contrastableGray + "Clock: " + EnumChatFormatting.RESET + clockString);
    }
}
