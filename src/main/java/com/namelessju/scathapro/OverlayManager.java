package com.namelessju.scathapro;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.namelessju.scathapro.overlay.OverlayContainer;
import com.namelessju.scathapro.overlay.OverlayElement;
import com.namelessju.scathapro.overlay.OverlayImage;
import com.namelessju.scathapro.overlay.OverlayText;
import com.namelessju.scathapro.overlay.OverlayElement.Alignment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

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
    private final OverlayText overallTotalKillsText;
    private final OverlayText totalKillsText;
    private final OverlayText wormStreakText;
    private final OverlayText coordsText;
    private final OverlayText dayText;
    private final OverlayText rarePetDropsText;
    private final OverlayText epicPetDropsText;
    private final OverlayText legendaryPetDropsText;
    
    private OverlayManager() {
        
        overlay = new OverlayContainer(0, 0, 1f);
        overlay.padding = 5;
        overlay.backgroundColor = 0x50000000;
        
        overlay.add(new OverlayText("Scatha Farming:", Util.Color.GOLD.getValue(), 0, 0, 1.3f));
        
        
        OverlayContainer countersContainer = new OverlayContainer(0, 16, 1f);
        
        countersContainer.add(scathaPetImage = new OverlayImage(null, 256, 256, 0, 0, 0.118f));
        countersContainer.add(rarePetDropsText = new OverlayText(null, Util.Color.BLUE.getValue(), 15, 2, 1f));
        rarePetDropsText.setAlignment(Alignment.CENTER);
        countersContainer.add(epicPetDropsText = new OverlayText(null, Util.Color.DARK_PURPLE.getValue(), 15, 11, 1f));
        epicPetDropsText.setAlignment(Alignment.CENTER);
        countersContainer.add(legendaryPetDropsText = new OverlayText(null, Util.Color.GOLD.getValue(), 15, 20, 1f));
        legendaryPetDropsText.setAlignment(Alignment.CENTER);
        
        
        killsContainer = new OverlayContainer(33, 0, 1f);
        
        OverlayText wormKillsTitle = new OverlayText("Worms", Util.Color.YELLOW.getValue(), 23, 0, 1f);
        wormKillsTitle.setAlignment(OverlayElement.Alignment.CENTER);
        killsContainer.add(wormKillsTitle);
        killsContainer.add(new OverlayImage("overlay/worm.png", 512, 256, 3, 10, 0.08f));
        killsContainer.add(overallWormKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 23, 11, 1f));
        overallWormKillsText.setAlignment(OverlayElement.Alignment.CENTER);
        killsContainer.add(regularWormKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 23, 22, 1f));
        regularWormKillsText.setAlignment(OverlayElement.Alignment.CENTER);
        
        OverlayText overlayScathaKillsTitle = new OverlayText("Scathas", Util.Color.YELLOW.getValue(), 66, 0, 1f);
        overlayScathaKillsTitle.setAlignment(OverlayElement.Alignment.CENTER);
        killsContainer.add(overlayScathaKillsTitle);
        killsContainer.add(new OverlayImage("overlay/scatha.png", 512, 256, 46, 10, 0.08f));
        killsContainer.add(overallScathaKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 66, 11, 1f));
        overallScathaKillsText.setAlignment(OverlayElement.Alignment.CENTER);
        killsContainer.add(scathaKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 66, 22, 1f));
        scathaKillsText.setAlignment(OverlayElement.Alignment.CENTER);

        killsContainer.add(new OverlayText("Total", Util.Color.WHITE.getValue(), 93, 0, 1f));
        
        killsContainer.add(overallTotalKillsText = new OverlayText(null, Util.Color.WHITE.getValue(), 94, 11, 1f));
        killsContainer.add(totalKillsText = new OverlayText(null, Util.Color.GRAY.getValue(), 94, 22, 1f));
        
        countersContainer.add(killsContainer);
        
        
        overlay.add(countersContainer);
        
        
        overlay.add(wormStreakText = new OverlayText(null, Util.Color.GRAY.getValue(), 0, 50, 1f));

        overlay.add(dayText = new OverlayText(null, Util.Color.WHITE.getValue(), 0, 63, 1f));
        overlay.add(coordsText = new OverlayText(null, Util.Color.GRAY.getValue(), 0, 73, 1f));
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

        updateWormStreak();
        updateCoords();
        updateDay();
        
        updatePetDrops();
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
        String petImage = "overlay/mode_icons/";
        
        switch (Config.instance.getInt(Config.Key.mode)) {
            case 1:
                petImage += "meme.png";
                break;
            case 2:
                petImage += "anime.png";
                break;
            default:
                petImage = "overlay/scatha_pet.png";
                break;
        }

        scathaPetImage.setImage(petImage, 256, 256);
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
                        : "No scatha for " + Util.numberToString(-scathaPro.wormStreak) + " " + (-scathaPro.wormStreak == 1 ? "spawn" : "spawns")
                )
                : "No worms spawned yet"
        );
    }
    
    public void updateDay() {
        World world = mc.theWorld;
        
        int worldDay = world != null ? (int) Math.floor(world.getWorldTime() / 24000f) : 0;
        long lobbyTime = world != null && Util.inCrystalHollows() ? Util.getCurrentTime() - scathaPro.lastWorldJoinTime : 0L;
        SimpleDateFormat timerFormat = new SimpleDateFormat("HH:mm:ss");
        timerFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        dayText.setText(EnumChatFormatting.RESET + "Day " + worldDay + " " + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + "(" + timerFormat.format(lobbyTime) + ")");
        
        if (worldDay >= 14 && Util.inCrystalHollows()) {
            if (worldDay >= 15) dayText.setColor(Util.Color.DARK_RED.getValue());
            else dayText.setColor(Util.Color.RED.getValue());
        }
        else dayText.setColor(Util.Color.WHITE.getValue());
    }
    
    public void updateCoords() {
        EntityPlayer player = mc.thePlayer;
        
        String facingAxis = "";
        
        int facing = player != null ? Util.getFacing(player) : 1;
        
        switch (facing) {
            case 0:
                facingAxis = "-Z";
                break;
            case 1:
                facingAxis = "+X";
                break;
            case 2:
                facingAxis = "+Z";
                break;
            case 3:
                facingAxis = "-X";
                break;
        }
        
        String coordinatesString = player != null ? (int) Math.floor(player.posX) + " "  + (int) Math.floor(player.posY) + " "  + (int) Math.floor(player.posZ) : "0 0 0";
        
        coordsText.setText(EnumChatFormatting.RESET + coordinatesString + " " + EnumChatFormatting.ITALIC + facingAxis);
    }

}
