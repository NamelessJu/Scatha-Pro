package com.namelessju.scathapro.eventlisteners;

import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.PersistentData;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.alertmodes.Alert;
import com.namelessju.scathapro.events.AchievementUnlockedEvent;
import com.namelessju.scathapro.events.BedrockWallEvent;
import com.namelessju.scathapro.events.CrystalHollowsTickEvent;
import com.namelessju.scathapro.events.GoblinSpawnEvent;
import com.namelessju.scathapro.events.MeetDeveloperEvent;
import com.namelessju.scathapro.events.ScathaPetDropEvent;
import com.namelessju.scathapro.events.UpdateEvent;
import com.namelessju.scathapro.events.WormDespawnEvent;
import com.namelessju.scathapro.events.WormHitEvent;
import com.namelessju.scathapro.events.WormKillEvent;
import com.namelessju.scathapro.events.WormPreSpawnEvent;
import com.namelessju.scathapro.events.WormSpawnEvent;
import com.namelessju.scathapro.objects.Worm;
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

public class ScathaProListeners {
    
    private ScathaPro scathaPro = ScathaPro.getInstance();
    private Minecraft mc = Minecraft.getMinecraft();
    
    
    private long lastKillTime = -1;
    private long lastPetDropTime = -1;
    private boolean lastKillIsScatha = false;
    private boolean droppedPetAtLastScatha = false;
    
    private long lastPreAlertTime = -1;
    
    private boolean sneakingBefore = false;
    private long lastSneakStartTime = -1;
    
    private long lastAchievementUnlockTime = -1;


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUpdate(UpdateEvent e) {
    	PersistentData persistentData = scathaPro.persistentData;
    	if (scathaPro.config.getBoolean(Config.Key.automaticBackups) && persistentData.getData().entrySet().size() > 0)
    		persistentData.backup("Update-" + (e.previousVersion != null ? "v" + e.previousVersion : "unknown") + "_to_v" + e.newVersion, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onCrystalHollowsTick(CrystalHollowsTickEvent e) {
        long now = Util.getCurrentTime();
        
        
        // Sneak start
        
        boolean sneaking = mc.thePlayer.isSneaking();
        if (!sneakingBefore && sneaking) {
            lastSneakStartTime = now;
        }
        sneakingBefore = sneaking;
        
        
        // Reset b2b scatha pet drop
        
        if (droppedPetAtLastScatha && lastKillIsScatha && now - lastKillTime > ScathaPro.pingTreshold && lastPetDropTime < lastKillTime)
        	droppedPetAtLastScatha = false;
        
        
        // Achievements
        
        float hours = (now - scathaPro.lastWorldJoinTime) / (1000f*60*60);
        Achievement.crystal_hollows_time_1.setProgress(hours);
        Achievement.crystal_hollows_time_2.setProgress(hours);
        Achievement.crystal_hollows_time_3.setProgress(hours);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormPreSpawn(WormPreSpawnEvent e) {
        if (scathaPro.config.getBoolean(Config.Key.wormPreAlert)) {
            long now = Util.getCurrentTime();
            if (now - lastPreAlertTime > 2500) {
                Alert.wormPrespawn.play();
                lastPreAlertTime = now;
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormSpawn(WormSpawnEvent e) {
    	// Scatha spawn
        if (e.worm.isScatha) {
            if (scathaPro.wormStreak < 0) scathaPro.wormStreak = 0;
            scathaPro.wormStreak ++;
            
            if (Util.getCurrentTime() - scathaPro.lastWorldJoinTime <= Achievement.scatha_spawn_time.goal * 60 * 1000) 
                Achievement.scatha_spawn_time.unlock();
            if (e.worm.armorStand.posY > 186)
                Achievement.scatha_spawn_chtop.unlock();
            else if (e.worm.armorStand.posY < 32.5)
                Achievement.scatha_spawn_chbottom.unlock();
            
            if (scathaPro.config.getBoolean(Config.Key.scathaAlert)) {
                Alert.scathaSpawn.play();
            }
        }
        // Worm spawn
        else {
            if (scathaPro.wormStreak > 0) scathaPro.wormStreak = 0;
            scathaPro.wormStreak --;
            
            if (scathaPro.config.getBoolean(Config.Key.wormAlert)) {
                Alert.wormSpawn.play();
            }
        }
        
        long now = Util.getCurrentTime();
        
        if (scathaPro.config.getBoolean(Config.Key.wormSpawnTimer) && scathaPro.lastWormSpawnTime >= 0L) {
        	int secondsSinceLastSpawn = (int) Math.floor((now - scathaPro.lastWormSpawnTime) / 1000D);
        	
        	String timeString;
        	
        	if (secondsSinceLastSpawn < 60) {
        		timeString = secondsSinceLastSpawn + " seconds";
        	}
        	else {
        		timeString = Util.numberToString(secondsSinceLastSpawn / 60D, 1) + " minutes";
        	}
        	
        	MessageUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Worm spawned " + timeString + " after previous worm");
        }
        
        scathaPro.lastWormSpawnTime = now;

        scathaPro.updateSpawnAchievements();
        
        scathaPro.overlayManager.updateWormStreak();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormHit(WormHitEvent e) {
        String skyblockItemID = NBTUtil.getSkyblockItemID(e.weapon);
        if (skyblockItemID != null && skyblockItemID.equals("DIRT") && e.worm.isScatha) Achievement.scatha_hit_dirt.unlock();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormKill(WormKillEvent e) {
        Worm worm = e.worm;
        if (worm.isScatha) {
            scathaPro.scathaKills ++;
            if (scathaPro.overallScathaKills >= 0) scathaPro.overallScathaKills ++;
            
            if (worm.getHitWeaponsCount() >= Achievement.kill_weapons_scatha.goal) Achievement.kill_weapons_scatha.unlock();
            if (worm.getHitWeaponsCount() > 0 && worm.getHitWeapons()[worm.getHitWeaponsCount() - 1].equals("TERMINATOR")) Achievement.scatha_kill_terminator.unlock();
            if (mc.thePlayer.isSneaking() && lastSneakStartTime >= 0 && worm.spawnTime >= lastSneakStartTime) Achievement.scatha_kill_sneak.unlock();
            
            lastKillIsScatha = true;
            
            scathaPro.overlayManager.updateScathaKills();
            scathaPro.overlayManager.updateScathaKillsSinceLastDrop();
        }
        else {
            scathaPro.regularWormKills ++;
            if (scathaPro.overallRegularWormKills >= 0) scathaPro.overallRegularWormKills ++;
            
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
    public void onWormDespawn(WormDespawnEvent e) {
        if (e.worm.getLifetime() >= 29 * 1000) Achievement.worm_despawn.unlock();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onScathaPetDrop(ScathaPetDropEvent e) {
        String rarityTitle = null;
        
        switch (e.petDrop.rarity) {
            case RARE:
                scathaPro.rarePetDrops ++;
                rarityTitle = EnumChatFormatting.BLUE + "RARE";
                break;
            case EPIC:
                scathaPro.epicPetDrops ++;
                rarityTitle = EnumChatFormatting.DARK_PURPLE + "EPIC";
                break;
            case LEGENDARY:
                scathaPro.legendaryPetDrops ++;
                rarityTitle = EnumChatFormatting.GOLD + "LEGENDARY";
                break;
            default:
                rarityTitle = EnumChatFormatting.GRAY + "unknown rarity";
        }
        
        if (scathaPro.config.getBoolean(Config.Key.petAlert)) {
            SoundUtil.playSound("random.chestopen", 1.5f, 0.95f);
            Alert.scathaPetDrop.play(rarityTitle);
        }
        
        if (scathaPro.config.getBoolean(Config.Key.dryStreakMessage) && scathaPro.overallScathaKills >= 0 && scathaPro.scathaKillsAtLastDrop >= 0) {
            int dryStreak = (scathaPro.overallScathaKills - 1) - scathaPro.scathaKillsAtLastDrop;
            if (dryStreak > 0) {
            	MessageUtil.sendModChatMessage(EnumChatFormatting.YELLOW + "Scatha pet dropped after a " + EnumChatFormatting.RED + dryStreak + " Scatha kill" + (dryStreak != 1 ? "s" : "") + EnumChatFormatting.YELLOW + " dry streak");
            }
            else if (dryStreak == 0) {
            	MessageUtil.sendModChatMessage(EnumChatFormatting.RED + "BACK TO BACK" + EnumChatFormatting.YELLOW + " Scatha pet drop!");
            }
        }
        
        
        scathaPro.updatePetDropAchievements();
        
        if (scathaPro.alertModeManager.getCurrentMode().id.equals("normal"))
            Achievement.scatha_pet_drop_mode_normal.unlock();
        else if (scathaPro.alertModeManager.getCurrentMode().id.equals("meme"))
            Achievement.scatha_pet_drop_mode_meme.unlock();
        else if (scathaPro.alertModeManager.getCurrentMode().id.equals("anime"))
            Achievement.scatha_pet_drop_mode_anime.unlock();
        
        if ((scathaPro.scathaKillsAtLastDrop >= 0 && scathaPro.overallScathaKills >= 0 && scathaPro.overallScathaKills == scathaPro.scathaKillsAtLastDrop + 1) || droppedPetAtLastScatha) {
        	Achievement.scatha_pet_drop_b2b.unlock();
        }
        if (scathaPro.overallScathaKills >= 0) {
            scathaPro.scathaKillsAtLastDrop = scathaPro.overallScathaKills;
            scathaPro.overlayManager.updateScathaKillsSinceLastDrop();
        }
        droppedPetAtLastScatha = true;
        lastPetDropTime = Util.getCurrentTime(); 
        
        scathaPro.persistentData.savePetDrops();
        
        scathaPro.overlayManager.updatePetDrops();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBedrockWall(BedrockWallEvent e) {
        if (scathaPro.config.getBoolean(Config.Key.wallAlert)) {
            Alert.bedrockWall.play();
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onGoblinSpawn(GoblinSpawnEvent e) {
        if (scathaPro.config.getBoolean(Config.Key.goblinAlert)) {
        	
        	String goblinText;
        	
        	switch (e.goblin.type) {
				case GOLD:
		        	goblinText = EnumChatFormatting.YELLOW + "Golden Goblin";
					break;
				case DIAMOND:
		        	goblinText = EnumChatFormatting.AQUA + "Diamond Goblin";
					break;
				default:
					goblinText = EnumChatFormatting.GRAY + "Unknown Goblin";
        	}
        	
            Alert.goblinSpawn.play(goblinText);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAchievementUnlocked(AchievementUnlockedEvent e) {
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
        
        if (now >= ScathaPro.getInstance().lastWorldJoinTime + 1000 && now >= lastAchievementUnlockTime + 1000) {
            switch (achievement.type) {
                case SECRET:
                    SoundUtil.playModSound("achievements.unlock", 1f, 0.75f);
                    break;
                case HIDDEN:
                    SoundUtil.playModSound("achievements.unlock_hidden", 0.75f, 0.75f);
                    break;
                default:
                    SoundUtil.playModSound("achievements.unlock", 1f, 1f);
                    break;
            }
        }
        
        
        scathaPro.updateProgressAchievements();

        
        lastAchievementUnlockTime = now;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMeetDeveloper(MeetDeveloperEvent e) {
        Achievement.meet_developer.unlock();
    }
    
}
