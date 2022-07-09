package com.namelessju.scathapro.eventlisteners;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;
import com.namelessju.scathapro.Config;
import com.namelessju.scathapro.OverlayManager;
import com.namelessju.scathapro.PersistentData;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.Util;
import com.namelessju.scathapro.Worm;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.gui.FakeBanGui;
import com.namelessju.scathapro.gui.OverlaySettingsGui;

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
    
    private boolean inCrystalHollowsBefore = false;

    private long lastKillTime = -1;
    private long lastPetDropTime = -1;
    private boolean lastKillIsScatha = false;
    private boolean droppedPetAtLastScatha = false;
    
    private boolean sneakingBefore = false;
    private long lastSneakStartTime = -1;
    private long lastDeveloperCheckTime = -1;
    
    
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

                boolean inCrystalHollows = Util.inCrystalHollows();

                
                if (inCrystalHollows) {
                    
                    boolean sneaking = player.isSneaking();
                    if (!sneakingBefore && sneaking) {
                        lastSneakStartTime = now;
                    }
                    sneakingBefore = sneaking;
                    
                    
                    // Pre-release notice 
                    
                    if (!inCrystalHollowsBefore) 
                        Util.sendModChatMessage(EnumChatFormatting.RED.toString() + EnumChatFormatting.ITALIC + "This is a pre-release version of 1.2! Please report any problems you find on the scatha farming discord!" + EnumChatFormatting.RESET);
                    
                    
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
                                newWorm = new Worm(entityID, false);
                            else if (StringUtils.stripControlCodes(entityName).contains("[Lv10] Scatha "))
                                newWorm = new Worm(entityID, true);
                            
                            if (newWorm != null) {
                                scathaPro.activeWorms.add(newWorm);
                                
                                if (!isRegisteredWorm) {
                                    scathaPro.registeredWorms.add(newWorm.entityID);
                                    
                                    if (newWorm.isScatha) {
                                        if (scathaPro.wormStreak < 0) scathaPro.wormStreak = 0;
                                        scathaPro.wormStreak ++;
                                        
                                        if (now - scathaPro.lastWorldJoinTime <= Achievement.scatha_spawn_time.goal * 60 * 1000) 
                                            Achievement.scatha_spawn_time.setProgress(Achievement.scatha_spawn_time.goal);
                                        if (e.posY > 186)
                                            Achievement.scatha_spawn_chtop.setProgress(Achievement.scatha_spawn_chtop.goal);
                                        
                                        if (Config.instance.getBoolean(Config.Key.scathaAlert)) {
                                            mc.ingameGUI.displayTitle(null, null, 0, 40, 10);
                                            mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Pray to RNGesus!", 0, 0, 0);
                                            mc.ingameGUI.displayTitle(EnumChatFormatting.RED + "Scatha", null, 0, 0, 0);
                                            
                                            if (!Util.playModeSound("alert.scatha")) Util.playSoundAtPlayer("random.levelup", 1f, 0.8f);
                                        }
                                    }
                                    else {
                                        if (scathaPro.wormStreak > 0) scathaPro.wormStreak = 0;
                                        scathaPro.wormStreak --;
    
                                        if (Config.instance.getBoolean(Config.Key.wormAlert)) {
                                            mc.ingameGUI.displayTitle(null, null, 5, 20, 5);
                                            mc.ingameGUI.displayTitle(null, EnumChatFormatting.GRAY + "Just a regular worm...", 0, 0, 0);
                                            mc.ingameGUI.displayTitle(EnumChatFormatting.YELLOW + "Worm", null, 0, 0, 0);
                    
                                            if (!Util.playModeSound("alert.worm")) Util.playSoundAtPlayer("random.levelup", 1f, 0.5f);
                                        }
                                    }
    
                                    scathaPro.updateSpawnAchievements();
                                    
                                    OverlayManager.instance.updateWormStreak();
                                }
                            }
                        }
                    }
                    
                    
                    // Projectile worm hit detection
                    
                    List<EntityArrow> arrows = world.getEntities(EntityArrow.class, new Predicate<EntityArrow>() {
                        @Override
                        public boolean apply(EntityArrow input) {
                            Minecraft mc = Minecraft.getMinecraft();
                            EntityPlayer player = mc != null ? Minecraft.getMinecraft().thePlayer : null;
                            return player != null ? input.shootingEntity == player : false;
                        }
                    });
                    for (int i = 0; i < arrows.size(); i ++) {
                        EntityArrow arrow = arrows.get(i);
                        List<EntityArmorStand> hitArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(arrow.posX, arrow.posY, arrow.posZ, arrow.posX, arrow.posY, arrow.posZ).expand(3f, 3f, 3f));
                        for (int j = 0; j < hitArmorStands.size(); j ++) {
                            EntityArmorStand armorStand = hitArmorStands.get(j);
                            Worm worm = Worm.getByID(armorStand.getEntityId());
                            if (worm != null) worm.attack(scathaPro.lastProjectileWeaponUsed);
                        }
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
                        int entityID = worm.entityID;
                        
                        if (world.getEntityByID(entityID) == null) {
                            long lifetime = worm.getLifetime();
                            
                            if (now - worm.getLastAttackTime() < 1500) {
                                
                                if (worm.isScatha) {
                                    scathaPro.scathaKills ++;
                                    if (scathaPro.overallScathaKills >= 0) scathaPro.overallScathaKills ++;
                                    
                                    if (worm.getHitWeaponsCount() >= Achievement.kill_weapons_scatha.goal) Achievement.kill_weapons_scatha.setProgress(Achievement.kill_weapons_scatha.goal);
                                    if (worm.getHitWeaponsCount() > 0 && worm.getHitWeapons()[worm.getHitWeaponsCount() - 1].equals("TERMINATOR")) Achievement.scatha_kill_terminator.setProgress(Achievement.scatha_kill_terminator.goal);
                                    if (sneaking && lastSneakStartTime >= 0 && worm.spawnTime >= lastSneakStartTime) Achievement.scatha_kill_sneak.setProgress(Achievement.scatha_kill_sneak.goal);
                                    
                                    lastKillIsScatha = true;
                                    
                                    OverlayManager.instance.updateScathaKills();
                                }
                                else {
                                    scathaPro.regularWormKills ++;
                                    if (scathaPro.overallRegularWormKills >= 0) scathaPro.overallRegularWormKills ++;
                                    
                                    if (worm.getHitWeaponsCount() >= Achievement.kill_weapons_regular_worm.goal) Achievement.kill_weapons_regular_worm.setProgress(Achievement.kill_weapons_regular_worm.goal);
                                    
                                    lastKillIsScatha = false;
    
                                    OverlayManager.instance.updateWormKills();
                                }
                                
                                OverlayManager.instance.updateTotalKills();
                                
                                scathaPro.updateKillAchievements();
                                
                                if (lifetime <= Achievement.worm_kill_time_1.goal * 1000) Achievement.worm_kill_time_1.setProgress(Achievement.worm_kill_time_1.goal);
                                else if (lifetime >= Achievement.worm_kill_time_2.goal * 1000) Achievement.worm_kill_time_2.setProgress(Achievement.worm_kill_time_2.goal);
                                
                                lastKillTime = now;
                            }
                            else {
                                if (lifetime >= 29 * 1000) Achievement.worm_despawn.setProgress(Achievement.worm_despawn.goal);
                            }
                            
                            scathaPro.activeWorms.remove(worm);
                        }
                    }
                    
                    
                    // Bedrock wall detection
    
                    if (Config.instance.getBoolean(Config.Key.wallAlert)) {
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
                                    
                                    if ((skyblockItemID != null && skyblockItemID.equals("PET") || Config.instance.getBoolean(Config.Key.devMode)) && StringUtils.stripControlCodes(displayName).contains("Scatha")) {
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
                    
                    if (scathaPro.lastWormAttackTime >= 0 && now - scathaPro.lastWormAttackTime < 1000 && scathaPro.previousScathaPets != null) {
                        
                        int newScathaPet = -1;
                        
                        for (Integer rarityID : currentScathaPets.keySet()) {
                            int currentRarityCount = currentScathaPets.get(rarityID);
                            Integer previousRarityCount = scathaPro.previousScathaPets.get(rarityID);
                            int difference = currentRarityCount - (previousRarityCount != null ? previousRarityCount : 0);
                            if (difference > 0 && rarityID > newScathaPet) newScathaPet = rarityID;
                        }
                        
                        if (newScathaPet >= 0) {
                            String rarityTitle = null;
                            
                            switch (newScathaPet) {
                                case 1:
                                    scathaPro.rarePetDrops ++;
                                    rarityTitle = EnumChatFormatting.BLUE + "RARE";
                                    break;
                                case 2:
                                    scathaPro.epicPetDrops ++;
                                    rarityTitle = EnumChatFormatting.DARK_PURPLE + "EPIC";
                                    break;
                                case 3:
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
                                
                                Util.playSoundAtPlayer("random.chestopen", 1.5f, 0.95f);
                                
                                if (!Util.playModeSound("alert.pet_drop")) Util.playSoundAtPlayer("mob.wither.death", 0.75f, 0.8f);
                            }
                            
                            scathaPro.updatePetDropAchievements();
                            
                            if (droppedPetAtLastScatha) Achievement.scatha_pet_drop_b2b.setProgress(Achievement.scatha_pet_drop_b2b.goal);
                            droppedPetAtLastScatha = true;
                            lastPetDropTime = now;
                            
                            PersistentData.instance.savePetDrops();
                            
                            OverlayManager.instance.updatePetDrops();
                        }
                    }
                    
                    scathaPro.previousScathaPets = currentScathaPets;
                    
                    
                    if (droppedPetAtLastScatha && lastKillIsScatha && now - lastKillTime > 1000 && lastPetDropTime < lastKillTime) droppedPetAtLastScatha = false;
                    
                    
                    // Update UI overlay
                    
                    if (Config.instance.getBoolean(Config.Key.overlay) && !Minecraft.getMinecraft().gameSettings.showDebugInfo) {
                        OverlayManager.instance.updateCoords();
                        OverlayManager.instance.updateDay();
                        
                        OverlayManager.instance.updatePosition();
                    }
                    
                    
                    // Achievements
                    
                    float hours = (now - scathaPro.lastWorldJoinTime) / (1000f*60*60);
                    Achievement.crystal_hollows_time_1.setProgress(hours);
                    Achievement.crystal_hollows_time_2.setProgress(hours);
                    Achievement.crystal_hollows_time_3.setProgress(hours);
                    
                    
                    // Open fake ban screen
                    
                    if (mc.currentScreen == null && scathaPro.showFakeBan) {
                        scathaPro.openGuiNextTick = new FakeBanGui();
                        
                        scathaPro.showFakeBan = false;
                    }
                    
                }

                inCrystalHollowsBefore = inCrystalHollows;
                
                
                // Dev check
                
                if (now - lastDeveloperCheckTime >= 1000) {
                    NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
                    if (netHandler != null) {
                        Collection<NetworkPlayerInfo> playerInfos = netHandler.getPlayerInfoMap();
                        
                        for (Iterator<NetworkPlayerInfo> iterator = playerInfos.iterator(); iterator.hasNext();) {
                            NetworkPlayerInfo p = iterator.next();
                            
                            if (Util.isDeveloper(p)) Achievement.meet_developer.setProgress(Achievement.meet_developer.goal);
                        }
                    }
                    
                    lastDeveloperCheckTime = now;
                }
                
            }
        }
    }
}
