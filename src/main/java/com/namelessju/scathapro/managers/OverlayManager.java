package com.namelessju.scathapro.managers;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.events.OverlayInitEvent;
import com.namelessju.scathapro.overlay.OverlayContainer;
import com.namelessju.scathapro.overlay.OverlayElement;
import com.namelessju.scathapro.overlay.OverlayImage;
import com.namelessju.scathapro.overlay.OverlayProgressBar;
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
    private final ScathaPro scathaPro;
    private final Minecraft mc;
    
    private final OverlayContainer overlay;
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
    
    
    public OverlayManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.minecraft;
        
        overlay = new OverlayContainer(0, 0, 1f);
        overlay.padding = 5;
        overlay.backgroundColor = 0x50000000;
        
        
        MinecraftForge.EVENT_BUS.post(new OverlayInitEvent.Pre(overlay));
        
        
        OverlayContainer titleContainer = new OverlayContainer(0, 0, 1f);
        titleContainer.add(scathaPetImage = new OverlayImage(null, 256, 256, 0, 0, 0.043f));
        titleContainer.add(new OverlayText("Scatha Farming:", Util.Color.GOLD.getValue(), 16, 0, 1.3f));
        overlay.add(titleContainer);
        
        
        OverlayContainer countersContainer = new OverlayContainer(0, 16, 1f);
        
        
        OverlayContainer petDropsContainer = new OverlayContainer(0, 0, 1f);
        
        petDropsContainer.add(new OverlayText("Pets", Util.Color.GREEN.getValue(), 0, 0, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet.png", 256, 256, 0, 11, 0.033f));
        petDropsContainer.add(rarePetDropsText = new OverlayText(null, Util.Color.BLUE.getValue(), 12, 11, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet.png", 256, 256, 0, 22, 0.033f));
        petDropsContainer.add(epicPetDropsText = new OverlayText(null, Util.Color.DARK_PURPLE.getValue(), 12, 22, 1f));
        petDropsContainer.add(new OverlayImage("overlay/scatha_pet.png", 256, 256, 0, 33, 0.033f));
        petDropsContainer.add(legendaryPetDropsText = new OverlayText(null, Util.Color.GOLD.getValue(), 12, 33, 1f));
        
        countersContainer.add(petDropsContainer);
        
        
        killsContainer = new OverlayContainer(28, 0, 1f);
        
        OverlayText wormKillsTitle = new OverlayText("Worms", Util.Color.YELLOW.getValue(), 18, 0, 1f);
        wormKillsTitle.setAlignment(Alignment.CENTER);
        killsContainer.add(wormKillsTitle);
        OverlayText overlayScathaKillsTitle = new OverlayText("Scathas", Util.Color.YELLOW.getValue(), 61, 0, 1f);
        overlayScathaKillsTitle.setAlignment(Alignment.CENTER);
        killsContainer.add(overlayScathaKillsTitle);
        
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
        
        
        overlay.add(countersContainer);

        
        overlay.add(scathaKillsSinceLastDropText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 60, 1f));

        overlay.add(dayText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 71, 1f));
        overlay.add(coordsText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 81, 1f));
        

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
    
        if (overlayPositionPercentage[0] == 0.5) overlay.setContentAlignment(OverlayElement.Alignment.CENTER);
        else if (overlayPositionPercentage[0] > 0.5) overlay.setContentAlignment(OverlayElement.Alignment.RIGHT);
        else overlay.setContentAlignment(OverlayElement.Alignment.LEFT);
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
        
        regularWormKillsText.setText(Util.numberToString(world != null ? scathaPro.variables.regularWormKills : 0));
        overallWormKillsText.setText(scathaPro.variables.overallRegularWormKills >= 0 ? Util.numberToString(scathaPro.variables.overallRegularWormKills) : EnumChatFormatting.OBFUSCATED + "?");

        updateTotalKills();
    }
    
    public void updateScathaKills()
    {
        World world = mc.theWorld;
        
        scathaKillsText.setText(Util.numberToString(world != null ? scathaPro.variables.scathaKills : 0));
        overallScathaKillsText.setText(scathaPro.variables.overallScathaKills >= 0 ? Util.numberToString(scathaPro.variables.overallScathaKills) : EnumChatFormatting.OBFUSCATED + "?");

        updateTotalKills();
        updateScathaKillsSinceLastDrop();
    }
    
    private void updateTotalKills()
    {
        World world = mc.theWorld;

        int totalKills = world != null ? scathaPro.variables.regularWormKills + scathaPro.variables.scathaKills : 0;
        int overallTotalKills = scathaPro.variables.overallRegularWormKills >= 0 && scathaPro.variables.overallScathaKills >= 0 ? scathaPro.variables.overallRegularWormKills + scathaPro.variables.overallScathaKills : -1;
        
        float percentage = totalKills > 0 ? ((float) scathaPro.variables.scathaKills / totalKills) * 100 : -1f;
        float overallPercentage = overallTotalKills > 0 ? ((float) scathaPro.variables.overallScathaKills / overallTotalKills) * 100 : -1f;

        totalKillsText.setText(EnumChatFormatting.RESET + Util.numberToString(totalKills) + (percentage >= 0 ? EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + " (" + Util.numberToString(percentage, 1, true) + "%)" : ""));
        overallTotalKillsText.setText(overallTotalKills >= 0 ? EnumChatFormatting.RESET + Util.numberToString(overallTotalKills) + (overallPercentage >= 0 ? EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + " (" + Util.numberToString(overallPercentage, 1, true) + "%)" : "") : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateWormStreak()
    {
        wormStreakText.setText(
                scathaPro.variables.scathaStreak != 0
                ? (
                        scathaPro.variables.scathaStreak > 0
                        ? "Scatha spawn streak: " + Util.numberToString(scathaPro.variables.scathaStreak)
                        : "No Scatha for " + Util.numberToString(-scathaPro.variables.scathaStreak) + " " + (-scathaPro.variables.scathaStreak == 1 ? "spawn" : "spawns")
                )
                : "No worms spawned yet"
        );
    }
    
    public void updateDay()
    {
        World world = mc.theWorld;
        
        long worldTime = world != null ? world.getWorldTime() : 0;
        int worldDay = world != null ? (int) Math.floor(worldTime / 24000f) : 0;
        long lobbyTime = world != null && Util.inCrystalHollows() ? Util.getCurrentTime() - scathaPro.variables.lastWorldJoinTime : 0L;
        SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
        timerFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        int worldTimeDay = (int) ((worldTime % 24000f) % 24000f);
        int worldTimeHours = (int) Math.floor(worldTimeDay / 1000f);
        int worldTimeMinutes = (int) Math.floor((worldTimeDay - worldTimeHours * 1000f) / 1000f * 60f);
        
        EnumChatFormatting dayColor = EnumChatFormatting.WHITE;
        if (worldDay >= 29 && Util.inCrystalHollows())
        {
            if (worldDay >= 30) dayColor = EnumChatFormatting.DARK_RED;
            else dayColor = EnumChatFormatting.RED;
        }
        
        dayText.setText(EnumChatFormatting.RESET.toString() + dayColor + "Day " + worldDay + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(" + String.format("%02d:%02d", worldTimeHours, worldTimeMinutes) + ")" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + " / " + timerFormat.format(lobbyTime));
    }
    
    public void updateCoords()
    {
        double wallMin = 202D;
        double wallMax = 824D;
        double wallLength = wallMax - wallMin;

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
                    wallDistance = wallMax - player.posZ;
                    break;
                case 1:
                    facingAxis = "+X";
                    wallDistance = player.posX - wallMin;
                    break;
                case 2:
                    facingAxis = "+Z";
                    wallDistance = player.posZ - wallMin;
                    break;
                case 3:
                    facingAxis = "-X";
                    wallDistance = wallMax - player.posX;
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
        if (scathaPro.variables.overallScathaKills >= 0 && scathaPro.variables.scathaKillsAtLastDrop >= 0)
        {
            int scathaKillsSinceLastDrop = scathaPro.variables.overallScathaKills - scathaPro.variables.scathaKillsAtLastDrop;
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
            spawnCooldownProgressBar.setProgress(1f - ((Util.getCurrentTime() - scathaPro.variables.lastWormSpawnTime) / 30000f));
        }
        else
        {
            spawnCooldownProgressBar.setProgress(0f);
        }
    }

}
