package com.namelessju.scathapro.overlay;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.events.OverlayInitEvent;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.OverlayStatsType;
import com.namelessju.scathapro.overlay.elements.DynamicOverlayContainer;
import com.namelessju.scathapro.overlay.elements.OverlayContainer;
import com.namelessju.scathapro.overlay.elements.OverlayElement;
import com.namelessju.scathapro.overlay.elements.OverlayImage;
import com.namelessju.scathapro.overlay.elements.OverlayProgressBar;
import com.namelessju.scathapro.overlay.elements.OverlayText;
import com.namelessju.scathapro.overlay.elements.DynamicOverlayContainer.Direction;
import com.namelessju.scathapro.overlay.elements.OverlayElement.Alignment;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class Overlay
{
    public static class ToggleableOverlayElement
    {
        public final OverlayElement element;
        
        public ToggleableOverlayElement(OverlayElement element)
        {
            this.element = element;
        }
        
        public void toggle()
        {
            element.setVisible(!element.isVisible());
        }
    }
    
    private final ScathaPro scathaPro;
    private final Minecraft mc;
    
    private final DynamicOverlayContainer mainContainer;
    
    private final OverlayImage scathaIcon;
    private final OverlayContainer killsContainer;
    private final OverlayText regularWormKillsText;
    private final OverlayText secondaryRegularWormKillsText;
    private final OverlayText scathaKillsText;
    private final OverlayText secondaryScathaKillsText;
    private final OverlayProgressBar spawnCooldownProgressBar;
    private final OverlayText totalKillsText;
    private final OverlayText secondaryTotalKillsText;
    private final OverlayText wormStreakText;
    private final OverlayText coordsText;
    private final OverlayText dayText;
    private final OverlayText rarePetDropsText;
    private final OverlayText epicPetDropsText;
    private final OverlayText legendaryPetDropsText;
    private final OverlayText scathaKillsSinceLastDropText;
    
    
    public final List<ToggleableOverlayElement> toggleableOverlayElements = Lists.<ToggleableOverlayElement>newArrayList();
    
    private OverlayStatsType statsType = null;
    
    
    public Overlay(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.getMinecraft();
        
        
        String currentTypeId = scathaPro.getConfig().getString(Config.Key.statsType);
        if (!currentTypeId.replace("", " ").isEmpty())
        {
            for (OverlayStatsType type : OverlayStatsType.values())
            {
                if (type.getValue().equals(currentTypeId)) statsType = type;
            }
        }
        if (statsType == null) statsType = OverlayStatsType.values()[0];
        
        
        mainContainer = new DynamicOverlayContainer(0, 0, 1f, Direction.VERTICAL);
        mainContainer.padding = 5;
        mainContainer.backgroundColor = 0x50000000;
        
        
        OverlayContainer titleContainer = new OverlayContainer(0, 0, 1f);
        titleContainer.add(scathaIcon = new OverlayImage(null, 256, 256, 0, 0, 0.043f));
        titleContainer.add(new OverlayText(new Random().nextFloat() < 0.001f ? "Scathing:" : "Scatha Farming:", Util.Color.GOLD.getValue(), 16, 0, 1.3f));
        mainContainer.add(titleContainer);
        toggleableOverlayElements.add(new ToggleableOverlayElement(titleContainer));
        
        
        DynamicOverlayContainer countersContainer = new DynamicOverlayContainer(0, 5, 1f, Direction.HORIZONTAL);
        
        
        OverlayContainer petDropsContainer = new OverlayContainer(0, 0, 1f);
        
        petDropsContainer.add(new OverlayText("Pets", Util.Color.GREEN.getValue(), 0, 0, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet.png", 256, 256, 0, 11, 0.033f));
        petDropsContainer.add(rarePetDropsText = new OverlayText(null, Util.Color.BLUE.getValue(), 12, 11, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet.png", 256, 256, 0, 22, 0.033f));
        petDropsContainer.add(epicPetDropsText = new OverlayText(null, Util.Color.DARK_PURPLE.getValue(), 12, 22, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet.png", 256, 256, 0, 33, 0.033f));
        petDropsContainer.add(legendaryPetDropsText = new OverlayText(null, Util.Color.GOLD.getValue(), 12, 33, 1f));
        
        countersContainer.add(petDropsContainer);
        toggleableOverlayElements.add(new ToggleableOverlayElement(petDropsContainer));
        
        
        killsContainer = new OverlayContainer(5, 0, 1f);
        
        killsContainer.add(new OverlayText("Worms", Util.Color.YELLOW.getValue(), 18, 0, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(new OverlayText("Scathas", Util.Color.YELLOW.getValue(), 61, 0, 1f).setAlignment(Alignment.CENTER));
        
        killsContainer.add(new OverlayImage("overlay/worm.png", 512, 256, -2, 10, 0.08f));
        killsContainer.add(new OverlayImage("overlay/scatha.png", 512, 256, 41, 10, 0.08f));
        
        killsContainer.add(spawnCooldownProgressBar = new OverlayProgressBar(2, 10, 79, 21, 1f, 0x50FFFFFF, -1));
        
        killsContainer.add(regularWormKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 18, 11, 1f));
        regularWormKillsText.setAlignment(Alignment.CENTER);
        
        killsContainer.add(secondaryRegularWormKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 18, 22, 1f));
        secondaryRegularWormKillsText.setAlignment(Alignment.CENTER);
        
        killsContainer.add(scathaKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 61, 11, 1f));
        scathaKillsText.setAlignment(Alignment.CENTER);
        
        killsContainer.add(secondaryScathaKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 61, 22, 1f));
        secondaryScathaKillsText.setAlignment(Alignment.CENTER);

        killsContainer.add(new OverlayText("Total", Util.Color.WHITE.getValue(), 89, 0, 1f));
        
        killsContainer.add(totalKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 89, 11, 1f));
        killsContainer.add(secondaryTotalKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 89, 22, 1f));
        
        killsContainer.add(wormStreakText = new OverlayText(null, Util.Color.GRAY.getValue(), 3, 33, 1f));
        
        countersContainer.add(killsContainer);
        toggleableOverlayElements.add(new ToggleableOverlayElement(killsContainer));
        
        
        mainContainer.add(countersContainer);
        
        
        mainContainer.add(scathaKillsSinceLastDropText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 4, 1f));
        toggleableOverlayElements.add(new ToggleableOverlayElement(scathaKillsSinceLastDropText));
        
        mainContainer.add(dayText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 3, 1f));
        toggleableOverlayElements.add(new ToggleableOverlayElement(dayText));
        
        mainContainer.add(coordsText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 3, 1f));
        toggleableOverlayElements.add(new ToggleableOverlayElement(coordsText));
        

        MinecraftForge.EVENT_BUS.post(new OverlayInitEvent(mainContainer));
        
        
        updateVisibility();
    }
    
    public void setStatsType(OverlayStatsType statsType)
    {
        this.statsType = statsType;
        
        updateWormKills();
        updateScathaKills();
        updateTotalKills();
        updateWormStreak();
    }
    
    public void drawOverlay()
    {
        if (mainContainer.isVisible())
        {
            updateSpawnCooldownProgressBar();
            updateCoords();
            updateDay();
            updatePosition();
        }
        
        mainContainer.draw();
    }
    
    public void updateOverlayFull()
    {
        updateScathaPetImage();

        updateWormKills();
        updateScathaKills();
        updateTotalKills();
        
        updateWormStreak();

        updateSpawnCooldownProgressBar();
        
        updateCoords();
        updateDay();
        
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
                overlayPositionPercentage[0] >= 0 ? (int) Math.round(-mainContainer.getWidth() * overlayPositionPercentage[0]) : 0,
                overlayPositionPercentage[1] >= 0 ? (int) Math.round(-mainContainer.getHeight() * overlayPositionPercentage[1]) : 0
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
        
        secondaryRegularWormKillsText.setText(Util.numberToString(world != null ? statsType.getRegularWormKills() : 0));
        regularWormKillsText.setText(scathaPro.variables.regularWormKills >= 0 ? Util.numberToString(scathaPro.variables.regularWormKills) : EnumChatFormatting.OBFUSCATED + "?");

        updateTotalKills();
    }
    
    public void updateScathaKills()
    {
        World world = mc.theWorld;
        
        secondaryScathaKillsText.setText(Util.numberToString(world != null ? statsType.getScathaKills() : 0));
        scathaKillsText.setText(scathaPro.variables.scathaKills >= 0 ? Util.numberToString(scathaPro.variables.scathaKills) : EnumChatFormatting.OBFUSCATED + "?");
        
        updateTotalKills();
        updateScathaKillsSinceLastDrop();
    }
    
    public void updateTotalKills()
    {
        World world = mc.theWorld;
        
        int secondaryTotalKills = world != null ? statsType.getRegularWormKills() + statsType.getScathaKills() : 0;
        int totalKills = scathaPro.variables.regularWormKills >= 0 && scathaPro.variables.scathaKills >= 0 ? scathaPro.variables.regularWormKills + scathaPro.variables.scathaKills : -1;
        
        float secondaryScathaPercentage = secondaryTotalKills > 0 ? ((float) statsType.getScathaKills() / secondaryTotalKills) * 100 : -1f;
        float scathaPercentage = totalKills > 0 ? ((float) scathaPro.variables.scathaKills / totalKills) * 100 : -1f;
        
        int scathaPercentageDecimalDigits = scathaPro.getConfig().getInt(Config.Key.scathaPercentageDecimalDigits);
        
        secondaryTotalKillsText.setText(EnumChatFormatting.RESET + Util.numberToString(secondaryTotalKills) + (secondaryScathaPercentage >= 0 ? EnumChatFormatting.GRAY + " (" + Util.numberToString(secondaryScathaPercentage, scathaPercentageDecimalDigits, true) + "%)" : ""));
        totalKillsText.setText(totalKills >= 0 ? EnumChatFormatting.RESET + Util.numberToString(totalKills) + (scathaPercentage >= 0 ? EnumChatFormatting.GRAY + " (" + Util.numberToString(scathaPercentage, scathaPercentageDecimalDigits, true) + "%)" : "") : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateWormStreak()
    {
        int scathaSpawnStreak = statsType.getScathaSpawnStreak();
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
    
    public void updateDay()
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
        
        EnumChatFormatting dayColor = EnumChatFormatting.WHITE;
        if (worldDay >= Constants.crystalHollowsCloseDay - 1 && scathaPro.isInCrystalHollows())
        {
            if (worldDay >= Constants.crystalHollowsCloseDay) dayColor = EnumChatFormatting.DARK_RED;
            else dayColor = EnumChatFormatting.RED;
        }
        
        dayText.setText(EnumChatFormatting.RESET.toString() + dayColor + "Day " + worldDay + " " + EnumChatFormatting.GRAY + "(" + String.format("%02d:%02d", worldTimeHours, worldTimeMinutes) + ") / " + timerFormat.format(lobbyTime));
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
        
        coordsText.setText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE + coordinatesString + EnumChatFormatting.GRAY + " " + facingAxis + " (" + wallProgressString + "% to wall)");
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
    
    public void updateSpawnCooldownProgressBar()
    {
        if (scathaPro.variables.wormSpawnCooldownStartTime >= 0f)
        {
            spawnCooldownProgressBar.setProgress(1f - ((float) (Util.getCurrentTime() - scathaPro.variables.wormSpawnCooldownStartTime) / Constants.wormSpawnCooldown));
        }
        else spawnCooldownProgressBar.setProgress(0f);
    }

}
