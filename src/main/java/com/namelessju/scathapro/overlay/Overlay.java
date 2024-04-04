package com.namelessju.scathapro.overlay;

import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.events.OverlayInitEvent;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.OverlayStats;
import com.namelessju.scathapro.overlay.elements.DynamicOverlayContainer;
import com.namelessju.scathapro.overlay.elements.OverlayContainer;
import com.namelessju.scathapro.overlay.elements.OverlayElement;
import com.namelessju.scathapro.overlay.elements.OverlayImage;
import com.namelessju.scathapro.overlay.elements.OverlayProgressBar;
import com.namelessju.scathapro.overlay.elements.OverlayText;
import com.namelessju.scathapro.overlay.elements.DynamicOverlayContainer.Direction;
import com.namelessju.scathapro.overlay.elements.OverlayElement.Alignment;
import com.namelessju.scathapro.util.JsonUtil;
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
    
    
    private final DynamicOverlayContainer mainContainer;
    
    private final OverlayImage scathaIcon;
    private final OverlayText regularWormKillsText;
    private final OverlayText secondaryRegularWormKillsText;
    private final OverlayText scathaKillsText;
    private final OverlayText secondaryScathaKillsText;
    private final OverlayProgressBar spawnCooldownProgressBar;
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
    private final OverlayText wormSpawnTimerText;
    
    
    public final List<ToggleableOverlayElement> toggleableOverlayElements = Lists.<ToggleableOverlayElement>newArrayList();
    
    
    private OverlayStats statsType = OverlayStats.PER_LOBBY;
    public final boolean easterEggTitleActive;
    
    
    public Overlay(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.getMinecraft();
        
        
        String currentTypeId = scathaPro.getConfig().getString(Config.Key.statsType);
        if (!currentTypeId.replace("", " ").isEmpty())
        {
            for (OverlayStats type : OverlayStats.values())
            {
                if (type.getOptionValue().equals(currentTypeId)) statsType = type;
            }
        }
        if (statsType == null) statsType = OverlayStats.values()[0];
        
        easterEggTitleActive = new Random().nextFloat() < 0.005f;
        
        
        mainContainer = new DynamicOverlayContainer(0, 0, 1f, Direction.VERTICAL);
        mainContainer.padding = 5;
        mainContainer.backgroundColor = 0x50000000;
        
        
        OverlayContainer headerContainer = new OverlayContainer(0, 0, 1f).setMargin(0, 5);
        headerContainer.add(scathaIcon = new OverlayImage(null, 256, 256, 0, 0, 0.043f));
        headerContainer.add(new OverlayText(easterEggTitleActive ? EnumChatFormatting.LIGHT_PURPLE + "Scappa Farming:" : "Scatha Farming:", Util.Color.GOLD.getValue(), 16, 0, 1.3f));
        mainContainer.add(headerContainer);
        addToggleableElement("header", "Header", headerContainer);
        
        
        DynamicOverlayContainer countersContainer = new DynamicOverlayContainer(0, 0, 1f, Direction.HORIZONTAL).setMargin(0, 4);
        
        
        OverlayContainer petDropsContainer = new OverlayContainer(0, 0, 1f);
        petDropsContainer.add(new OverlayText("Pets", Util.Color.GREEN.getValue(), 0, 0, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet.png", 256, 256, 0, 11, 0.033f));
        petDropsContainer.add(rarePetDropsText = new OverlayText(null, Util.Color.BLUE.getValue(), 12, 11, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet.png", 256, 256, 0, 22, 0.033f));
        petDropsContainer.add(epicPetDropsText = new OverlayText(null, Util.Color.DARK_PURPLE.getValue(), 12, 22, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet.png", 256, 256, 0, 33, 0.033f));
        petDropsContainer.add(legendaryPetDropsText = new OverlayText(null, Util.Color.GOLD.getValue(), 12, 33, 1f));
        countersContainer.add(petDropsContainer);
        addToggleableElement("petDrops", "Pet Drop Counters", petDropsContainer);
        
        
        OverlayContainer killsContainer = new OverlayContainer(8, 0, 1f);
        
        killsContainer.add(spawnCooldownProgressBar = new OverlayProgressBar(0, 10, 77, 21, 1f, 0x50FFFFFF, -1));
        
        killsContainer.add(new OverlayText("Worms", Util.Color.YELLOW.getValue(), 15, 0, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(new OverlayImage("overlay/worm.png", 512, 256, -5, 10, 0.08f));
        killsContainer.add(regularWormKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 15, 11, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(secondaryRegularWormKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 15, 22, 1f).setAlignment(Alignment.CENTER));
        
        killsContainer.add(new OverlayText("Scathas", Util.Color.YELLOW.getValue(), 58, 0, 1f).setAlignment(Alignment.CENTER));
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
        
        
        mainContainer.add(wormSpawnTimerText = new OverlayText(null, Util.Color.GRAY.getValue(), 0, 2, 1f));
        addToggleableElement("timeSinceWormSpawn", "Time Since Last Spawn", wormSpawnTimerText, false);
        
        mainContainer.add(spawnCooldownTimerText = new OverlayText(null, Util.Color.GRAY.getValue(), 0, 2, 1f));
        addToggleableElement("spawnCooldownTimer", "Spawn Cooldown Timer", spawnCooldownTimerText, false);
        
        mainContainer.add(scathaKillsSinceLastDropText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 2, 1f));
        addToggleableElement("scathaKillsSinceLastPetDrop", "Scathas Since Pet Drop", scathaKillsSinceLastDropText);
        
        mainContainer.add(timeText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 2, 1f));
        addToggleableElement("time", "Time Display", timeText);
        
        mainContainer.add(coordsText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 2, 1f));
        addToggleableElement("coords", "Coordinates Display", coordsText);
        

        MinecraftForge.EVENT_BUS.post(new OverlayInitEvent(mainContainer));
        
        
        updateVisibility();
        
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
        boolean overlayVisible = scathaPro.getConfig().getBoolean(Config.Key.overlay);
        scathaPro.getConfig().set(Config.Key.overlay, !overlayVisible);
        scathaPro.getConfig().save();
        updateVisibility();
        return !overlayVisible;
    }
    
    public void setStatsType(OverlayStats statsType)
    {
        this.statsType = statsType;
        
        updateWormKills();
        updateScathaKills();
        updateTotalKills();
        updateWormStreak();
    }

    public void drawOverlay()
    {
        updateRealtimeElements();
        mainContainer.draw();
    }
    
    public void drawOverlay(int x, int y, Alignment alignment)
    {
        if (!mainContainer.isVisible()) return;
        
        updateRealtimeElements();
        
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        mainContainer.draw(false, alignment);
        GlStateManager.popMatrix();
    }
    
    public void updateRealtimeElements()
    {
        if (!mainContainer.isVisible()) return;

        updateTimeSinceLastWormSpawn();
        updateSpawnCooldown();
        updateCoords();
        updateTime();
        updatePosition();
    }
    
    public void updateOverlayFull()
    {
        updateScathaPetImage();

        updateWormKills();
        updateScathaKills();
        updateTotalKills();
        
        updateWormStreak();

        updateSpawnCooldown();
        updateTimeSinceLastWormSpawn();
        
        updateCoords();
        updateTime();
        
        updatePetDrops();
        
        updatePosition();
        updateScale();
        updateVisibility();
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
    
    public void updateScale()
    {
        mainContainer.setScale((float) scathaPro.getConfig().getDouble(Config.Key.overlayScale));
    }
    
    public void updateVisibility()
    {
        mainContainer.setVisible(scathaPro.getConfig().getBoolean(Config.Key.overlay));
    }
    
    public void updateScathaPetImage()
    {
        scathaIcon.setImage(scathaPro.getAlertModeManager().getCurrentMode().getIconPath(), 256, 256);
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
        
        secondaryScathaKillsText.setText(Util.numberToString(world != null ? statsType.scathaKills : 0));
        scathaKillsText.setText(scathaPro.variables.scathaKills >= 0 ? Util.numberToString(scathaPro.variables.scathaKills) : EnumChatFormatting.OBFUSCATED + "?");
        
        updateTotalKills();
        updateScathaKillsSinceLastDrop();
    }
    
    public void updateTotalKills()
    {
        World world = mc.theWorld;
        
        int secondaryTotalKills = world != null ? statsType.regularWormKills + statsType.scathaKills : 0;
        int totalKills = scathaPro.variables.regularWormKills >= 0 && scathaPro.variables.scathaKills >= 0 ? scathaPro.variables.regularWormKills + scathaPro.variables.scathaKills : -1;
        
        float secondaryScathaPercentage = secondaryTotalKills > 0 ? ((float) statsType.scathaKills / secondaryTotalKills) * 100 : -1f;
        float scathaPercentage = totalKills > 0 ? ((float) scathaPro.variables.scathaKills / totalKills) * 100 : -1f;
        
        int scathaPercentageDecimalDigits = scathaPro.getConfig().getInt(Config.Key.scathaPercentageDecimalDigits);
        
        secondaryTotalKillsText.setText(EnumChatFormatting.RESET + Util.numberToString(secondaryTotalKills) + (secondaryScathaPercentage >= 0 ? EnumChatFormatting.GRAY + " (" + Util.numberToString(secondaryScathaPercentage, scathaPercentageDecimalDigits, true) + "%)" : ""));
        totalKillsText.setText(totalKills >= 0 ? EnumChatFormatting.RESET + Util.numberToString(totalKills) + (scathaPercentage >= 0 ? EnumChatFormatting.GRAY + " (" + Util.numberToString(scathaPercentage, scathaPercentageDecimalDigits, true) + "%)" : "") : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateWormStreak()
    {
        int scathaSpawnStreak = statsType.scathaSpawnStreak;
        wormStreakText.setText(
            scathaSpawnStreak != 0
            ? (
                scathaSpawnStreak > 0
                ? "Scatha spawn streak: " + Util.numberToString(scathaSpawnStreak)
                : "No Scatha for " + Util.numberToString(-scathaSpawnStreak) + " " + (-scathaSpawnStreak == 1 ? "spawn" : "spawns")
            )
            : "No worms spawned yet"
        );
    }
    
    public void updateTime()
    {
        World world = mc.theWorld;
        
        long worldTime = world != null ? world.getWorldTime() : 0;
        int worldDay = world != null ? (int) Math.floor(worldTime / 24000f) : 0;
        long lobbyTime = world != null && scathaPro.isInCrystalHollows() ? Util.getCurrentTime() - scathaPro.variables.lastWorldJoinTime : 0L;
        SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
        timerFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        int worldTimeDay = (int) ((worldTime % 24000f) % 24000f);
        int worldTimeHours = (int) Math.floor(worldTimeDay / 1000f);
        int worldTimeMinutes = (int) Math.floor((worldTimeDay - worldTimeHours * 1000f) / 1000f * 60f);
        
        /*
        EnumChatFormatting dayColor = EnumChatFormatting.WHITE;
        if (worldDay >= Constants.crystalHollowsCloseDay - 1 && scathaPro.isInCrystalHollows())
        {
            if (worldDay >= Constants.crystalHollowsCloseDay) dayColor = EnumChatFormatting.DARK_RED;
            else dayColor = EnumChatFormatting.RED;
        }
        */
        
        timeText.setText(EnumChatFormatting.RESET.toString() + "Day " + worldDay + " " + EnumChatFormatting.GRAY + "(" + String.format("%02d:%02d", worldTimeHours, worldTimeMinutes) + ") / " + timerFormat.format(lobbyTime));
    }
    
    public void updateCoords()
    {
        double wallLength = Constants.crystalHollowsBoundsMax - Constants.crystalHollowsBoundsMin;

        String coordinatesString;
        String facingAxis;
        double wallProgress;
        
        EntityPlayer player = mc.thePlayer;
        if (player != null)
        {
            BlockPos blockPos = Util.entityBlockPos(player);
            coordinatesString = blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
            
            int facing = Util.getFacing(player);
            double wallDistance = 0D;
            
            switch (facing)
            {
                case 0:
                    facingAxis = "-Z";
                    wallDistance = Constants.crystalHollowsBoundsMax - player.posZ;
                    break;
                case 1:
                    facingAxis = "+X";
                    wallDistance = player.posX - Constants.crystalHollowsBoundsMin;
                    break;
                case 2:
                    facingAxis = "+Z";
                    wallDistance = player.posZ - Constants.crystalHollowsBoundsMin;
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
            wallProgress = 0D;
        }
        
        float roundedWallProgress = (float) (Math.floor(wallProgress * 100 * 10) / 10f);
        String wallProgressString = roundedWallProgress >= 100f ? "100" : (roundedWallProgress <= 0f ? "0" : Util.numberToString(roundedWallProgress, 1, true));
        
        coordsText.setText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE + coordinatesString + EnumChatFormatting.GRAY + " / " + EnumChatFormatting.WHITE + facingAxis + EnumChatFormatting.GRAY + " (" + wallProgressString + "% to wall)");
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
            else killsText = EnumChatFormatting.OBFUSCATED + "???"; // 3 obfuscated characters to indicate that the difference is negative
        }
        else killsText = EnumChatFormatting.OBFUSCATED + "?";
        
        scathaKillsSinceLastDropText.setText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + "Scathas since last pet drop: " + killsText);
    }
    
    public void updateSpawnCooldown()
    {
        double cooldownTimer = Util.getCurrentTime() - scathaPro.variables.wormSpawnCooldownStartTime;
        
        if (scathaPro.variables.wormSpawnCooldownStartTime >= 0f)
        {
            spawnCooldownProgressBar.setProgress(1f - ((float) cooldownTimer / Constants.wormSpawnCooldown));
            spawnCooldownTimerText.setText("Spawn cooldown: " + Util.numberToString((Constants.wormSpawnCooldown - cooldownTimer) / 1000D, 1, true, RoundingMode.CEILING) + "s");
        }
        else
        {
            spawnCooldownProgressBar.setProgress(0f);
            spawnCooldownTimerText.setText("Worms ready to spawn");
        }
    }
    
    public void updateTimeSinceLastWormSpawn()
    {
        String timeString;
        if (scathaPro.variables.lastWormSpawnTime >= 0L)
        {
            double timeSinceLastWormSpawn = (Util.getCurrentTime() - scathaPro.variables.lastWormSpawnTime) / 1000D;
            timeString = Util.numberToString(timeSinceLastWormSpawn, 1, true, RoundingMode.FLOOR) + "s";
        }
        else timeString = "-";
        wormSpawnTimerText.setText("Time since last spawn: " + timeString);
    }

}
