package com.namelessju.scathapro.eventlisteners;

import java.util.List;

import com.namelessju.scathapro.API;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.OverlayManager;
import com.namelessju.scathapro.PersistentData;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.events.WormPreSpawnEvent;
import com.namelessju.scathapro.objects.Worm;
import com.namelessju.scathapro.util.ChatUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
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
                PersistentData.instance.loadData();
                
                persistentDataLoaded = true;
            }
            
            // Reset
            
            scathaPro.wormStreak = 0;

            scathaPro.registeredWorms.clear();
            scathaPro.activeWorms.clear();
            
            scathaPro.regularWormKills = 0;
            scathaPro.scathaKills = 0;
            
            scathaPro.inBedrockWallRange = false;
            
            scathaPro.resetPreviousScathaPets();
            
            scathaPro.repeatProfilesDataRequest = true;
            
            scathaPro.lastWorldJoinTime = Util.getCurrentTime();
            
            // Update overlay
            
            OverlayManager.instance.updateOverlayFull();
            
            // Update achievements
            
            scathaPro.updateKillAchievements();
            scathaPro.updateSpawnAchievements();
            scathaPro.updatePetDropAchievements();
            
            Achievement.crystal_hollows_time_1.setProgress(0);
            Achievement.crystal_hollows_time_2.setProgress(0);
            Achievement.crystal_hollows_time_3.setProgress(0);
            
            // API request
            
            if (scathaPro.repeatProfilesDataRequest && Util.getCurrentTime() - scathaPro.lastProfilesDataRequestTime > 1000 * 60 * 5)
                API.requestProfilesData();
        }
    }
    
    @SubscribeEvent
    public void onAttack(AttackEntityEvent e) { // Worm attack detection
        if (e.target instanceof EntityArmorStand) {
            EntityArmorStand entity = (EntityArmorStand) e.target;
            World world = entity.worldObj;
            
            ItemStack helmetItem = entity.getEquipmentInSlot(4);
            if (helmetItem != null && NBTUtil.isWormSkull(helmetItem) || Config.instance.getBoolean(Config.Key.devMode)) {
                
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
        
        String unformattedText = StringUtils.stripControlCodes(e.message.getUnformattedText());
        
        // Automatically update API key when generating new one
        
        if (unformattedText.startsWith("Your new API key is ") && e.message.getSiblings().size() >= 1) {
            String apiKey = e.message.getSiblings().get(0).getChatStyle().getChatClickEvent().getValue();
            Config.instance.set(Config.Key.apiKey, apiKey);
            Config.instance.save();
            
            ChatUtil.sendModChatMessage("Automatically updated API key to " + apiKey);

            ScathaPro.getInstance().repeatProfilesDataRequest = true;
            if (scathaPro.profilesDataRequestNeeded()) API.requestProfilesData();
        }
        
        // Add copy button
        
        ChatUtil.addChatCopyButton(e.message);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSound(PlaySoundEvent e) {
        if ((Util.inCrystalHollows() && e.sound.getPitch() == 2.0952382f || Config.instance.getBoolean(Config.Key.devMode) && e.sound.getPitch() >= 2f) && e.name.equals("mob.spider.step"))
            MinecraftForge.EVENT_BUS.post(new WormPreSpawnEvent());
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent e) {
        if (Config.instance.getBoolean(Config.Key.devMode)) {
            ItemStack item = e.itemStack;
    
            if (item != null) {
                String skyblockItemID = NBTUtil.getSkyblockItemID(item);
                if (skyblockItemID != null) e.toolTip.add(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + skyblockItemID + EnumChatFormatting.RESET);
            }
        }
    }

}
