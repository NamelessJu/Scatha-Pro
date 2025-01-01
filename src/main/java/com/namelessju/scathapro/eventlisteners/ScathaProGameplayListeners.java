package com.namelessju.scathapro.eventlisteners;

import java.math.RoundingMode;
import java.util.Random;

import com.google.common.base.Predicate;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import com.namelessju.scathapro.events.BedrockWallDetectedEvent;
import com.namelessju.scathapro.events.ScathaPetDropEvent;
import com.namelessju.scathapro.events.WormDespawnEvent;
import com.namelessju.scathapro.events.WormHitEvent;
import com.namelessju.scathapro.events.WormKillEvent;
import com.namelessju.scathapro.events.WormPreSpawnEvent;
import com.namelessju.scathapro.events.WormSpawnEvent;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.enums.WormStatsType;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.SoundUtil;
import com.namelessju.scathapro.util.TimeUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ScathaProGameplayListeners extends ScathaProListener
{
    public ScathaProGameplayListeners(ScathaPro scathaPro)
    {
        super(scathaPro);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWormPreSpawn(WormPreSpawnEvent event)
    {
        scathaPro.variables.startWormSpawnCooldown(true);
        Alert.wormPrespawn.play();
        
        scathaPro.getAchievementLogicManager().handleAnomalousDesireRecoverAchievement();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWormSpawn(WormSpawnEvent event)
    {
        long now = TimeUtil.now();
        EntityPlayer player = scathaPro.getMinecraft().thePlayer;
        
        boolean spawnedBySelf = false;
        
        AxisAlignedBB playerDetectionAABB = new AxisAlignedBB(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ).expand(35f, 7f, 35f);
        int nearbyPlayerCount =  scathaPro.getMinecraft().theWorld.getEntitiesInAABBexcluding(player, playerDetectionAABB, new Predicate<Entity>() {
            @Override
            public boolean apply(Entity input)
            {
                return input instanceof EntityPlayer;
            }
        }).size();
        
        BlockPos playerPos = Util.entityBlockPos(player);
        BlockPos wormPos = Util.entityBlockPos(event.worm.getEntity()).down(1); // down because the armor stand is floating 1 block above ground
        
        if (nearbyPlayerCount <= 0) spawnedBySelf = true;
        else
        {
            int xDistance = Math.abs(playerPos.getX() - wormPos.getX());
            int zDistance = Math.abs(playerPos.getZ() - wormPos.getZ());
            
            if (
                xDistance <= 1 || zDistance <= 1
                && Math.abs(playerPos.getY() - wormPos.getY()) <= 1
            ) {
                spawnedBySelf = true;
                
                if (xDistance > 1 || zDistance > 1)
                {
                    int[] obstructionCheckDirection = {0, 0};

                    BlockPos currentCheckPos = wormPos;
                    BlockPos targetPos;
                    
                    if (xDistance >= zDistance)
                    {
                        obstructionCheckDirection[0] = (int) Math.signum(playerPos.getX() - wormPos.getX());
                        targetPos = new BlockPos(playerPos.getX(), wormPos.getY(), wormPos.getZ());
                    }
                    else
                    {
                        obstructionCheckDirection[1] = (int) Math.signum(playerPos.getZ() - wormPos.getZ());
                        targetPos = new BlockPos(wormPos.getX(), wormPos.getY(), playerPos.getZ());
                    }
                    
                    for (int i = 0; i < 5; i ++)
                    {
                        currentCheckPos = currentCheckPos.add(obstructionCheckDirection[0], 0, obstructionCheckDirection[1]);
                        
                        if (currentCheckPos.equals(targetPos)) break;
                        
                        Block block = mc.theWorld.getBlockState(currentCheckPos).getBlock();
                        if (block != Blocks.air && block != Blocks.lava && block != Blocks.flowing_lava && block != Blocks.water && block != Blocks.flowing_water)
                        {
                            scathaPro.logDebug("Worm (" + wormPos.toString() + ") not in FOV of player (" + playerPos.toString() + ") - blocked by " + block.getRegistryName() + " at " + currentCheckPos.toString());
                            spawnedBySelf = false;
                            break;
                        }
                    }
                }
            }
            else
            {
                scathaPro.logDebug("Worm spawn detection: player wasn't close to the worm's X or Z position");
            }
        }
        
        if (spawnedBySelf) scathaPro.logDebug("Worm spawned: assumed to be spawned by this client's player");
        else scathaPro.logDebug("Worm spawned: assumed to be spawned by other player");
        
        if (!spawnedBySelf) return;
        
        // Scatha spawn
        if (event.worm.isScatha)
        {
            Alert.scathaSpawn.play();
            WormStatsType.addScathaSpawn();
            scathaPro.getAchievementLogicManager().updateScathaSpawnAchievements(now, event.worm);
            
            if (scathaPro.isScappaModeActive())
            {
                SoundUtil.playMovingSound(ScathaPro.MODID + ":scappa", 1f, 1f, event.worm.getEntity());
            }
        }
        // Regular worm spawn
        else
        {
            Alert.regularWormSpawn.play();
            WormStatsType.addRegularWormSpawn();
        }
        
        if (event.timeSincePreviousSpawn >= 0L)
        {
            if (scathaPro.getConfig().getBoolean(Config.Key.wormSpawnTimer))
            {
                int secondsSinceLastSpawn = (int) Math.floor(event.timeSincePreviousSpawn / 1000D);
                String timeString;
                if (secondsSinceLastSpawn < 60) timeString = secondsSinceLastSpawn + " seconds";
                else timeString = Util.numberToString(secondsSinceLastSpawn / 60D, 1, true, RoundingMode.FLOOR) + " minutes";
                TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Worm spawned " + timeString + " after previous worm");
            }
        }
        
        scathaPro.getPersistentData().saveDailyStatsData();
        
        scathaPro.variables.lastWormSpawnTime = now;
        scathaPro.variables.startWormSpawnCooldown(false); // in case pre-spawn doesn't trigger when master volume is 0
        scathaPro.getAchievementLogicManager().updateSpawnAchievements(event);
        scathaPro.getOverlay().updateWormStreak();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWormHit(WormHitEvent event)
    {
        String skyblockItemID = NBTUtil.getSkyblockItemID(event.weapon);
        if (skyblockItemID != null && skyblockItemID.equals("DIRT") && event.worm.isScatha) Achievement.scatha_hit_dirt.unlock();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onWormKill(WormKillEvent event)
    {
        DetectedWorm worm = event.worm;
        if (worm.isScatha)
        {
            scathaPro.variables.addScathaKill();
            scathaPro.variables.lastScathaKillTime = TimeUtil.now();
            
            if (worm.getHitWeaponsCount() >= Achievement.kill_weapons_scatha.goal) Achievement.kill_weapons_scatha.unlock();
            if (worm.getHitWeaponsCount() > 0)
            {
                String lastHitWeapon = worm.getHitWeapons()[worm.getHitWeaponsCount() - 1];
                if (lastHitWeapon.equals("TERMINATOR")) Achievement.scatha_kill_terminator.unlock();
                else if (lastHitWeapon.equals("JUJU_SHORTBOW")) Achievement.scatha_kill_juju.unlock();
                else if (
                    lastHitWeapon.equals("FINE_TOPAZ_GEM") || lastHitWeapon.equals("FINE_AMETHYST_GEM") || lastHitWeapon.equals("FINE_JADE_GEM")
                    || lastHitWeapon.equals("FINE_AMBER_GEM") || lastHitWeapon.equals("FINE_SAPPHIRE_GEM")
                ) {
                    Achievement.scatha_kill_gemstone.unlock();
                }
            }
            if (mc.thePlayer.isSneaking() && scathaPro.variables.lastSneakStartTime >= 0 && worm.spawnTime >= scathaPro.variables.lastSneakStartTime) Achievement.scatha_kill_sneak.unlock();
            if (mc.thePlayer.posY - worm.getEntity().posY >= 0) Achievement.scatha_kill_highground.unlock(); // note: this armor stand (= Scatha name tag) is positioned a little bit above the worm visuals
            
            // seems weird but works because it would only be a problem on a kill that is about to trigger the b2b drop
            // but in that case it happens so fast that it won't ever be noticed
            Achievement.scatha_pet_drop_b2b.setProgress(0f);
            
            scathaPro.getOverlay().updateScathaKills();
            scathaPro.getOverlay().updateScathaKillsSinceLastDrop();
            scathaPro.getPersistentData().updateScathaFarmingStreak(true);
            
            
            if (!scathaPro.variables.scappaModeUnlocked)
            {
                if (new Random().nextFloat() < 0.004f)
                {
                    scathaPro.variables.scappaModeActiveTemp = true;
                    scathaPro.variables.scappaModeUnlocked = true;
                    scathaPro.getPersistentData().saveMiscData();
                    
                    TextUtil.sendChatDivider();
                    TextUtil.sendModChatMessage(TextUtil.getRainbowText("Scappa mode unlocked!") + "\n" + EnumChatFormatting.GRAY + "This was a 1/250 chance per Scatha kill. It will stay active for this game session, afterwards you can from now on toggle it freely under Scatha-Pro Settings > Miscellaneous.");
                    TextUtil.sendChatDivider();
                    
                    Achievement.scappa_mode.unlock();
                    
                    scathaPro.getOverlay().updateScappaMode();
                }
            }
        }
        else
        {
            scathaPro.variables.addRegularWormKill();
            
            if (worm.getHitWeaponsCount() >= Achievement.kill_weapons_regular_worm.goal) Achievement.kill_weapons_regular_worm.unlock();
            
            scathaPro.getOverlay().updateWormKills();
        }

        scathaPro.getPersistentData().saveWormKills();
        scathaPro.getPersistentData().saveDailyStatsData();
        
        scathaPro.getAchievementLogicManager().updateKillsAchievements();
        
        if (worm.getCurrentLifetime() <= 1000) Achievement.worm_kill_time_1.unlock();
        else if (worm.getCurrentLifetime() >= Constants.wormLifetime - 3000) Achievement.worm_kill_time_2.unlock();
        
        scathaPro.variables.lastKillTime = TimeUtil.now();
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWormDespawn(WormDespawnEvent event)
    {
        Achievement.worm_despawn.unlock();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onScathaPetDrop(ScathaPetDropEvent event)
    {
        String rarityTitle;

        switch (event.petDrop.rarity)
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
                TextUtil.sendModChatMessage(EnumChatFormatting.YELLOW + "Scatha pet dropped after a " + EnumChatFormatting.RED + dryStreak + " Scatha kill" + (dryStreak != 1 ? "s" : "") + EnumChatFormatting.YELLOW + " dry streak");
            }
            else if (dryStreak == 0)
            {
                TextUtil.sendModChatMessage(EnumChatFormatting.RED + "BACK TO BACK" + EnumChatFormatting.YELLOW + " Scatha pet drop!");
            }
        }
        
        
        scathaPro.getAchievementLogicManager().updatePetDropAchievements();
        
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
        
        if (mc.entityRenderer.isShaderActive()) // Super Secret Setting
        {
            Achievement.scatha_pet_drop_super_secret_setting.unlock();
        }
        
        if (scathaPro.variables.scathaKillsAtLastDrop >= 0 && scathaPro.variables.scathaKills >= 0 && scathaPro.variables.scathaKills == scathaPro.variables.scathaKillsAtLastDrop + 1)
        {
            Achievement.scatha_pet_drop_b2b.unlock();
        }
        Achievement.scatha_pet_drop_b2b.setProgress(1f);
        
        
        if (scathaPro.variables.scathaKills >= 0)
        {
            scathaPro.variables.scathaKillsAtLastDrop = scathaPro.variables.scathaKills;
            scathaPro.getOverlay().updateScathaKillsSinceLastDrop();
            scathaPro.getAchievementLogicManager().updateDryStreakAchievements();
        }
        
        scathaPro.variables.lastPetDropTime = TimeUtil.now(); 
        
        scathaPro.getPersistentData().savePetDrops();
        
        scathaPro.getOverlay().updatePetDrops();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBedrockWall(BedrockWallDetectedEvent event)
    {
        Alert.bedrockWall.play();
    }
}
