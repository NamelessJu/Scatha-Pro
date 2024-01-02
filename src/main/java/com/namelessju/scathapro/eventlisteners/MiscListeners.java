package com.namelessju.scathapro.eventlisteners;

import java.util.List;

import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.PersistentData;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.ScathaProSound;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.events.UpdateEvent;
import com.namelessju.scathapro.events.WormPreSpawnEvent;
import com.namelessju.scathapro.objects.Worm;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MiscListeners {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final ScathaPro scathaPro = ScathaPro.getInstance();

    private boolean persistentDataLoaded = false;
    
    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent e) {
        Entity entity = e.entity;

        if (entity == mc.thePlayer) {
            
            // Load data
            
            if (!persistentDataLoaded) {
            	PersistentData persistentData = ScathaPro.getInstance().persistentData;
            	persistentData.loadData();
                
                String lastUsedVersion = JsonUtil.getString(persistentData.getData(), "global/lastUsedVersion");
                if (lastUsedVersion == null || !lastUsedVersion.equals(ScathaPro.VERSION)) {
                    MinecraftForge.EVENT_BUS.post(new UpdateEvent(lastUsedVersion, ScathaPro.VERSION));
                      
                    JsonUtil.set(persistentData.getData(), "global/lastUsedVersion", new JsonPrimitive(ScathaPro.VERSION));
                    persistentData.saveData();
                }
                
                persistentDataLoaded = true;
            }
            
            // Reset
            
            scathaPro.wormStreak = 0;

            scathaPro.registeredWorms.clear();
            scathaPro.activeWorms.clear();
            
            scathaPro.regularWormKills = 0;
            scathaPro.scathaKills = 0;
            
            scathaPro.lastWormSpawnTime = -1;
            
            scathaPro.inBedrockWallRange = false;
            
            scathaPro.resetPreviousScathaPets();
            
            scathaPro.lastWorldJoinTime = Util.getCurrentTime();
            
            // Update overlay
            
            ScathaPro.getInstance().overlayManager.updateOverlayFull();
            
            // Update achievements
            
            scathaPro.updateKillAchievements();
            scathaPro.updateSpawnAchievements();
            scathaPro.updatePetDropAchievements();
            scathaPro.updateProgressAchievements();
            
            Achievement.crystal_hollows_time_1.setProgress(0);
            Achievement.crystal_hollows_time_2.setProgress(0);
            Achievement.crystal_hollows_time_3.setProgress(0);
        }
    }
    
    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) { // Worm attack detection
        if (e.target instanceof EntityArmorStand) {
            EntityArmorStand entity = (EntityArmorStand) e.target;
            World world = entity.worldObj;
            
            ItemStack helmetItem = entity.getEquipmentInSlot(4);
            if (helmetItem != null && NBTUtil.isWormSkull(helmetItem) || scathaPro.config.getBoolean(Config.Key.devMode)) {
                
                List<EntityArmorStand> nearbyArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(entity.posX, entity.posY, entity.posZ, entity.posX, entity.posY, entity.posZ).expand(8f, 2f, 8f));
                
                for (int i = 0; i < nearbyArmorStands.size(); i ++) {
                    
                    EntityArmorStand armorStand = nearbyArmorStands.get(i);
                    
                    int entityID = armorStand.getEntityId();
                    Worm worm = Worm.getByID(entityID);
                    
                    if (worm != null) {
                        ItemStack weapon = null;
                        if (mc.thePlayer != null) weapon = mc.thePlayer.getHeldItem();
                        worm.attack(weapon);
                        break;
                    }
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onInteractItem(PlayerInteractEvent e) {
        ItemStack heldItem = e.entityPlayer.getHeldItem();
        if (heldItem != null) {
            if (heldItem.getItem() == Items.fishing_rod || heldItem.getItem() == Items.bow)
                scathaPro.lastProjectileWeaponUsed = heldItem;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChatReceived(ClientChatReceivedEvent e) {
        if (e.type == 2) return;
        
        MessageUtil.addChatCopyButton(e.message);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSound(PlaySoundEvent e) {
    	
    	// Detect worm pre-spawn
        if (
        		e.name.equals("mob.spider.step")
        		&& (
        			e.sound.getPitch() == 2.0952382f && Util.inCrystalHollows()
    				|| e.sound.getPitch() >= 2f && scathaPro.config.getBoolean(Config.Key.devMode)
				)
    		)
        {
            MinecraftForge.EVENT_BUS.post(new WormPreSpawnEvent());
    	}
        
        
        // Mute other sounds option
        if (
        		scathaPro.config.getBoolean(Config.Key.muteOtherSounds)
        		&& Util.inCrystalHollows()
        		&& !(e.sound instanceof ScathaProSound) && !e.name.equals("gui.button.press")
    		)
        {
        	e.result = null;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent e) {
        if (scathaPro.config.getBoolean(Config.Key.devMode)) {
            ItemStack item = e.itemStack;
    
            if (item != null) {
                String skyblockItemID = NBTUtil.getSkyblockItemID(item);
                if (skyblockItemID != null) e.toolTip.add(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + skyblockItemID + EnumChatFormatting.RESET);
            }
        }
    }

}
