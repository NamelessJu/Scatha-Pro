package com.namelessju.scathapro.managers;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import com.google.common.collect.Lists;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.events.OverlayInitEvent;
import com.namelessju.scathapro.overlay.DynamicOverlayContainer;
import com.namelessju.scathapro.overlay.DynamicOverlayContainer.Direction;
import com.namelessju.scathapro.overlay.OverlayContainer;
import com.namelessju.scathapro.overlay.OverlayElement;
import com.namelessju.scathapro.overlay.OverlayImage;
import com.namelessju.scathapro.overlay.OverlayProgressBar;
import com.namelessju.scathapro.overlay.OverlaySpacing;
import com.namelessju.scathapro.overlay.OverlayText;
import com.namelessju.scathapro.overlay.OverlayElement.Alignment;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class OverlayManager
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
    
    private final DynamicOverlayContainer overlay;
    
    private final OverlayImage scathaPetImage;
    private final OverlayContainer killsContainer;
    private final OverlayText overallWormKillsText;
    private final OverlayText regularWormKillsText;
    private final OverlayText overallScathaKillsText;
    private final OverlayText scathaKillsText;
    private final OverlayProgressBar spawnCooldownProgressBar;
    private final OverlayText overallTotalKillsText;
    private final OverlayText totalKillsText;
    private final OverlayText wormStreakText;
    private final OverlayText coordsText;
    private final OverlayText dayText;
    private final OverlayText rarePetDropsText;
    private final OverlayText epicPetDropsText;
    private final OverlayText legendaryPetDropsText;
    private final OverlayText scathaKillsSinceLastDropText;
    
    
    public final List<ToggleableOverlayElement> toggleableOverlayElements = Lists.<ToggleableOverlayElement>newArrayList();
    
    
    public OverlayManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.minecraft;
        
        overlay = new DynamicOverlayContainer(0, 0, 1f, Direction.VERTICAL);
        overlay.padding = 5;
        overlay.backgroundColor = 0x50000000;
        
        
        MinecraftForge.EVENT_BUS.post(new OverlayInitEvent.Pre(overlay));
        
        
        OverlayContainer titleContainer = new OverlayContainer(0, 0, 1f);
        titleContainer.add(scathaPetImage = new OverlayImage(null, 256, 256, 0, 0, 0.043f));
        titleContainer.add(new OverlayText(new Random().nextFloat() < 0.001f ? "Scathing:" : "Scatha Farming:", Util.Color.GOLD.getValue(), 16, 0, 1.3f));
        overlay.add(titleContainer);
        toggleableOverlayElements.add(new ToggleableOverlayElement(titleContainer));
        
        overlay.add(new OverlaySpacing(0, 5));
        
        
        DynamicOverlayContainer countersContainer = new DynamicOverlayContainer(0, 0, 1f, Direction.HORIZONTAL);
        
        
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

        
        countersContainer.add(new OverlaySpacing(5, 0));
        
        
        killsContainer = new OverlayContainer(0, 0, 1f);
        
        killsContainer.add(new OverlayText("Worms", Util.Color.YELLOW.getValue(), 18, 0, 1f).setAlignment(Alignment.CENTER));
        killsContainer.add(new OverlayText("Scathas", Util.Color.YELLOW.getValue(), 61, 0, 1f).setAlignment(Alignment.CENTER));
        
        killsContainer.add(new OverlayImage("overlay/worm.png", 512, 256, -2, 10, 0.08f));
        killsContainer.add(new OverlayImage("overlay/scatha.png", 512, 256, 41, 10, 0.08f));

        killsContainer.add(spawnCooldownProgressBar = new OverlayProgressBar(2, 10, 79, 21, 1f, 0x50FFFFFF, -1));
        
        killsContainer.add(overallWormKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 18, 11, 1f));
        overallWormKillsText.setAlignment(Alignment.CENTER);
        
        killsContainer.add(regularWormKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 18, 22, 1f));
        regularWormKillsText.setAlignment(Alignment.CENTER);
        
        killsContainer.add(overallScathaKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 61, 11, 1f));
        overallScathaKillsText.setAlignment(Alignment.CENTER);
        
        killsContainer.add(scathaKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 61, 22, 1f));
        scathaKillsText.setAlignment(Alignment.CENTER);

        killsContainer.add(new OverlayText("Total", Util.Color.WHITE.getValue(), 89, 0, 1f));
        
        killsContainer.add(overallTotalKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 89, 11, 1f));
        killsContainer.add(totalKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 89, 22, 1f));
        
        killsContainer.add(wormStreakText = new OverlayText(null, Util.Color.GRAY.getValue(), 3, 33, 1f));
        
        countersContainer.add(killsContainer);
        toggleableOverlayElements.add(new ToggleableOverlayElement(killsContainer));
        
        
        overlay.add(countersContainer);

        
        overlay.add(scathaKillsSinceLastDropText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 3, 1f));
        toggleableOverlayElements.add(new ToggleableOverlayElement(scathaKillsSinceLastDropText));
        
        overlay.add(dayText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 3, 1f));
        toggleableOverlayElements.add(new ToggleableOverlayElement(dayText));
        
        overlay.add(coordsText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 3, 1f));
        toggleableOverlayElements.add(new ToggleableOverlayElement(coordsText));
        

        MinecraftForge.EVENT_BUS.post(new OverlayInitEvent.Post(overlay));
        
        
        updateVisibility();
    }
    
    public void drawOverlay()
    {
        if (overlay.isVisible())
        {
            updateSpawnCooldownProgressBar();
            updateCoords();
            updateDay();
            updatePosition();
        }
        
        overlay.draw();
    }
    
    public void updateOverlayFull()
    {
        updateScathaPetImage();

        updateWormKills();
        updateScathaKills();

        updateSpawnCooldownProgressBar();

        updateWormStreak();
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
    
        final double[] overlayPositionPercentage = {scathaPro.config.getDouble(Config.Key.overlayX), scathaPro.config.getDouble(Config.Key.overlayY)};
        final int[] overlayPosition = {
                overlayPositionPercentage[0] >= 0 ? (int) Math.round(scaledResolution.getScaledWidth() * overlayPositionPercentage[0]) : 10,
                overlayPositionPercentage[1] >= 0 ? (int) Math.round(scaledResolution.getScaledHeight() * overlayPositionPercentage[1]) : 10,
        };
        final int[] translation = {
                overlayPositionPercentage[0] >= 0 ? (int) Math.round(-overlay.getWidth() * overlayPositionPercentage[0]) : 0,
                overlayPositionPercentage[1] >= 0 ? (int) Math.round(-overlay.getHeight() * overlayPositionPercentage[1]) : 0
        };
        overlay.setPosition(overlayPosition[0] + translation[0], overlayPosition[1] + translation[1]);
    
        /*
        if (overlayPositionPercentage[0] == 0.5) overlay.setContentAlignment(OverlayElement.Alignment.CENTER);
        else if (overlayPositionPercentage[0] > 0.5) overlay.setContentAlignment(OverlayElement.Alignment.RIGHT);
        else overlay.setContentAlignment(OverlayElement.Alignment.LEFT);
        */
    }
    
    public void updateScale()
    {
        overlay.setScale((float) scathaPro.config.getDouble(Config.Key.overlayScale));
    }
    
    public void updateVisibility()
    {
        overlay.setVisible(scathaPro.config.getBoolean(Config.Key.overlay));
    }
    
    public void updateScathaPetImage()
    {
        scathaPetImage.setImage(scathaPro.alertModeManager.getCurrentMode().getIconPath(), 256, 256);
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
        
        regularWormKillsText.setText(Util.numberToString(world != null ? scathaPro.variables.lobbyRegularWormKills : 0));
        overallWormKillsText.setText(scathaPro.variables.regularWormKills >= 0 ? Util.numberToString(scathaPro.variables.regularWormKills) : EnumChatFormatting.OBFUSCATED + "?");

        updateTotalKills();
    }
    
    public void updateScathaKills()
    {
        World world = mc.theWorld;
        
        scathaKillsText.setText(Util.numberToString(world != null ? scathaPro.variables.lobbyScathaKills : 0));
        overallScathaKillsText.setText(scathaPro.variables.scathaKills >= 0 ? Util.numberToString(scathaPro.variables.scathaKills) : EnumChatFormatting.OBFUSCATED + "?");

        updateTotalKills();
        updateScathaKillsSinceLastDrop();
    }
    
    private void updateTotalKills()
    {
        World world = mc.theWorld;

        int totalKills = world != null ? scathaPro.variables.lobbyRegularWormKills + scathaPro.variables.lobbyScathaKills : 0;
        int overallTotalKills = scathaPro.variables.regularWormKills >= 0 && scathaPro.variables.scathaKills >= 0 ? scathaPro.variables.regularWormKills + scathaPro.variables.scathaKills : -1;
        
        float percentage = totalKills > 0 ? ((float) scathaPro.variables.lobbyScathaKills / totalKills) * 100 : -1f;
        float overallPercentage = overallTotalKills > 0 ? ((float) scathaPro.variables.scathaKills / overallTotalKills) * 100 : -1f;

        totalKillsText.setText(EnumChatFormatting.RESET + Util.numberToString(totalKills) + (percentage >= 0 ? EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + " (" + Util.numberToString(percentage, 1, true) + "%)" : ""));
        overallTotalKillsText.setText(overallTotalKills >= 0 ? EnumChatFormatting.RESET + Util.numberToString(overallTotalKills) + (overallPercentage >= 0 ? EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + " (" + Util.numberToString(overallPercentage, 1, true) + "%)" : "") : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateWormStreak()
    {
        wormStreakText.setText(
                scathaPro.variables.scathaSpawnStreak != 0
                ? (
                        scathaPro.variables.scathaSpawnStreak > 0
                        ? "Scatha spawn streak: " + Util.numberToString(scathaPro.variables.scathaSpawnStreak)
                        : "No Scatha for " + Util.numberToString(-scathaPro.variables.scathaSpawnStreak) + " " + (-scathaPro.variables.scathaSpawnStreak == 1 ? "spawn" : "spawns")
                )
                : "No worms spawned yet"
        );
    }
    
    public void updateDay()
    {
        World world = mc.theWorld;
        
        long worldTime = world != null ? world.getWorldTime() : 0;
        int worldDay = world != null ? (int) Math.floor(worldTime / 24000f) : 0;
        long lobbyTime = world != null && scathaPro.inCrystalHollows() ? Util.getCurrentTime() - scathaPro.variables.lastWorldJoinTime : 0L;
        SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
        timerFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        int worldTimeDay = (int) ((worldTime % 24000f) % 24000f);
        int worldTimeHours = (int) Math.floor(worldTimeDay / 1000f);
        int worldTimeMinutes = (int) Math.floor((worldTimeDay - worldTimeHours * 1000f) / 1000f * 60f);
        
        EnumChatFormatting dayColor = EnumChatFormatting.WHITE;
        if (worldDay >= 29 && scathaPro.inCrystalHollows())
        {
            if (worldDay >= 30) dayColor = EnumChatFormatting.DARK_RED;
            else dayColor = EnumChatFormatting.RED;
        }
        
        dayText.setText(EnumChatFormatting.RESET.toString() + dayColor + "Day " + worldDay + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(" + String.format("%02d:%02d", worldTimeHours, worldTimeMinutes) + ")" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + " / " + timerFormat.format(lobbyTime));
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
        
        coordsText.setText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE + coordinatesString + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + facingAxis + " (" + Util.numberToString(Math.floor(wallProgress * 1000D) / 10D, 1) + "% to wall)");
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
        if (scathaPro.variables.lastWormSpawnTime >= 0f)
        {
            spawnCooldownProgressBar.setProgress(1f - ((float) (Util.getCurrentTime() - scathaPro.variables.lastWormSpawnTime) / Constants.wormSpawnCooldown));
        }
        else
        {
            spawnCooldownProgressBar.setProgress(0f);
        }
    }

}
