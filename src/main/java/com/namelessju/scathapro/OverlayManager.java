package com.namelessju.scathapro;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

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
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class OverlayManager {
    public static final OverlayManager instance = new OverlayManager();

    private final ScathaPro scathaPro = ScathaPro.getInstance();
    private final Minecraft mc = Minecraft.getMinecraft();
    
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
    private final OverlayText scathaKillsAtLastDropText;
    
    
    private OverlayManager() {
        
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

        
        overlay.add(scathaKillsAtLastDropText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 60, 1f));

        overlay.add(dayText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 71, 1f));
        overlay.add(coordsText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 81, 1f));
        

        MinecraftForge.EVENT_BUS.post(new OverlayInitEvent.Post(overlay));
    }
    public void drawOverlay() {
        overlay.draw();
    }
    
    public void updateOverlayFull() {
        updatePosition();
        updateScale();
        updateVisibility();
        
        updateScathaPetImage();

        updateWormKills();
        updateScathaKills();
        updateTotalKills();

        updateCooldownProgressBar();

        updateWormStreak();
        updateCoords();
        updateDay();
        
        updatePetDrops();
        
        updateScathaKillsAtLastDrop();
    }

    public void updatePosition() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
    
        final double[] overlayPositionPercentage = {Config.instance.getDouble(Config.Key.overlayX), Config.instance.getDouble(Config.Key.overlayY)};
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
    
    public void updateScale() {
        overlay.setScale((float) Config.instance.getDouble(Config.Key.overlayScale));
    }
    
    public void updateVisibility() {
        overlay.setVisible(Config.instance.getBoolean(Config.Key.overlay));
    }
    
    public void updateScathaPetImage() {
        String alertModeID = AlertMode.getCurrentMode().id;
        
        String petImagePath = "overlay/" + (alertModeID != null ? "mode_icons/" + alertModeID : "scatha_pet") + ".png";

        scathaPetImage.setImage(petImagePath, 256, 256);
    }
    
    public void updatePetDrops() {
        rarePetDropsText.setText(Integer.toString(scathaPro.rarePetDrops));
        epicPetDropsText.setText(Integer.toString(scathaPro.epicPetDrops));
        legendaryPetDropsText.setText(Integer.toString(scathaPro.legendaryPetDrops));
    }
    
    public void updateWormKills() {
        World world = mc.theWorld;
        
        regularWormKillsText.setText(Util.numberToString(world != null ? scathaPro.regularWormKills : 0));
        overallWormKillsText.setText(scathaPro.overallRegularWormKills >= 0 ? Util.numberToString(scathaPro.overallRegularWormKills) : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateScathaKills() {
        World world = mc.theWorld;
        
        scathaKillsText.setText(Util.numberToString(world != null ? scathaPro.scathaKills : 0));
        overallScathaKillsText.setText(scathaPro.overallScathaKills >= 0 ? Util.numberToString(scathaPro.overallScathaKills) : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateTotalKills() {
        World world = mc.theWorld;

        int totalKills = world != null ? scathaPro.regularWormKills + scathaPro.scathaKills : 0;
        int overallTotalKills = scathaPro.overallRegularWormKills >= 0 && scathaPro.overallScathaKills >= 0 ? scathaPro.overallRegularWormKills + scathaPro.overallScathaKills : -1;
        
        int percentage = totalKills > 0 ? (int) Math.round(((float) scathaPro.scathaKills / totalKills) * 100) : -1;
        int overallPercentage = overallTotalKills > 0 ? (int) Math.round(((float) scathaPro.overallScathaKills / overallTotalKills) * 100) : -1;

        totalKillsText.setText(EnumChatFormatting.RESET + Util.numberToString(totalKills) + (percentage >= 0 ? EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + " (" + percentage + "%)" : ""));
        overallTotalKillsText.setText(overallTotalKills >= 0 ? EnumChatFormatting.RESET + Util.numberToString(overallTotalKills) + (overallPercentage >= 0 ? EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + " (" + overallPercentage + "%)" : "") : EnumChatFormatting.OBFUSCATED + "?");
    }
    
    public void updateWormStreak() {
        wormStreakText.setText(
                scathaPro.wormStreak != 0
                ? (
                        scathaPro.wormStreak > 0
                        ? "Scatha streak: " + Util.numberToString(scathaPro.wormStreak)
                        : "No Scatha for " + Util.numberToString(-scathaPro.wormStreak) + " " + (-scathaPro.wormStreak == 1 ? "spawn" : "spawns")
                )
                : "No worms spawned yet"
        );
    }
    
    public void updateDay() {
        World world = mc.theWorld;
        
        long worldTime = world != null ? world.getWorldTime() : 0;
        int worldDay = world != null ? (int) Math.floor(worldTime / 24000f) : 0;
        long lobbyTime = world != null && Util.inCrystalHollows() ? Util.getCurrentTime() - scathaPro.lastWorldJoinTime : 0L;
        SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
        timerFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        int worldTimeDay = (int) ((worldTime % 24000f) % 24000f);
        int worldTimeHours = (int) Math.floor(worldTimeDay / 1000f);
        int worldTimeMinutes = (int) Math.floor((worldTimeDay - worldTimeHours * 1000f) / 1000f * 60f);
        
        EnumChatFormatting dayColor = EnumChatFormatting.WHITE;
        if (worldDay >= 29 && Util.inCrystalHollows()) {
            if (worldDay >= 30) dayColor = EnumChatFormatting.DARK_RED;
            else dayColor = EnumChatFormatting.RED;
        }
        
        dayText.setText(EnumChatFormatting.RESET.toString() + dayColor + "Day " + worldDay + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(" + String.format("%02d:%02d", worldTimeHours, worldTimeMinutes) + ")" + EnumChatFormatting.RESET + EnumChatFormatting.GRAY + " / " + timerFormat.format(lobbyTime));
    }
    
    public void updateCoords() {
        double wallMin = 202D;
        double wallMax = 824D;
        double wallLength = wallMax - wallMin;
        
        EntityPlayer player = mc.thePlayer;
        
        String facingAxis = "";
        double wallDistance = 0D;
        
        int facing = player != null ? Util.getFacing(player) : 1;
        switch (facing) {
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
        }
        
        double wallProgress = Math.min(Math.max((wallDistance - 1D) / (wallLength - 2D), 0D), 1D);
        
        String coordinatesString = player != null ? (int) Math.floor(player.posX) + " "  + (int) Math.floor(player.posY) + " "  + (int) Math.floor(player.posZ) : EnumChatFormatting.OBFUSCATED + "0 0 0";
        
        coordsText.setText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.WHITE + coordinatesString + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + facingAxis + " (" + Util.numberToString(Math.floor(wallProgress * 1000D) / 10D, 1) + "% to wall)");
    }
    
    public void updateScathaKillsAtLastDrop() {
        scathaKillsAtLastDropText.setText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + "Scathas since last pet drop: " + (scathaPro.overallScathaKills >= 0 && scathaPro.scathaKillsAtLastDrop >= 0 ? scathaPro.overallScathaKills - scathaPro.scathaKillsAtLastDrop : EnumChatFormatting.OBFUSCATED + "?"));
    }
    
    public void updateCooldownProgressBar() {
    	if (scathaPro.lastWormSpawnTime >= 0f) {
        	spawnCooldownProgressBar.setProgress(1f - ((Util.getCurrentTime() - scathaPro.lastWormSpawnTime) / 30000f));
    	}
    	else {
        	spawnCooldownProgressBar.setProgress(0f);
    	}
    }

}
