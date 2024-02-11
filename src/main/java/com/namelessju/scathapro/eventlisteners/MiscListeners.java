package com.namelessju.scathapro.eventlisteners;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedEntity;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import com.namelessju.scathapro.events.ModUpdateEvent;
import com.namelessju.scathapro.events.WormPreSpawnEvent;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.PersistentData;
import com.namelessju.scathapro.miscellaneous.ScathaProSound;
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

public class MiscListeners
{
    private final ScathaPro scathaPro;
    private final Minecraft mc;

    private boolean persistentDataLoaded = false;
    private long lastPreAlertTime = -1;
    
    public MiscListeners(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.minecraft;
    }
    
    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent e)
    {
        Entity entity = e.entity;
        if (entity != mc.thePlayer) return;
            
        // Load data
        
        if (!persistentDataLoaded)
        {
            PersistentData persistentData = scathaPro.persistentData;
            persistentData.loadData();
            
            String lastUsedVersion = JsonUtil.getString(persistentData.getData(), "global/lastUsedVersion");
            if (lastUsedVersion == null || !lastUsedVersion.equals(ScathaPro.VERSION))
            {
                MinecraftForge.EVENT_BUS.post(new ModUpdateEvent(lastUsedVersion, ScathaPro.VERSION));
                
                JsonUtil.set(persistentData.getData(), "global/lastUsedVersion", new JsonPrimitive(ScathaPro.VERSION));
                persistentData.saveData();
            }
            
            persistentDataLoaded = true;
        }
        
        // Reset
        
        DetectedEntity.clearLists();
        
        scathaPro.variables.lastWorldJoinTime = Util.getCurrentTime();
        scathaPro.variables.resetForNewLobby();
        
        // Update overlay
        
        scathaPro.overlayManager.updateOverlayFull();
        
        // Update achievements
        
        scathaPro.updateKillAchievements();
        scathaPro.updateSpawnAchievements();
        scathaPro.updatePetDropAchievements();
        scathaPro.updateProgressAchievements();
        
        Achievement.crystal_hollows_time_1.setProgress(0);
        Achievement.crystal_hollows_time_2.setProgress(0);
        Achievement.crystal_hollows_time_3.setProgress(0);
    }
    
    @SubscribeEvent
    public void onAttack(AttackEntityEvent e)
    {
        // Worm melee attack detection
        
        if (!(e.target instanceof EntityArmorStand)) return;
        final EntityArmorStand attackedArmorStand = (EntityArmorStand) e.target;
        
        // Check for attacked armor stand being the worm itself (= the name tag)
        DetectedWorm worm = DetectedWorm.getById(attackedArmorStand.getEntityId());

        // Attacked armor stand could be a body piece, check for main armor stand nearby
        if (worm == null)
        {
            ItemStack helmetItem = attackedArmorStand.getEquipmentInSlot(4);
            if (helmetItem != null && NBTUtil.isWormSkull(helmetItem) || scathaPro.config.getBoolean(Config.Key.devMode))
            {
                World world = attackedArmorStand.worldObj;
                List<EntityArmorStand> nearbyArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(attackedArmorStand.posX, attackedArmorStand.posY, attackedArmorStand.posZ, attackedArmorStand.posX, attackedArmorStand.posY, attackedArmorStand.posZ).expand(8f, 2f, 8f), new Predicate<EntityArmorStand>() {
                    @Override
                    public boolean apply(EntityArmorStand armorStand) {
                        return armorStand != attackedArmorStand;
                    }
                });
                
                for (int i = 0; i < nearbyArmorStands.size(); i ++)
                {
                    EntityArmorStand armorStand = nearbyArmorStands.get(i);
                    int entityID = armorStand.getEntityId();
                    DetectedWorm nearbyWorm = DetectedWorm.getById(entityID);
                    if (nearbyWorm != null)
                    {
                        worm = nearbyWorm;
                        break;
                    }
                }
            }
        }
        
        if (worm != null)
        {
            ItemStack weapon = null;
            if (mc.thePlayer != null) weapon = mc.thePlayer.getHeldItem();
            worm.attack(weapon);
        }
    }
    
    @SubscribeEvent
    public void onInteractItem(PlayerInteractEvent e)
    {
        ItemStack heldItem = e.entityPlayer.getHeldItem();
        if (heldItem != null && (heldItem.getItem() == Items.fishing_rod || heldItem.getItem() == Items.bow))
        {
            scathaPro.variables.lastProjectileWeaponUsed = heldItem;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChatReceived(ClientChatReceivedEvent e)
    {
        if (e.type == 2) return;
        MessageUtil.addChatCopyButton(e.message);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSound(PlaySoundEvent e)
    {
        // Detect worm pre-spawn
        
        long now = Util.getCurrentTime();
        
        if
        (
            now - lastPreAlertTime > 3000 && e.name.equals("mob.spider.step")
            &&
            (
                e.sound.getPitch() == 2.0952382f && scathaPro.inCrystalHollows()
                || e.sound.getPitch() >= 2f && scathaPro.config.getBoolean(Config.Key.devMode)
            )
        )
        {
            MinecraftForge.EVENT_BUS.post(new WormPreSpawnEvent());
            
            lastPreAlertTime = now;
        }
        
        
        // Mute other sounds option
        if
        (
            scathaPro.config.getBoolean(Config.Key.muteOtherSounds)
            && scathaPro.inCrystalHollows()
            && !(e.sound instanceof ScathaProSound) && !e.name.equals("gui.button.press")
        )
        {
            e.result = null;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent e)
    {
        if (!scathaPro.config.getBoolean(Config.Key.devMode)) return;
        
        String skyblockItemID = NBTUtil.getSkyblockItemID(e.itemStack);
        if (skyblockItemID != null)
        {
            e.toolTip.add("");
            e.toolTip.add(EnumChatFormatting.RESET.toString() + EnumChatFormatting.GRAY + EnumChatFormatting.ITALIC + skyblockItemID + EnumChatFormatting.RESET);
        }
    }

}
