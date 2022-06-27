package com.namelessju.scathapro.eventlisteners;

import java.util.HashMap;
import java.util.List;

import com.namelessju.scathapro.API;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.Util;
import com.namelessju.scathapro.Worm;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.gui.OverlaySettingsGui;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class LoopListeners {

    Minecraft mc = Minecraft.getMinecraft();
    ScathaPro scathaPro = ScathaPro.getInstance();
    Config config = Config.getInstance();
    
    private boolean inCrystalHollowsBefore = false;
    
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event)
    {
        if (event.type == ElementType.TEXT) {
            if (config.getBoolean(Config.Key.overlay) && Util.inCrystalHollows() && !mc.gameSettings.showDebugInfo && !(mc.currentScreen instanceof OverlaySettingsGui))
                scathaPro.drawOverlay();
            
            if (config.getBoolean(Config.Key.showRotationAngles)) {
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
                
                boolean inCrystalHollows = Util.inCrystalHollows();
                if (inCrystalHollows && !inCrystalHollowsBefore) 
                    Util.sendModChatMessage(EnumChatFormatting.RED.toString() + EnumChatFormatting.ITALIC + "This is a pre-release version of 1.2! Please report any problems you find on the scatha farming discord!" + EnumChatFormatting.RESET);
                inCrystalHollowsBefore = inCrystalHollows;
                
                // Worm detection
                
                List<EntityArmorStand> nearbyArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(player.posX, player.posY, player.posZ, player.posX, player.posY, player.posZ).expand(20f, 10f, 20f));
                
                for (int i = 0; i < nearbyArmorStands.size(); i ++) {
                    
                    EntityArmorStand e = nearbyArmorStands.get(i);
                    int entityID = e.getEntityId();
                    String entityName = e.getName();
                    
                    if (entityName != null && Worm.getByID(scathaPro.registeredWorms, entityID) == null && entityName.contains(Util.getUnicodeString("2764"))) {
                        
                        if (StringUtils.stripControlCodes(entityName).contains("[Lv5] Worm ")) {
                            scathaPro.registeredWorms.add(new Worm(entityID, false));

                            scathaPro.backToBackWorms ++;
                            scathaPro.backToBackScathas = 0;
                            scathaPro.updateSpawnAchievements();

                            if (config.getBoolean(Config.Key.wormAlert)) {
                                mc.ingameGUI.displayTitle(null, null, 5, 20, 5);
                                mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Just a regular worm...", 0, 0, 0);
                                mc.ingameGUI.displayTitle(EnumChatFormatting.YELLOW + "Worm", null, 0, 0, 0);
        
                                if (!Util.playModeSound("alert.worm")) Util.playSoundAtPlayer("random.orb", 1f, 0.5f);
                            }
                        }
                        else if (StringUtils.stripControlCodes(entityName).contains("[Lv10] Scatha ")) {
                            scathaPro.registeredWorms.add(new Worm(entityID, true));

                            scathaPro.backToBackWorms = 0;
                            scathaPro.backToBackScathas ++;
                            scathaPro.updateSpawnAchievements();
                            
                            if (config.getBoolean(Config.Key.scathaAlert)) {
                                mc.ingameGUI.displayTitle(null, null, 0, 40, 10);
                                mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Pray to RNGesus!", 0, 0, 0);
                                mc.ingameGUI.displayTitle(EnumChatFormatting.RED + "Scatha", null, 0, 0, 0);
                                
                                if (!Util.playModeSound("alert.scatha")) Util.playSoundAtPlayer("random.orb", 1f, 0.8f);
                            }
                        }
                        
                        scathaPro.updateOverlayWormStreak();
                    }
                }
                
    
                // Worm kill detection
                
                for (int i = scathaPro.registeredWorms.size() - 1; i >= 0; i --) {
                    Worm worm = scathaPro.registeredWorms.get(i);
                    int entityID = worm.getEntityID();
                    
                    if (world.getEntityByID(entityID) == null) {    
                        if (now - worm.getLastAttackTime() < 1000 || scathaPro.lastFishingRodCast >= 0 && now - scathaPro.lastFishingRodCast < 1500) {
                            if (worm.isScatha()) {
                                scathaPro.scathaKills ++;
                                if (scathaPro.overallScathaKills >= 0) scathaPro.overallScathaKills ++;
                                
                                scathaPro.updateOverlayScathaKills();
                            }
                            else {
                                scathaPro.wormKills ++;
                                if (scathaPro.overallWormKills >= 0) scathaPro.overallWormKills ++;

                                scathaPro.updateOverlayWormKills();
                            }
                            
                            scathaPro.updateOverlayTotalKills();

                            scathaPro.updateKillAchievements();
                        }
                        
                        scathaPro.registeredWorms.remove(worm);
                    }
                }
                
                
                // Bedrock wall detection

                if (config.getBoolean(Config.Key.wallAlert)) {
                    if (inCrystalHollows) {
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
                            else if (!viewBlocked && block != Blocks.air) viewBlocked = true;
                        }
                        
                        if (!bedrockFound) scathaPro.inBedrockWallRange = false;
                        else if (!scathaPro.inBedrockWallRange) {
                            scathaPro.inBedrockWallRange = true;
                            
                            if (viewBlocked) {
                                mc.ingameGUI.displayTitle(null, null, 3, 20, 5);
                                mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Close to bedrock wall", 0, 0, 0);
                                mc.ingameGUI.displayTitle("", null, 0, 0, 0);
                                
                                if (!Util.playModeSound("alert.bedrock_wall")) Util.playSoundAtPlayer("note.pling", 1f, 0.5f);
                            }
                        }
                    }
                }
                
                
                // Scatha pet drop detection

                if (config.getBoolean(Config.Key.petAlert) && inCrystalHollows) {
                    // if (scathaPro.lastWorldJoinTime >= 0 && now - scathaPro.lastWorldJoinTime > 3000) { }
                    
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
                                    String displayName = displayNbt.getString("Name");
                                    NBTTagList displayLoreList = displayNbt.getTagList("Lore", 8);
                                    
                                    StringBuilder displayLoreBuilder = new StringBuilder();
                                    for (int j = 0; j < displayLoreList.tagCount(); j ++) {
                                        String loreLine = displayLoreList.getStringTagAt(j);
                                        if (j > 0) displayLoreBuilder.append("\n");
                                        displayLoreBuilder.append(loreLine);
                                    }
                                    String displayLore = displayLoreBuilder.toString();
                                    
                                    String skyblockItemID = Util.getSkyblockItemID(item);
                                    
                                    if ((skyblockItemID != null && skyblockItemID.equals("PET") || config.getBoolean(Config.Key.devMode)) && StringUtils.stripControlCodes(displayName).contains("Scatha")) {
                                        int rarity = 0;
                                        if (StringUtils.stripControlCodes(displayLore).contains("RARE")) rarity = 1;
                                        else if (StringUtils.stripControlCodes(displayLore).contains("EPIC")) rarity = 2;
                                        else if (StringUtils.stripControlCodes(displayLore).contains("LEGENDARY")) rarity = 3;
                                        
                                        Integer currentRarityAmount = currentScathaPets.get(rarity);
                                        currentScathaPets.put(rarity, (currentRarityAmount != null ? currentRarityAmount : 0) + item.stackSize);
                                    }
                                }
                            }
                        }
                    }
                    
                    if (
                            (
                                scathaPro.lastWormAttackTime >= 0 && now - scathaPro.lastWormAttackTime < 1000
                                ||
                                scathaPro.lastFishingRodCast >= 0 && now - scathaPro.lastFishingRodCast < 1500
                            )
                            && scathaPro.previousScathaPets != null
                        ) {
                        
                        int newScathaPet = -1;
                        
                        for (Integer rarityID : currentScathaPets.keySet()) {
                            int currentRarityCount = currentScathaPets.get(rarityID);
                            Integer previousRarityCount = scathaPro.previousScathaPets.get(rarityID);
                            int difference = currentRarityCount - (previousRarityCount != null ? previousRarityCount : 0);
                            if (difference > 0 && rarityID > newScathaPet) newScathaPet = rarityID;
                        }
                        
                        if (newScathaPet >= 0) {
                            mc.ingameGUI.displayTitle(null, null, 0, 130, 20);
                            
                            switch (newScathaPet) {
                                case 1:
                                    mc.ingameGUI.displayTitle(null, EnumChatFormatting.BLUE + "RARE", 0, 0, 0);
                                    scathaPro.rarePetDrops ++;
                                    break;
                                case 2:
                                    mc.ingameGUI.displayTitle(null, EnumChatFormatting.DARK_PURPLE + "EPIC", 0, 0, 0);
                                    scathaPro.epicPetDrops ++;
                                    break;
                                case 3:
                                    mc.ingameGUI.displayTitle(null, EnumChatFormatting.GOLD + "LEGENDARY", 0, 0, 0);
                                    scathaPro.legendaryPetDrops ++;
                                    break;
                                default:
                                    mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "unknown rarity", 0, 0, 0);
                            }
                            
                            mc.ingameGUI.displayTitle(EnumChatFormatting.YELLOW + "Scatha Pet!", null, 0, 0, 0);
                            
                            Util.playSoundAtPlayer("random.chestopen", 1.5f, 0.95f);
                            
                            if (!Util.playModeSound("alert.pet_drop")) Util.playSoundAtPlayer("mob.wither.death", 0.75f, 0.8f);
                            
                            scathaPro.updatePetDropAchievements();
                            
                            scathaPro.savePetDrops();
                        }
                    }
                    
                    scathaPro.previousScathaPets = currentScathaPets;
                }
                
                
                // API request
                
                if (scathaPro.repeatProfilesDataRequest && now - scathaPro.lastWorldJoinTime > 3000 && inCrystalHollows && now - scathaPro.lastProfilesDataRequestTime > 1000 * 60 * 5) {
                    scathaPro.lastProfilesDataRequestTime = now;
                    API.requestProfilesData();
                }
                
                
                // Update UI overlay
                
                if (config.getBoolean(Config.Key.overlay) && inCrystalHollows && !Minecraft.getMinecraft().gameSettings.showDebugInfo) {
                    scathaPro.updateOverlayCoords();
                    scathaPro.updateOverlayDay();
                    
                    scathaPro.updateOverlayPosition();
                }
                
                // Achievements
                
                if (inCrystalHollows) {
                    float hours = (now - scathaPro.lastWorldJoinTime) / (1000f*60*60);
                    Achievement.crystal_hollows_time_1.setProgress(hours);
                    Achievement.crystal_hollows_time_2.setProgress(hours);
                    Achievement.crystal_hollows_time_3.setProgress(hours);
                }
            }
        }
    }
}
