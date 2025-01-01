package com.namelessju.scathapro.eventlisteners;

import java.util.List;

import com.google.common.base.Predicate;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedEntity;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import com.namelessju.scathapro.events.WormPreSpawnEvent;
import com.namelessju.scathapro.gui.menus.FakeBanGui;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.managers.Config.Key;
import com.namelessju.scathapro.miscellaneous.ScathaProSound;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.TimeUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MiscListeners
{
    private final ScathaPro scathaPro;
    private final Minecraft mc;
    
    private long lastPreAlertTime = -1;
    
    public MiscListeners(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.getMinecraft();
    }
    
    @SubscribeEvent
    public void onWorldJoin(EntityJoinWorldEvent event)
    {
        if (event.entity != mc.thePlayer) return;
        
        // Reset
        
        DetectedEntity.clearLists();
        
        scathaPro.variables.lastWorldJoinTime = TimeUtil.now();
        scathaPro.variables.resetForNewLobby();

        scathaPro.getInputManager().unlockCameraRotation();
        
        // Update overlay
        
        scathaPro.getOverlay().updateOverlayFull();
        
        // Update achievements
        
        scathaPro.getAchievementLogicManager().updateKillsAchievements();
        scathaPro.getAchievementLogicManager().updateSpawnAchievements(null);
        
        Achievement.crystal_hollows_time_1.setProgress(0);
        Achievement.crystal_hollows_time_2.setProgress(0);
        Achievement.crystal_hollows_time_3.setProgress(0);
    }
    
    @SubscribeEvent
    public void onAttack(AttackEntityEvent event)
    {
        // Worm melee attack detection
        
        if (!(event.target instanceof EntityArmorStand)) return;
        final EntityArmorStand attackedArmorStand = (EntityArmorStand) event.target;
        
        DetectedWorm worm = null;
        
        // [Dev mode only] Check for attacked armor stand being the worm itself (= the name tag)
        if (scathaPro.getConfig().getBoolean(Config.Key.devMode))
        {
            worm = DetectedWorm.getById(attackedArmorStand.getEntityId());
        }
        
        // Check for main worm armor stand (= name tag) nearby
        if(worm == null && NBTUtil.isWormSkull(attackedArmorStand.getEquipmentInSlot(4)))
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
        
        if (worm != null)
        {
            ItemStack weapon = null;
            if (mc.thePlayer != null) weapon = mc.thePlayer.getHeldItem();
            worm.attack(weapon);
            
            if (!worm.lootsharePossible && NBTUtil.isWormSkull(attackedArmorStand.getEquipmentInSlot(4), true))
            {
                worm.lootsharePossible = true;
            }
        }
    }
    
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) return;
        
        ItemStack heldItem = event.entityPlayer.getHeldItem();
        if (heldItem != null)
        {
            if (heldItem.getItem() == Items.fishing_rod)
            {
                scathaPro.variables.lastProjectileWeaponUsed = heldItem;
            }
        }
    }
    
    @SubscribeEvent
    public void onUseItemStop(PlayerUseItemEvent.Stop event)
    {
        if (!scathaPro.isInCrystalHollows()) return;
        
        if (event.item != null && event.item.getItem() == Items.bow)
        {
            scathaPro.variables.lastProjectileWeaponUsed = event.item;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatReceivedImmediate(ClientChatReceivedEvent event)
    {
        if (event.type == 2) return;
        
        IChatComponent extendedPetDropMessage = TextUtil.extendPetDropMessage(event.message.getFormattedText());
        if (extendedPetDropMessage != null) event.message = extendedPetDropMessage;
        
        String unformattedText = StringUtils.stripControlCodes(event.message.getFormattedText());
        
        if (scathaPro.getConfig().getBoolean(Key.hideWormSpawnMessage)
            && unformattedText.equals("You hear the sound of something approaching..."))
        {
            event.setCanceled(true);
            return;
        }
        else if (unformattedText.equals("You used your Anomalous Desire Pickaxe Ability!"))
        {
            long now = TimeUtil.now();
            
            int cooldown = NBTUtil.getAnomalousDesireCooldown(mc.thePlayer.getHeldItem());
            if (cooldown >= 0)
            {
                scathaPro.variables.anomalousDesireReadyTime = now + cooldown * 1000L;
                scathaPro.variables.anomalousDesireCooldownEndTime = scathaPro.variables.anomalousDesireReadyTime;
            }
            
            scathaPro.variables.anomalousDesireWastedForRecovery = false;
            scathaPro.variables.anomalousDesireStartTime = now;
            
            if (scathaPro.variables.wormSpawnCooldownStartTime >= 0L)
            {
                long spawnCooldownElapsedTime = now - scathaPro.variables.wormSpawnCooldownStartTime;
                if (spawnCooldownElapsedTime < (long) (Constants.wormSpawnCooldown * 0.5D))
                {
                    scathaPro.variables.anomalousDesireWastedForRecovery = true;
                    
                    if (spawnCooldownElapsedTime < (long) (Constants.wormSpawnCooldown * 1D/3D)) Achievement.anomalous_desire_waste.unlock();
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onChatReceived(ClientChatReceivedEvent event)
    {
        if (event.type == 2) return;
        
        scathaPro.variables.lastChatMessageIsDivider = event.message.getUnformattedText().equals(TextUtil.chatDividerComponent.getUnformattedText());
        
        TextUtil.addChatCopyButton(event.message);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSound(PlaySoundEvent event)
    {
        // Mute sounds in fake ban screen
        
        if (mc.currentScreen != null && mc.currentScreen instanceof FakeBanGui && !event.name.equals("gui.button.press"))
        {
            event.result = null;
            return;
        }
        
        
        if (!scathaPro.isInCrystalHollows()) return;
        
        
        // Detect worm pre-spawn
        
        long now = TimeUtil.now();
        
        if
        (
            now - lastPreAlertTime > 10000 && event.name.equals("mob.spider.step")
            &&
            (
                event.sound.getPitch() == 2.0952382f && scathaPro.isInCrystalHollows()
                || event.sound.getPitch() >= 2f && scathaPro.getConfig().getBoolean(Config.Key.devMode)
            )
        ) {
            MinecraftForge.EVENT_BUS.post(new WormPreSpawnEvent());
            
            lastPreAlertTime = now;
        }
        
        // Mute non-Scatha-Pro sounds in Crystal Hollows
        
        if
        (
            scathaPro.getConfig().getBoolean(Config.Key.muteCrystalHollowsSounds)
            && scathaPro.isInCrystalHollows()
            && !(event.sound instanceof ScathaProSound) && !event.name.equals("gui.button.press")
        ) {
            event.result = null;
            return;
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTooltip(ItemTooltipEvent event)
    {
        if (!scathaPro.getConfig().getBoolean(Config.Key.devMode)) return;
        
        String skyblockItemID = NBTUtil.getSkyblockItemID(event.itemStack);
        if (skyblockItemID != null)
        {
            event.toolTip.add("");
            event.toolTip.add(EnumChatFormatting.RESET.toString() + EnumChatFormatting.DARK_GRAY + "Skyblock ID: " + skyblockItemID + EnumChatFormatting.RESET);
        }
    }
    
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event)
    {
        scathaPro.getInputManager().onKeyInput();
    }
    
    @SubscribeEvent
    public void onConnectedToServer(ClientConnectedToServerEvent event)
    {
        if (event.isLocal) return;
        scathaPro.getPersistentData().updateLoadedPlayer();
    }
}
