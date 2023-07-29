package com.namelessju.scathapro.eventlisteners;

import com.namelessju.scathapro.AlertMode;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.OverlayManager;
import com.namelessju.scathapro.PersistentData;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.events.AchievementUnlockedEvent;
import com.namelessju.scathapro.events.BedrockWallEvent;
import com.namelessju.scathapro.events.CrystalHollowsTickEvent;
import com.namelessju.scathapro.events.MeetDeveloperEvent;
import com.namelessju.scathapro.events.ScathaPetDropEvent;
import com.namelessju.scathapro.events.UpdateEvent;
import com.namelessju.scathapro.events.WormDespawnEvent;
import com.namelessju.scathapro.events.WormHitEvent;
import com.namelessju.scathapro.events.WormKillEvent;
import com.namelessju.scathapro.events.WormPreSpawnEvent;
import com.namelessju.scathapro.events.WormSpawnEvent;
import com.namelessju.scathapro.objects.Worm;
import com.namelessju.scathapro.util.ChatUtil;
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
    	if (Config.instance.getBoolean(Config.Key.automaticBackups) && PersistentData.instance.getData().entrySet().size() > 0)
    		PersistentData.instance.backup("Update-" + (e.previousVersion != null ? "v" + e.previousVersion : "unknown") + "_to_v" + e.newVersion, true);
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
        if (Config.instance.getBoolean(Config.Key.wormPreAlert)) {
            long now = Util.getCurrentTime();
            if (now - lastPreAlertTime > 2500) {
                mc.ingameGUI.displayTitle(null, null, 0, 20, 5);
                mc.ingameGUI.displayTitle(null, EnumChatFormatting.YELLOW + "Worm about to spawn...", 0, 0, 0);
                mc.ingameGUI.displayTitle("", null, 0, 0, 0);
                
                if (!AlertMode.playModeSound("prespawn")) SoundUtil.playSound("random.orb", 1f, 0.5f);
                
                lastPreAlertTime = now;
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormSpawn(WormSpawnEvent e) {
        if (e.worm.isScatha) {
            if (scathaPro.wormStreak < 0) scathaPro.wormStreak = 0;
            scathaPro.wormStreak ++;
            
            if (Util.getCurrentTime() - scathaPro.lastWorldJoinTime <= Achievement.scatha_spawn_time.goal * 60 * 1000) 
                Achievement.scatha_spawn_time.unlock();
            if (e.worm.armorStand.posY > 186)
                Achievement.scatha_spawn_chtop.unlock();
            else if (e.worm.armorStand.posY < 32.5)
                Achievement.scatha_spawn_chbottom.unlock();
            
            if (Config.instance.getBoolean(Config.Key.scathaAlert)) {
                mc.ingameGUI.displayTitle(null, null, 0, 40, 10);
                mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Pray to RNGesus!", 0, 0, 0);
                mc.ingameGUI.displayTitle(EnumChatFormatting.RED + "Scatha", null, 0, 0, 0);
                
                if (!AlertMode.playModeSound("scatha")) SoundUtil.playSound("random.levelup", 1f, 0.8f);
            }
        }
        else {
            if (scathaPro.wormStreak > 0) scathaPro.wormStreak = 0;
            scathaPro.wormStreak --;
            
            if (Config.instance.getBoolean(Config.Key.wormAlert)) {
                mc.ingameGUI.displayTitle(null, null, 5, 20, 5);
                mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Just a regular worm...", 0, 0, 0);
                mc.ingameGUI.displayTitle(EnumChatFormatting.YELLOW + "Worm", null, 0, 0, 0);

                if (!AlertMode.playModeSound("worm")) SoundUtil.playSound("random.levelup", 1f, 0.5f);
            }
        }
        
        long now = Util.getCurrentTime();
        
        if (Config.instance.getBoolean(Config.Key.wormSpawnTimer) && scathaPro.lastWormSpawnTime >= 0L) {
        	int secondsSinceLastSpawn = (int) Math.floor((now - scathaPro.lastWormSpawnTime) / 1000D);
        	
        	String timeString;
        	
        	if (secondsSinceLastSpawn < 60) {
        		timeString = secondsSinceLastSpawn + " seconds";
        	}
        	else {
        		timeString = Util.numberToString(secondsSinceLastSpawn / 60D, 1) + " minutes";
        	}
        	
        	ChatUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Worm spawned " + timeString + " after previous worm");
        }
        
        scathaPro.lastWormSpawnTime = now;

        scathaPro.updateSpawnAchievements();
        
        OverlayManager.instance.updateWormStreak();
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
            
            OverlayManager.instance.updateScathaKills();
            OverlayManager.instance.updateScathaKillsAtLastDrop();
        }
        else {
            scathaPro.regularWormKills ++;
            if (scathaPro.overallRegularWormKills >= 0) scathaPro.overallRegularWormKills ++;
            
            if (worm.getHitWeaponsCount() >= Achievement.kill_weapons_regular_worm.goal) Achievement.kill_weapons_regular_worm.unlock();
            
            lastKillIsScatha = false;

            OverlayManager.instance.updateWormKills();
        }
        
        PersistentData.instance.saveWormKills();
        
        OverlayManager.instance.updateTotalKills();
        
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
        
        if (Config.instance.getBoolean(Config.Key.petAlert)) {
            
            mc.ingameGUI.displayTitle(null, null, 0, 130, 20);
            mc.ingameGUI.displayTitle(null, rarityTitle, 0, 0, 0);
            mc.ingameGUI.displayTitle(EnumChatFormatting.YELLOW + "Scatha Pet!", null, 0, 0, 0);
            
            SoundUtil.playSound("random.chestopen", 1.5f, 0.95f);
            
            if (!AlertMode.playModeSound("pet_drop")) SoundUtil.playSound("mob.wither.death", 0.75f, 0.8f);
        }
        
        scathaPro.updatePetDropAchievements();
        
        switch (Config.instance.getInt(Config.Key.mode)) {
            case 0:
                Achievement.scatha_pet_drop_mode_normal.unlock();
                break;
            case 1:
                Achievement.scatha_pet_drop_mode_meme.unlock();
                break;
            case 2:
                Achievement.scatha_pet_drop_mode_anime.unlock();
                break;
        }
        
        if ((scathaPro.scathaKillsAtLastDrop >= 0 && scathaPro.overallScathaKills >= 0 && scathaPro.overallScathaKills == scathaPro.scathaKillsAtLastDrop + 1) || droppedPetAtLastScatha) Achievement.scatha_pet_drop_b2b.unlock();
        if (scathaPro.overallScathaKills >= 0) {
            scathaPro.scathaKillsAtLastDrop = scathaPro.overallScathaKills;
            OverlayManager.instance.updateScathaKillsAtLastDrop();
        }
        droppedPetAtLastScatha = true;
        lastPetDropTime = Util.getCurrentTime(); 
        
        PersistentData.instance.savePetDrops();
        
        OverlayManager.instance.updatePetDrops();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBedrockWall(BedrockWallEvent e) {
        if (Config.instance.getBoolean(Config.Key.wallAlert)) {
            mc.ingameGUI.displayTitle(null, null, 3, 20, 5);
            mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Close to bedrock wall", 0, 0, 0);
            mc.ingameGUI.displayTitle("", null, 0, 0, 0);
            
            if (!AlertMode.playModeSound("bedrock_wall")) SoundUtil.playSound("note.pling", 1f, 0.5f);
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
        
        ChatUtil.sendModChatMessage(chatMessage);
        
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
