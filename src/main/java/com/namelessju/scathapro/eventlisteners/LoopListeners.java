package com.namelessju.scathapro.eventlisteners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.OverlayManager;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.UpdateChecker;
import com.namelessju.scathapro.Worm;
import com.namelessju.scathapro.events.BedrockWallEvent;
import com.namelessju.scathapro.events.CrystalHollowsTickEvent;
import com.namelessju.scathapro.events.MeetDeveloperEvent;
import com.namelessju.scathapro.events.ScathaPetDropEvent;
import com.namelessju.scathapro.events.WormDespawnEvent;
import com.namelessju.scathapro.events.WormKillEvent;
import com.namelessju.scathapro.events.WormSpawnEvent;
import com.namelessju.scathapro.gui.FakeBanGui;
import com.namelessju.scathapro.gui.OverlaySettingsGui;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class LoopListeners {

    Minecraft mc = Minecraft.getMinecraft();
    ScathaPro scathaPro = ScathaPro.getInstance();
    
    private boolean firstIngameFrame = true;
    
    private long lastDeveloperCheckTime = -1;
    private boolean developerFoundBefore = false;
    
    private HashMap<Integer, String> arrowOwners = new HashMap<Integer, String>();
    
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event)
    {
        if (event.type == ElementType.TEXT) {
            if (Config.instance.getBoolean(Config.Key.overlay) && Util.inCrystalHollows() && !mc.gameSettings.showDebugInfo && !(mc.currentScreen instanceof OverlaySettingsGui))
                OverlayManager.instance.drawOverlay();
            
            if (Config.instance.getBoolean(Config.Key.showRotationAngles)) {
                EntityPlayer player = mc.thePlayer;
                
                if (player != null) {
                    ScaledResolution scaledResolution = new ScaledResolution(mc);
                    FontRenderer fontRenderer = mc.fontRendererObj;
    
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(Math.round(scaledResolution.getScaledWidth() / 2), Math.round(scaledResolution.getScaledHeight() / 2), 0);
                    GlStateManager.scale(0.75f, 0.75f, 1f);
                    
                    float yaw = player.rotationYaw % 360;
                    if (yaw < 0) yaw = 360 + yaw;
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(10, -4, 0);
                    fontRenderer.drawString(Util.numberToString(yaw, 1), 0, 0, Util.Color.GRAY.getValue(), true);
                    GlStateManager.popMatrix();
                    
                    String pitchString = Util.numberToString(player.rotationPitch, 1);
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(-fontRenderer.getStringWidth(pitchString) * 0.5, 10, 0);
                    fontRenderer.drawString(pitchString, 0, 0, Util.Color.GRAY.getValue(), true);
                    GlStateManager.popMatrix();
                    
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START) {
            
            long now = Util.getCurrentTime();
            
            if (scathaPro.openGuiNextTick != null) {
                mc.displayGuiScreen(scathaPro.openGuiNextTick);
                scathaPro.openGuiNextTick = null;
            }
            
            
            EntityPlayer player = mc.thePlayer;
            
            if (player != null) {
                World world = player.worldObj;
                
                
                if (firstIngameFrame) {
                    UpdateChecker.checkForUpdate();
                    
                    firstIngameFrame = false;
                }
                

                boolean inCrystalHollows = Util.inCrystalHollows();
                if (inCrystalHollows) {
                    
                    
                    MinecraftForge.EVENT_BUS.post(new CrystalHollowsTickEvent());
                    
                    
                    // Worm detection
                    
                    List<EntityArmorStand> nearbyArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ).expand(20f, 10f, 20f));
                    
                    for (int i = 0; i < nearbyArmorStands.size(); i ++) {
                        
                        EntityArmorStand e = nearbyArmorStands.get(i);
                        int entityID = e.getEntityId();
                        String entityName = e.getName();
                        
                        if (entityName != null && Worm.getByID(entityID) == null && entityName.contains(Util.getUnicodeString("2764"))) {
                            boolean isRegisteredWorm = scathaPro.registeredWorms.contains(entityID);
                            
                            Worm newWorm = null;
                            
                            if (StringUtils.stripControlCodes(entityName).contains("[Lv5] Worm "))
                                newWorm = new Worm(e, false);
                            else if (StringUtils.stripControlCodes(entityName).contains("[Lv10] Scatha "))
                                newWorm = new Worm(e, true);
                            
                            if (newWorm != null) {
                                scathaPro.activeWorms.add(newWorm);
                                
                                if (!isRegisteredWorm) {
                                    scathaPro.registeredWorms.add(entityID);

                                    MinecraftForge.EVENT_BUS.post(new WormSpawnEvent(newWorm));
                                }
                            }
                        }
                    }
                    
                    
                    // Projectile worm hit detection
                    
                    List<EntityArrow> arrows = world.getEntities(EntityArrow.class, new Predicate<EntityArrow>() {
                        @Override
                        public boolean apply(EntityArrow input) {
                            return !input.onGround;
                        }
                    });
                    for (int i = 0; i < arrows.size(); i ++) {
                        EntityArrow arrow = arrows.get(i);
                        int id = arrow.getEntityId();
                        
                        if (!arrowOwners.containsKey(id)) {
                            EntityPlayer owner = world.getClosestPlayerToEntity(arrow, -1);
                            arrowOwners.put(id, Util.getUUIDString(owner.getUniqueID()));
                        }
                        
                        if (arrowOwners.get(id).equals(Util.getPlayerUUIDString())) {
                            List<EntityArmorStand> hitArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(arrow.posX, arrow.posY, arrow.posZ, arrow.posX, arrow.posY, arrow.posZ).expand(3f, 3f, 3f));
                            for (int j = 0; j < hitArmorStands.size(); j ++) {
                                EntityArmorStand armorStand = hitArmorStands.get(j);
                                Worm worm = Worm.getByID(armorStand.getEntityId());
                                if (worm != null) worm.attack(scathaPro.lastProjectileWeaponUsed);
                            }
                        }
                    }
                    for (int i = 0; i < arrowOwners.size(); i ++) {
                        int arrowID = new ArrayList<Integer>(arrowOwners.keySet()).get(i);
                        if (world.getEntityByID(arrowID) == null) arrowOwners.remove(arrowID);
                    }
                    
                    List<EntityFishHook> fishHooks = world.getEntities(EntityFishHook.class, new Predicate<EntityFishHook>() {
                        @Override
                        public boolean apply(EntityFishHook input) {
                            Minecraft mc = Minecraft.getMinecraft();
                            EntityPlayer player = mc != null ? Minecraft.getMinecraft().thePlayer : null;
                            return player != null ? input.angler == player : false;
                        }
                    });
                    for (int i = 0; i < fishHooks.size(); i ++) {
                        EntityFishHook hook = fishHooks.get(i);
                        List<EntityArmorStand> hookedArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(hook.posX, hook.posY, hook.posZ, hook.posX, hook.posY, hook.posZ).expand(3f, 3f, 3f));
                        for (int j = 0; j < hookedArmorStands.size(); j ++) {
                            EntityArmorStand armorStand = hookedArmorStands.get(j);
                            Worm worm = Worm.getByID(armorStand.getEntityId());
                            if (worm != null) worm.attack(scathaPro.lastProjectileWeaponUsed);
                        }
                    }
                    
                    
                    // Worm kill/despawn detection
                    
                    for (int i = scathaPro.activeWorms.size() - 1; i >= 0; i --) {
                        Worm worm = scathaPro.activeWorms.get(i);
                        int entityID = worm.armorStand.getEntityId();
                        
                        if (world.getEntityByID(entityID) == null) {
                            if (now - worm.getLastAttackTime() < 2000) MinecraftForge.EVENT_BUS.post(new WormKillEvent(worm));
                            else MinecraftForge.EVENT_BUS.post(new WormDespawnEvent(worm));
                            
                            scathaPro.activeWorms.remove(worm);
                        }
                    }
                    
                    
                    // Bedrock wall detection
    
                    int[] checkDirection = {0, 0};
                    
                    switch (Util.getFacing(player)) {
                        case 0:
                            checkDirection[1] = -1;
                            break;
                        case 1:
                            checkDirection[0] = 1;
                            break;
                        case 2:
                            checkDirection[1] = 1;
                            break;
                        case 3:
                            checkDirection[0] = -1;
                            break;
                    }
                    
                    boolean bedrockFound = false;
                    boolean viewBlocked = false;
                    BlockPos playerPos = Util.entityBlockPos(player);
                    
                    for (int i = 0; i < 10; i ++) {
                        BlockPos checkPos = playerPos.add(checkDirection[0] * i, 1, checkDirection[1] * i);
                        Block block = world.getBlockState(checkPos).getBlock();
                        if (block == Blocks.bedrock) {
                            bedrockFound = true;
                            break;
                        }
                        else if (block != Blocks.air) viewBlocked = true;
                    }
                    
                    if (!bedrockFound) scathaPro.inBedrockWallRange = false;
                    else if (!scathaPro.inBedrockWallRange) {
                        scathaPro.inBedrockWallRange = true;
                        
                        if (viewBlocked) MinecraftForge.EVENT_BUS.post(new BedrockWallEvent());
                    }
                    
                    
                    // Scatha pet drop detection
                    
                    ItemStack[] inventory = player.inventory.mainInventory;
                    
                    HashMap<Integer, Integer> currentScathaPets = new HashMap<Integer, Integer>();
    
                    for (int i = 0; i < inventory.length; i++) {
                        if (i == 8) continue;

                        ItemStack item = inventory[i];
                        
                        if (item != null) {
                            NBTTagCompound nbt = item.getTagCompound();
                            
                            if (nbt != null) {
                                NBTTagCompound displayNbt = nbt.getCompoundTag("display");
                                if (displayNbt != null) {
                                    
                                    String skyblockItemID = NBTUtil.getSkyblockItemID(item);
                                    
                                    if (skyblockItemID != null && skyblockItemID.equals("PET")) {

                                        NBTTagCompound skyblockNbt = NBTUtil.getSkyblockTagCompound(item);
                                        
                                        JsonObject petInfo = null;
                                        try {
                                            petInfo = new JsonParser().parse(skyblockNbt.getString("petInfo")).getAsJsonObject();
                                        }
                                        catch (Exception e) {}
                                        
                                        if (petInfo != null) {
                                            
                                            String petType = null;
                                            JsonElement petTypeElement = petInfo.get("type");
                                            if (petTypeElement.isJsonPrimitive()) {
                                                JsonPrimitive petTypePrimitive = petTypeElement.getAsJsonPrimitive();
                                                if (petTypePrimitive.isString()) petType = petTypePrimitive.getAsString();
                                            }
                                            
                                            if (petType != null && petType.equals("SCATHA")) {
                                                
                                                String petTier = null;
                                                JsonElement petTierElement = petInfo.get("tier");
                                                if (petTierElement.isJsonPrimitive()) {
                                                    JsonPrimitive petTierPrimitive = petTierElement.getAsJsonPrimitive();
                                                    if (petTierPrimitive.isString()) petTier = petTierPrimitive.getAsString();
                                                }
                                                
                                                int rarity = 0;
                                                if (petTier.equals("RARE")) rarity = 1;
                                                else if (petTier.equals("EPIC")) rarity = 2;
                                                else if (petTier.equals("LEGENDARY")) rarity = 3;
                                                
                                                Integer currentRarityAmount = currentScathaPets.get(rarity);
                                                currentScathaPets.put(rarity, (currentRarityAmount != null ? currentRarityAmount : 0) + item.stackSize);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (scathaPro.lastWormAttackTime >= 0 && now - scathaPro.lastWormAttackTime < 2000 && scathaPro.previousScathaPets != null) {
                        
                        int newScathaPet = -1;
                        
                        for (Integer rarityID : currentScathaPets.keySet()) {
                            int currentRarityCount = currentScathaPets.get(rarityID);
                            Integer previousRarityCount = scathaPro.previousScathaPets.get(rarityID);
                            int difference = currentRarityCount - (previousRarityCount != null ? previousRarityCount : 0);
                            if (difference > 0 && rarityID > newScathaPet) newScathaPet = rarityID;
                        }
                        
                        if (newScathaPet >= 0) MinecraftForge.EVENT_BUS.post(new ScathaPetDropEvent(newScathaPet));
                    }
                    
                    scathaPro.previousScathaPets = currentScathaPets;
                    
                }
                
                
                // Dev check
                
                if (now - lastDeveloperCheckTime >= 1000) {
                    NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
                    if (netHandler != null) {
                        Collection<NetworkPlayerInfo> playerInfos = netHandler.getPlayerInfoMap();
                        
                        boolean developerFound = false;
                        
                        for (Iterator<NetworkPlayerInfo> iterator = playerInfos.iterator(); iterator.hasNext();) {
                            NetworkPlayerInfo p = iterator.next();
                            
                            if (Util.isDeveloper(p)) {
                                if (!developerFoundBefore) MinecraftForge.EVENT_BUS.post(new MeetDeveloperEvent(p));
                                developerFound = true;
                                break;
                            }
                        }
                        
                        developerFoundBefore = developerFound;
                    }
                    
                    lastDeveloperCheckTime = now;
                }
                
                
                // Open fake ban screen
                
                if (mc.currentScreen == null && scathaPro.showFakeBan) {
                    scathaPro.openGuiNextTick = new FakeBanGui();
                    
                    scathaPro.showFakeBan = false;
                }
            }
        }
    }
}
