package com.namelessju.scathapro.eventlisteners;

import java.math.RoundingMode;
import java.util.Collection;

import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import com.namelessju.scathapro.events.AchievementUnlockedEvent;
import com.namelessju.scathapro.events.BedrockDetectedEvent;
import com.namelessju.scathapro.events.CrystalHollowsTickEvent;
import com.namelessju.scathapro.events.ScathaPetDropEvent;
import com.namelessju.scathapro.events.SkyblockAreaDetectedEvent;
import com.namelessju.scathapro.events.ModUpdateEvent;
import com.namelessju.scathapro.events.WormDespawnEvent;
import com.namelessju.scathapro.events.WormHitEvent;
import com.namelessju.scathapro.events.WormKillEvent;
import com.namelessju.scathapro.events.WormPreSpawnEvent;
import com.namelessju.scathapro.events.WormSpawnEvent;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.OverlayStats;
import com.namelessju.scathapro.miscellaneous.SkyblockArea;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.SoundUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ScathaProListeners
{
    private final ScathaPro scathaPro;
    private final Minecraft mc;
    
    
    private long lastKillTime = -1;
    private long lastPetDropTime = -1;
    private boolean lastKillIsScatha = false;
    private boolean droppedPetAtLastScatha = false;
    
    private boolean sneakingBefore = false;
    private long lastSneakStartTime = -1;
    
    private long lastAchievementUnlockTime = -1;
    
    public ScathaProListeners(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.getMinecraft();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onUpdate(ModUpdateEvent e)
    {
        if (scathaPro.getConfig().getBoolean(Config.Key.automaticBackups) && scathaPro.getPersistentData().getData().entrySet().size() > 0)
        {
            scathaPro.getPersistentData().backup("Update_" + (e.previousVersion != null ? "v" + e.previousVersion : "unknown") + "_to_v" + e.newVersion, true);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSkyblockAreaDetected(SkyblockAreaDetectedEvent e)
    {
        if (e.area != SkyblockArea.CRYSTAL_HOLLOWS) return;
        
        if (scathaPro.getConfig().getBoolean(Config.Key.muteCrystalHollowsSounds))
        {
            ChatComponentText chatComponent = new ChatComponentText(EnumChatFormatting.GRAY + "You've muted sounds in the Crystal Hollows! Only Scatha-Pro sounds will play - you can unmute other sounds again in ");
            
            ChatComponentText commandComponent = new ChatComponentText(EnumChatFormatting.GRAY.toString() + EnumChatFormatting.UNDERLINE + "/scathapro settings");
            commandComponent.setChatStyle(
                    new ChatStyle()
                    .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/scathapro settings"))
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Open Scatha-Pro settings")))
            );
            chatComponent.appendSibling(commandComponent);
            
            chatComponent.appendSibling(new ChatComponentText(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + "!"));
            MessageUtil.sendModChatMessage(chatComponent);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onCrystalHollowsTick(CrystalHollowsTickEvent e)
    {
        long now = Util.getCurrentTime();
        
        
        // Sneak start
        
        boolean sneaking = mc.thePlayer.isSneaking();
        if (!sneakingBefore && sneaking)
        {
            lastSneakStartTime = now;
        }
        sneakingBefore = sneaking;
        
        
        // Reset b2b scatha pet drop
        
        if (droppedPetAtLastScatha && lastKillIsScatha && now - lastKillTime > Constants.pingTreshold && lastPetDropTime < lastKillTime)
        {
            droppedPetAtLastScatha = false;
        }
        
        
        // Achievements
        
        float hours = (now - scathaPro.variables.lastWorldJoinTime) / (1000f*60*60);
        Achievement.crystal_hollows_time_1.setProgress(hours);
        Achievement.crystal_hollows_time_2.setProgress(hours);
        Achievement.crystal_hollows_time_3.setProgress(hours);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWormPreSpawn(WormPreSpawnEvent e)
    {
        scathaPro.variables.startWormSpawnCooldown(true);
        Alert.wormPrespawn.play();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWormSpawn(WormSpawnEvent e)
    {
        long now = Util.getCurrentTime();
        
        // Scatha spawn
        if (e.worm.isScatha)
        {
            OverlayStats.addScathaSpawn();
            
            Alert.scathaSpawn.play();
            
            handleScathaSpawnAchievements(now, e.worm);
        }
        
        // Regular worm spawn
        else
        {
            OverlayStats.addRegularWormSpawn();
            
            Alert.regularWormSpawn.play();
        }
        
        
        if (scathaPro.getConfig().getBoolean(Config.Key.wormSpawnTimer) && scathaPro.variables.lastWormSpawnTime >= 0L)
        {
            int secondsSinceLastSpawn = (int) Math.floor((now - scathaPro.variables.lastWormSpawnTime) / 1000D);
            
            String timeString;
            
            if (secondsSinceLastSpawn < 60) timeString = secondsSinceLastSpawn + " seconds";
            else timeString = Util.numberToString(secondsSinceLastSpawn / 60D, 1, true, RoundingMode.FLOOR) + " minutes";
            
            MessageUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Worm spawned " + timeString + " after previous worm");
        }
        
        
        scathaPro.variables.lastWormSpawnTime = now;
        scathaPro.variables.startWormSpawnCooldown(false); // in case pre-spawn doesn't trigger when master volume is 0
        scathaPro.updateSpawnAchievements();
        scathaPro.getOverlay().updateWormStreak();
    }
    
    private void handleScathaSpawnAchievements(long now, DetectedWorm worm)
    {
        // Time achievements
        
        if (now - scathaPro.variables.lastWorldJoinTime <= Achievement.scatha_spawn_time.goal * 60 * 1000)
        {
            Achievement.scatha_spawn_time.unlock();
        }
        
        // Height achievements
        
        if (worm.getEntity().posY > 186) Achievement.scatha_spawn_chtop.unlock();
        else if (worm.getEntity().posY < 32.5) Achievement.scatha_spawn_chbottom.unlock();
        
        // Scoreboard achievements
        
        if (!scathaPro.getAchievementManager().isAchievementUnlocked(Achievement.scatha_spawn_heat_burning))
        {
            scathaPro.logDebug("Checking for scoreboard heat value...");
            
            Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
            ScoreObjective sidebarObjective = scoreboard.getObjectiveInDisplaySlot(1);
            if (sidebarObjective != null)
            {
                scathaPro.logDebug("Scoreboard objective found in sidebar: \"" + sidebarObjective.getDisplayName() + "\"");
                
                Collection<Score> scores = scoreboard.getSortedScores(sidebarObjective);
                for (Score score : scores)
                {
                    String playerName = score.getPlayerName();
                    ScorePlayerTeam playerTeam = scoreboard.getPlayersTeam(playerName);
                    String formattedScoreText = ScorePlayerTeam.formatPlayerName(playerTeam, playerName);
                    String unformattedText = StringUtils.stripControlCodes(formattedScoreText.replace(playerName, ""));
                    
                    scathaPro.logDebug("Scoreboard line: \"" + unformattedText + "\"");
                    
                    if (unformattedText.startsWith("Heat:"))
                    {
                        String valueString = unformattedText.substring(5).trim();
                        
                        while (valueString.length() > 0)
                        {
                            char firstChar = valueString.charAt(0);
                            if (firstChar >= '0' && firstChar <= '9' || firstChar == '-') break;
                            if (valueString.startsWith("IMMUNE"))
                            {
                                valueString = null;
                                break;
                            }
                            valueString = valueString.substring(1).trim();
                        }
                        
                        if (valueString != null && !valueString.isEmpty())
                        {
                            int heat = -1;
                            try
                            {
                                heat = Integer.parseInt(valueString);
                            }
                            catch (NumberFormatException exception)
                            {
                                scathaPro.logError("Error while parsing scoreboard heat value: \"" + unformattedText + "\" couldn't be parsed to an int");
                            }
                            scathaPro.logDebug("Scoreboard heat entry found - value: " + heat);
                            
                            if (heat >= 90) Achievement.scatha_spawn_heat_burning.unlock();
                        }
                        else
                        {
                            scathaPro.logDebug("Scoreboard heat entry found, but entry has no int value");
                        }
                        
                        break;
                    }
                }
            }
            else scathaPro.logDebug("No scoreboard objective in sidebar found");
        }
        
        // Player dependent achievements
        
        EntityPlayer player = mc.thePlayer;
        if (player != null)
        {
            ItemStack helmetItem = mc.thePlayer.getCurrentArmor(3);
            String skyblockItemID = NBTUtil.getSkyblockItemID(helmetItem);
            if (skyblockItemID != null && skyblockItemID.equals("PET"))
            {
                NBTTagCompound skyblockNbt = NBTUtil.getSkyblockTagCompound(helmetItem);
                String petType = JsonUtil.getString(JsonUtil.parseObject(skyblockNbt.getString("petInfo")), "type");
                if (petType != null && petType.equals("SCATHA"))
                {
                    Achievement.scatha_spawn_scatha_helmet.unlock();
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWormHit(WormHitEvent e)
    {
        String skyblockItemID = NBTUtil.getSkyblockItemID(e.weapon);
        if (skyblockItemID != null && skyblockItemID.equals("DIRT") && e.worm.isScatha) Achievement.scatha_hit_dirt.unlock();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWormKill(WormKillEvent e)
    {
        DetectedWorm worm = e.worm;
        if (worm.isScatha)
        {
            scathaPro.variables.addScathaKill();
            
            scathaPro.variables.lastScathaKillTime = Util.getCurrentTime();
            lastKillIsScatha = true;
            
            if (worm.getHitWeaponsCount() >= Achievement.kill_weapons_scatha.goal) Achievement.kill_weapons_scatha.unlock();
            if (worm.getHitWeaponsCount() > 0)
            {
                String lastHitWeapon = worm.getHitWeapons()[worm.getHitWeaponsCount() - 1];
                if (lastHitWeapon.equals("TERMINATOR")) Achievement.scatha_kill_terminator.unlock();
                else if (lastHitWeapon.equals("JUJU_SHORTBOW")) Achievement.scatha_kill_juju.unlock();
            }
            if (mc.thePlayer.isSneaking() && lastSneakStartTime >= 0 && worm.spawnTime >= lastSneakStartTime) Achievement.scatha_kill_sneak.unlock();
            if (mc.thePlayer.posY - worm.getEntity().posY >= 0) Achievement.scatha_kill_highground.unlock(); // note: this armor stand (= Scatha name tag) is a little above the worm visuals already
            
            scathaPro.getOverlay().updateScathaKills();
            scathaPro.getOverlay().updateScathaKillsSinceLastDrop();
        }
        else
        {
            scathaPro.variables.addRegularWormKill();
            
            if (worm.getHitWeaponsCount() >= Achievement.kill_weapons_regular_worm.goal) Achievement.kill_weapons_regular_worm.unlock();
            
            lastKillIsScatha = false;
            
            scathaPro.getOverlay().updateWormKills();
        }
        
        scathaPro.getPersistentData().saveWormKills();
        
        scathaPro.updateKillAchievements();
        
        if (worm.getCurrentLifetime() <= 1000) Achievement.worm_kill_time_1.unlock();
        else if (worm.getCurrentLifetime() >= Constants.wormLifetime - 3000) Achievement.worm_kill_time_2.unlock();
        
        lastKillTime = Util.getCurrentTime();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormDespawn(WormDespawnEvent e)
    {
        Achievement.worm_despawn.unlock();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onScathaPetDrop(ScathaPetDropEvent e)
    {
        String rarityTitle = null;
        
        switch (e.petDrop.rarity)
        {
            case RARE:
                scathaPro.variables.rarePetDrops = Math.min(scathaPro.variables.rarePetDrops + 1, Constants.maxLegitPetDropsAmount);
                rarityTitle = EnumChatFormatting.BLUE + "RARE";
                break;
                
            case EPIC:
                scathaPro.variables.epicPetDrops = Math.min(scathaPro.variables.epicPetDrops + 1, Constants.maxLegitPetDropsAmount);
                rarityTitle = EnumChatFormatting.DARK_PURPLE + "EPIC";
                break;
                
            case LEGENDARY:
                scathaPro.variables.legendaryPetDrops = Math.min(scathaPro.variables.legendaryPetDrops + 1, Constants.maxLegitPetDropsAmount);
                rarityTitle = EnumChatFormatting.GOLD + "LEGENDARY";
                break;
                
            default:
                rarityTitle = EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + "Unknown Rarity";
        }
        
        if (scathaPro.getConfig().getBoolean(Config.Key.scathaPetDropAlert))
        {
            SoundUtil.playSound("random.chestopen", 1.5f, 0.95f);
            Alert.scathaPetDrop.play(rarityTitle);
        }
        
        if (scathaPro.getConfig().getBoolean(Config.Key.dryStreakMessage) && scathaPro.variables.scathaKills >= 0 && scathaPro.variables.scathaKillsAtLastDrop >= 0)
        {
            int dryStreak = (scathaPro.variables.scathaKills - 1) - scathaPro.variables.scathaKillsAtLastDrop;
            if (dryStreak > 0)
            {
                MessageUtil.sendModChatMessage(EnumChatFormatting.YELLOW + "Scatha pet dropped after a " + EnumChatFormatting.RED + dryStreak + " Scatha kill" + (dryStreak != 1 ? "s" : "") + EnumChatFormatting.YELLOW + " dry streak");
            }
            else if (dryStreak == 0)
            {
                MessageUtil.sendModChatMessage(EnumChatFormatting.RED + "BACK TO BACK" + EnumChatFormatting.YELLOW + " Scatha pet drop!");
            }
        }
        
        
        scathaPro.updatePetDropAchievements();
        
        if (scathaPro.getAlertModeManager().getCurrentMode().id.equals("normal"))
        {
            Achievement.scatha_pet_drop_mode_normal.unlock();
        }
        else if (scathaPro.getAlertModeManager().getCurrentMode().id.equals("meme"))
        {
            Achievement.scatha_pet_drop_mode_meme.unlock();
        }
        else if (scathaPro.getAlertModeManager().getCurrentMode().id.equals("anime"))
        {
            Achievement.scatha_pet_drop_mode_anime.unlock();
        }
        else if (scathaPro.getAlertModeManager().getCurrentMode().id.equals("custom") && scathaPro.getCustomAlertModeManager().getCurrentSubmodeId() != null)
        {
            Achievement.scatha_pet_drop_mode_custom.unlock();
        }
        
        if ((scathaPro.variables.scathaKillsAtLastDrop >= 0 && scathaPro.variables.scathaKills >= 0 && scathaPro.variables.scathaKills == scathaPro.variables.scathaKillsAtLastDrop + 1) || droppedPetAtLastScatha)
        {
            Achievement.scatha_pet_drop_b2b.unlock();
        }
        
        if (scathaPro.variables.scathaKills >= 0)
        {
            scathaPro.variables.scathaKillsAtLastDrop = scathaPro.variables.scathaKills;
            scathaPro.getOverlay().updateScathaKillsSinceLastDrop();
        }
        
        droppedPetAtLastScatha = true;
        lastPetDropTime = Util.getCurrentTime(); 
        
        scathaPro.getPersistentData().savePetDrops();
        
        scathaPro.getOverlay().updatePetDrops();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBedrockWall(BedrockDetectedEvent e)
    {
        Alert.bedrockWall.play();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onAchievementUnlocked(AchievementUnlockedEvent e)
    {
        Achievement achievement = e.achievement;
        
        ChatComponentText chatMessage = new ChatComponentText(
                (
                        achievement.type.typeName != null
                        ? achievement.type.getFormattedName() + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + " achievement"
                        : EnumChatFormatting.GREEN + "Achievement"
                )
                + " unlocked" + EnumChatFormatting.GRAY + " - "
        );
        
        ChatComponentText achievementComponent = new ChatComponentText(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.ITALIC + achievement.achievementName);
        ChatStyle achievementStyle = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(EnumChatFormatting.GOLD + achievement.achievementName + "\n" + EnumChatFormatting.GRAY + achievement.description)));
        achievementComponent.setChatStyle(achievementStyle);
        
        chatMessage.appendSibling(achievementComponent);
        
        MessageUtil.sendModChatMessage(chatMessage);
        
        long now = Util.getCurrentTime();
        
        if (now >= scathaPro.variables.lastWorldJoinTime + 1000 && now >= lastAchievementUnlockTime + 1000) // required or the game might crash when too many achievements are unlocked at once
        {
            switch (achievement.type)
            {
                case SECRET:
                    SoundUtil.playModSound("achievements.unlock", 0.9f, 0.749154f);
                    break;
                case BONUS:
                    SoundUtil.playModSound("achievements.unlock_hidden", 0.75f, 1.259921f);
                    break;
                case HIDDEN:
                    SoundUtil.playModSound("achievements.unlock_hidden", 0.75f, 0.749154f);
                    break;
                default:
                    SoundUtil.playModSound("achievements.unlock", 0.9f, 1f);
            }
        }
        
        
        scathaPro.updateProgressAchievements();

        
        lastAchievementUnlockTime = now;
    }
    
}
