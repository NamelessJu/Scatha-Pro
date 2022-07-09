package com.namelessju.scathapro.eventlisteners;

import java.util.List;

import com.namelessju.scathapro.API;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.OverlayManager;
import com.namelessju.scathapro.PersistentData;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.Util;
import com.namelessju.scathapro.Worm;
import com.namelessju.scathapro.achievements.Achievement;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
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
    private long lastPreAlertTime = -1;
    
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
            if (helmetItem != null && Util.isWormSkull(helmetItem) || Config.instance.getBoolean(Config.Key.devMode)) {
                
                List<EntityArmorStand> nearbyArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, entity.getEntityBoundingBox().expand(8f, 8f, 8f));
                
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
            
            Util.sendModChatMessage("Automatically updated API key to " + apiKey);
            
            if (scathaPro.profilesDataRequestNeeded()) API.requestProfilesData();
        }
        
        // Add copy button
        
        Util.addChatCopyButton(e.message);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSound(PlaySoundEvent e) {
        long now = Util.getCurrentTime();
        if (Config.instance.getBoolean(Config.Key.wormPreAlert) && (Util.inCrystalHollows() && e.sound.getPitch() == 2.0952382f || Config.instance.getBoolean(Config.Key.devMode) && e.sound.getPitch() >= 2f) && e.name.equals("mob.spider.step") && now - lastPreAlertTime > 2500) {
            mc.ingameGUI.displayTitle(null, null, 0, 20, 5);
            mc.ingameGUI.displayTitle(null, EnumChatFormatting.YELLOW + "Worm about to spawn...", 0, 0, 0);
            mc.ingameGUI.displayTitle("", null, 0, 0, 0);
            
            if (!Util.playModeSound("alert.prespawn")) Util.playSoundAtPlayer("random.orb", 1f, 0.5f);
            
            lastPreAlertTime = now;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent e) {
        if (Config.instance.getBoolean(Config.Key.devMode)) {
            ItemStack item = e.itemStack;
    
            if (item != null) {
                String skyblockItemID = Util.getSkyblockItemID(item);
                if (skyblockItemID != null) e.toolTip.add(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + skyblockItemID + EnumChatFormatting.RESET);
            }
        }
    }

}
