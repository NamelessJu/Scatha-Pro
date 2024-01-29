package com.namelessju.scathapro.eventlisteners;

import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import com.namelessju.scathapro.events.AchievementUnlockedEvent;
import com.namelessju.scathapro.events.BedrockWallEvent;
import com.namelessju.scathapro.events.CrystalHollowsTickEvent;
import com.namelessju.scathapro.events.GoblinSpawnEvent;
import com.namelessju.scathapro.events.MeetDeveloperEvent;
import com.namelessju.scathapro.events.ScathaPetDropEvent;
import com.namelessju.scathapro.events.ModUpdateEvent;
import com.namelessju.scathapro.events.WormDespawnEvent;
import com.namelessju.scathapro.events.WormHitEvent;
import com.namelessju.scathapro.events.WormKillEvent;
import com.namelessju.scathapro.events.WormPreSpawnEvent;
import com.namelessju.scathapro.events.WormSpawnEvent;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.PersistentData;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.SoundUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
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
    
    private long lastPreAlertTime = -1;
    
    private boolean sneakingBefore = false;
    private long lastSneakStartTime = -1;
    
    private long lastAchievementUnlockTime = -1;
    
    public ScathaProListeners(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.minecraft;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUpdate(ModUpdateEvent e)
    {
        PersistentData persistentData = scathaPro.persistentData;
        if (scathaPro.config.getBoolean(Config.Key.automaticBackups) && persistentData.getData().entrySet().size() > 0)
        {
            persistentData.backup("Update_" + (e.previousVersion != null ? "v" + e.previousVersion : "unknown") + "_to_v" + e.newVersion, true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormPreSpawn(WormPreSpawnEvent e)
    {
        if (!scathaPro.config.getBoolean(Config.Key.wormPreAlert)) return;
        
        long now = Util.getCurrentTime();
        if (now - lastPreAlertTime > 3000)
        {
            Alert.wormPrespawn.play();
            lastPreAlertTime = now;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormSpawn(WormSpawnEvent e)
    {
        // Scatha spawn
        if (e.worm.isScatha)
        {
            if (scathaPro.variables.scathaStreak < 0) scathaPro.variables.scathaStreak = 0;
            scathaPro.variables.scathaStreak ++;
            
            if (Util.getCurrentTime() - scathaPro.variables.lastWorldJoinTime <= Achievement.scatha_spawn_time.goal * 60 * 1000)
            {
                Achievement.scatha_spawn_time.unlock();
            }
            if (e.worm.getEntity().posY > 186)
            {
                Achievement.scatha_spawn_chtop.unlock();
            }
            else if (e.worm.getEntity().posY < 32.5)
            {
                Achievement.scatha_spawn_chbottom.unlock();
            }
            
            if (scathaPro.config.getBoolean(Config.Key.scathaAlert))
            {
                Alert.scathaSpawn.play();
            }
        }
        
        // Worm spawn
        else
        {
            if (scathaPro.variables.scathaStreak > 0) scathaPro.variables.scathaStreak = 0;
            scathaPro.variables.scathaStreak --;
            
            if (scathaPro.config.getBoolean(Config.Key.wormAlert))
            {
                Alert.wormSpawn.play();
            }
        }
        
        long now = Util.getCurrentTime();
        
        if (scathaPro.config.getBoolean(Config.Key.wormSpawnTimer) && scathaPro.variables.lastWormSpawnTime >= 0L)
        {
            int secondsSinceLastSpawn = (int) Math.floor((now - scathaPro.variables.lastWormSpawnTime) / 1000D);
            
            String timeString;
            
            if (secondsSinceLastSpawn < 60)
            {
                timeString = secondsSinceLastSpawn + " seconds";
            }
            else
            {
                timeString = Util.numberToString(secondsSinceLastSpawn / 60D, 1) + " minutes";
            }
            
            MessageUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Worm spawned " + timeString + " after previous worm");
        }
        
        scathaPro.variables.lastWormSpawnTime = now;

        scathaPro.updateSpawnAchievements();
        
        scathaPro.overlayManager.updateWormStreak();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormHit(WormHitEvent e)
    {
        String skyblockItemID = NBTUtil.getSkyblockItemID(e.weapon);
        if (skyblockItemID != null && skyblockItemID.equals("DIRT") && e.worm.isScatha) Achievement.scatha_hit_dirt.unlock();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormKill(WormKillEvent e)
    {
        DetectedWorm worm = e.worm;
        if (worm.isScatha)
        {
            scathaPro.variables.scathaKills ++;
            if (scathaPro.variables.overallScathaKills >= 0) scathaPro.variables.overallScathaKills ++;
            
            if (worm.getHitWeaponsCount() >= Achievement.kill_weapons_scatha.goal) Achievement.kill_weapons_scatha.unlock();
            if (worm.getHitWeaponsCount() > 0 && worm.getHitWeapons()[worm.getHitWeaponsCount() - 1].equals("TERMINATOR")) Achievement.scatha_kill_terminator.unlock();
            if (mc.thePlayer.isSneaking() && lastSneakStartTime >= 0 && worm.spawnTime >= lastSneakStartTime) Achievement.scatha_kill_sneak.unlock();

            scathaPro.variables.lastScathaKillTime = Util.getCurrentTime();
            lastKillIsScatha = true;
            
            scathaPro.overlayManager.updateScathaKills();
            scathaPro.overlayManager.updateScathaKillsSinceLastDrop();
        }
        else
        {
            scathaPro.variables.regularWormKills ++;
            if (scathaPro.variables.overallRegularWormKills >= 0) scathaPro.variables.overallRegularWormKills ++;
            
            if (worm.getHitWeaponsCount() >= Achievement.kill_weapons_regular_worm.goal) Achievement.kill_weapons_regular_worm.unlock();
            
            lastKillIsScatha = false;

            scathaPro.overlayManager.updateWormKills();
        }
        
        scathaPro.persistentData.saveWormKills();
        
        scathaPro.updateKillAchievements();
        
        if (worm.getLifetime() <= Achievement.worm_kill_time_1.goal * 1000) Achievement.worm_kill_time_1.unlock();
        else if (worm.getLifetime() >= Achievement.worm_kill_time_2.goal * 1000) Achievement.worm_kill_time_2.unlock();
        
        lastKillTime = Util.getCurrentTime();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormDespawn(WormDespawnEvent e)
    {
        if (e.worm.getLifetime() >= 29 * 1000) Achievement.worm_despawn.unlock();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScathaPetDrop(ScathaPetDropEvent e)
    {
        String rarityTitle = null;
        
        switch (e.petDrop.rarity)
        {
            case RARE:
                scathaPro.variables.rarePetDrops ++;
                rarityTitle = EnumChatFormatting.BLUE + "Rare";
                break;
            case EPIC:
                scathaPro.variables.epicPetDrops ++;
                rarityTitle = EnumChatFormatting.DARK_PURPLE + "Epic";
                break;
            case LEGENDARY:
                scathaPro.variables.legendaryPetDrops ++;
                rarityTitle = EnumChatFormatting.GOLD + "Legendary";
                break;
            default:
                rarityTitle = EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + "Unknown Rarity";
        }
        
        if (scathaPro.config.getBoolean(Config.Key.petAlert))
        {
            SoundUtil.playSound("random.chestopen", 1.5f, 0.95f);
            Alert.scathaPetDrop.play(rarityTitle);
        }
        
        if (scathaPro.config.getBoolean(Config.Key.dryStreakMessage) && scathaPro.variables.overallScathaKills >= 0 && scathaPro.variables.scathaKillsAtLastDrop >= 0)
        {
            int dryStreak = (scathaPro.variables.overallScathaKills - 1) - scathaPro.variables.scathaKillsAtLastDrop;
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
        
        if (scathaPro.alertModeManager.getCurrentMode().id.equals("normal"))
        {
            Achievement.scatha_pet_drop_mode_normal.unlock();
        }
        else if (scathaPro.alertModeManager.getCurrentMode().id.equals("meme"))
        {
            Achievement.scatha_pet_drop_mode_meme.unlock();
        }
        else if (scathaPro.alertModeManager.getCurrentMode().id.equals("anime"))
        {
            Achievement.scatha_pet_drop_mode_anime.unlock();
        }
        else if (scathaPro.alertModeManager.getCurrentMode().id.equals("custom"))
        {
            Achievement.scatha_pet_drop_mode_custom.unlock();
        }
        
        if ((scathaPro.variables.scathaKillsAtLastDrop >= 0 && scathaPro.variables.overallScathaKills >= 0 && scathaPro.variables.overallScathaKills == scathaPro.variables.scathaKillsAtLastDrop + 1) || droppedPetAtLastScatha)
        {
            Achievement.scatha_pet_drop_b2b.unlock();
        }
        
        if (scathaPro.variables.overallScathaKills >= 0)
        {
            scathaPro.variables.scathaKillsAtLastDrop = scathaPro.variables.overallScathaKills;
            scathaPro.overlayManager.updateScathaKillsSinceLastDrop();
        }
        
        droppedPetAtLastScatha = true;
        lastPetDropTime = Util.getCurrentTime(); 
        
        scathaPro.persistentData.savePetDrops();
        
        scathaPro.overlayManager.updatePetDrops();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBedrockWall(BedrockWallEvent e)
    {
        if (scathaPro.config.getBoolean(Config.Key.wallAlert))
        {
            Alert.bedrockWall.play();
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGoblinSpawn(GoblinSpawnEvent e)
    {
        if (scathaPro.config.getBoolean(Config.Key.goblinAlert))
        {
            String goblinText;
            
            switch (e.goblin.type)
            {
                case GOLD:
                    goblinText = EnumChatFormatting.YELLOW + "Golden";
                    break;
                case DIAMOND:
                    goblinText = EnumChatFormatting.AQUA + "Diamond";
                    break;
                default:
                    goblinText = EnumChatFormatting.GRAY + "Unknown type";
            }
            
            Alert.goblinSpawn.play(goblinText);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAchievementUnlocked(AchievementUnlockedEvent e)
    {
        Achievement achievement = e.achievement;
        
        ChatComponentText chatMessage = new ChatComponentText(
                (
                        achievement.type.string != null
                        ? achievement.type.string + EnumChatFormatting.RESET + EnumChatFormatting.GREEN + " achievement"
                        : EnumChatFormatting.GREEN + "Achievement"
                )
                + " unlocked" + EnumChatFormatting.GRAY + " - "
        );
        
        ChatComponentText achievementComponent = new ChatComponentText(EnumChatFormatting.GOLD.toString() + EnumChatFormatting.ITALIC + achievement.name);
        ChatStyle achievementStyle = new ChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(achievement.name + "\n" + EnumChatFormatting.GRAY + achievement.description)));
        achievementComponent.setChatStyle(achievementStyle);
        
        chatMessage.appendSibling(achievementComponent);
        
        MessageUtil.sendModChatMessage(chatMessage);
        
        long now = Util.getCurrentTime();
        
        if (now >= scathaPro.variables.lastWorldJoinTime + 1000 && now >= lastAchievementUnlockTime + 1000)
        {
            switch (achievement.type)
            {
                case SECRET:
                    SoundUtil.playModSound("achievements.unlock", 1f, 0.75f);
                    break;
                case HIDDEN:
                    SoundUtil.playModSound("achievements.unlock_hidden", 0.75f, 0.75f);
                    break;
                default:
                    SoundUtil.playModSound("achievements.unlock", 1f, 1f);
            }
        }
        
        
        scathaPro.updateProgressAchievements();

        
        lastAchievementUnlockTime = now;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMeetDeveloper(MeetDeveloperEvent e)
    {
        Achievement.meet_developer.unlock();
    }
    
}
