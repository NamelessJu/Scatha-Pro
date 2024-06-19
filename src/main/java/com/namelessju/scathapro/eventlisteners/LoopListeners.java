package com.namelessju.scathapro.eventlisteners;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.gson.JsonObject;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedEntity;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import com.namelessju.scathapro.events.BedrockDetectedEvent;
import com.namelessju.scathapro.events.TickEvent.CrystalHollowsTickEvent;
import com.namelessju.scathapro.events.TickEvent.FirstCrystalHollowsTickEvent;
import com.namelessju.scathapro.events.TickEvent.FirstIngameTickEvent;
import com.namelessju.scathapro.events.TickEvent.FirstWorldTickEvent;
import com.namelessju.scathapro.events.ScathaPetDropEvent;
import com.namelessju.scathapro.events.SkyblockAreaDetectedEvent;
import com.namelessju.scathapro.gui.menus.FakeBanGui;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.PetDrop;
import com.namelessju.scathapro.miscellaneous.SkyblockArea;
import com.namelessju.scathapro.overlay.elements.OverlayContainer;
import com.namelessju.scathapro.overlay.elements.OverlayElement.Alignment;
import com.namelessju.scathapro.overlay.elements.OverlayImage;
import com.namelessju.scathapro.overlay.elements.OverlayText;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.MessageUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.TimeUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class LoopListeners
{
    private final ScathaPro scathaPro;
    private final Minecraft mc;
    
    
    private final OverlayContainer rotationAnglesOverlay;
    private final OverlayText yawText;
    private final OverlayText pitchText;
    private final OverlayImage rotationLockOverlay;
    
    
    private boolean firstIngameTickPending = true;
    private boolean firstCrystalHollowsFramePending = true;
    
    private long lastDeveloperCheckTime = -1;
    
    private long lastBedrockDetectionTime = -1;
    private boolean bedrockDetectedBefore = false;
    private int bedrockFacingBefore = -1;

    private List<PetDrop> receivedPets = new ArrayList<PetDrop>();
    private HashMap<Integer, String> arrowOwners = new HashMap<Integer, String>();
    
    private GuiChest lastChestCheckedForKillInfo = null;
    
    private boolean wormSpawnCooldownRunningBefore = false;
    
    private int dailyStatsCheckTickTimer = 0;
    
    
    public LoopListeners(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.getMinecraft();
        
        rotationAnglesOverlay = new OverlayContainer(0, 0, 0.75f);
        rotationAnglesOverlay.add(yawText = new OverlayText(EnumChatFormatting.OBFUSCATED + "?", Util.Color.WHITE.getValue(), 10, -4, 1f));
        rotationAnglesOverlay.add(pitchText = new OverlayText(EnumChatFormatting.OBFUSCATED + "?", Util.Color.WHITE.getValue(), 0, 10, 1f));
        pitchText.setAlignment(Alignment.CENTER);
        
        rotationLockOverlay = new OverlayImage("lock.png", 16, 16, -8, -20, 1f);
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event)
    {
        if (event.type == ElementType.TEXT)
        {
            // Overlay
            
            scathaPro.getOverlay().tryDrawOverlay();
            
            EntityPlayer player = mc.thePlayer;
            if (player != null)
            {
                ScaledResolution scaledResolution = new ScaledResolution(mc);
                GlStateManager.pushMatrix();
                GlStateManager.translate(Math.round(scaledResolution.getScaledWidth() / 2), Math.round(scaledResolution.getScaledHeight() / 2), 0);
                
                
                // Rotation Angles
                
                if (scathaPro.getConfig().getBoolean(Config.Key.showRotationAngles))
                {
                    updateRotationAngles(player);
                    rotationAnglesOverlay.draw();
                }
                
                // Rotation Lock
                
                if (scathaPro.getInputManager().isRotationLocked()) rotationLockOverlay.draw();

                
                GlStateManager.popMatrix();
            }
        }
    }
    
    private void updateRotationAngles(EntityPlayer player)
    {
        int decimalDigits = scathaPro.getConfig().getInt(Config.Key.rotationAnglesDecimalDigits);
        
        float yaw = player.rotationYaw % 360;
        if (yaw < 0) yaw += 360;
        yawText.setText(MessageUtil.contrastableGray() + Util.numberToString(yaw, decimalDigits, true));
        
        pitchText.setText(MessageUtil.contrastableGray() + Util.numberToString(player.rotationPitch, decimalDigits, true));
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;
        
        dailyStatsCheckTickTimer --;
        if (dailyStatsCheckTickTimer <= 0)
        {
            scathaPro.handleDailyStatsReset();
            dailyStatsCheckTickTimer = 20;
        }
        
        final EntityPlayer player = mc.thePlayer;
        final World world = player != null ? player.worldObj : null;
        if (world != null)
        {
            long now = TimeUtil.now();
            
            
            if (scathaPro.variables.openGuiNextTick != null)
            {
                mc.displayGuiScreen(scathaPro.variables.openGuiNextTick);
                scathaPro.variables.openGuiNextTick = null;
            }
            
            
            if (scathaPro.variables.cheaterDetected && mc.currentScreen == null)
            {
                mc.displayGuiScreen(new FakeBanGui());
                scathaPro.variables.cheaterDetected = false;
            }
            
            if (mc.currentScreen == null)
            {
                if (firstIngameTickPending)
                {
                    MinecraftForge.EVENT_BUS.post(new FirstIngameTickEvent());
                    firstIngameTickPending = false;
                }
                
                if (scathaPro.variables.firstWorldTickPending)
                {
                    MinecraftForge.EVENT_BUS.post(new FirstWorldTickEvent(player));
                    scathaPro.variables.firstWorldTickPending = false;
                }
            }
            
            
            if (scathaPro.variables.currentAreaCheckTimeIndex >= 0 && scathaPro.variables.currentAreaCheckTimeIndex < Constants.postWorldJoinAreaCheckTimes.length && Constants.postWorldJoinAreaCheckTimes.length > 0)
            {
                int checkTime = Constants.postWorldJoinAreaCheckTimes[scathaPro.variables.currentAreaCheckTimeIndex];
                if (now - scathaPro.variables.lastWorldJoinTime >= checkTime)
                {
                    SkyblockArea area = checkForArea();
                    
                    if (area == null)
                    {
                        scathaPro.variables.currentAreaCheckTimeIndex ++;
                        
                        if (scathaPro.variables.currentAreaCheckTimeIndex >= Constants.postWorldJoinAreaCheckTimes.length)
                        {
                            scathaPro.logDebug("No area detected, ran out of tries");
                        }
                    }
                    else
                    {
                        scathaPro.variables.currentArea = area;
                        
                        if (area == SkyblockArea.NONE) scathaPro.logDebug("No area detected, not in Skyblock");
                        else
                        {
                            scathaPro.logDebug("Area detected: " + area.name() + " - " + checkTime + " ms after world join on try " + (scathaPro.variables.currentAreaCheckTimeIndex + 1) + "/" + Constants.postWorldJoinAreaCheckTimes.length);
                            
                            MinecraftForge.EVENT_BUS.post(new SkyblockAreaDetectedEvent(area));
                        }
                        
                        scathaPro.variables.currentAreaCheckTimeIndex = -1;
                    }
                }
            }
            
            if (scathaPro.getConfig().getBoolean(Config.Key.automaticStatsParsing)) parseOpenedChest();
            
            
            if (scathaPro.isInCrystalHollows())
            {
                if (firstCrystalHollowsFramePending && mc.currentScreen == null)
                {
                    MinecraftForge.EVENT_BUS.post(new FirstCrystalHollowsTickEvent());
                    firstCrystalHollowsFramePending = false;
                }
                
                MinecraftForge.EVENT_BUS.post(new CrystalHollowsTickEvent(now));
                
                
                // Entity detection
                
                DetectedEntity.update(player);
                
                
                // Worm projectile hits
                
                // Arrows
                List<EntityArrow> arrows = world.getEntities(EntityArrow.class, new Predicate<EntityArrow>() {
                    @Override
                    public boolean apply(EntityArrow input)
                    {
                        return !input.onGround;
                    }
                });
                
                for (int i = 0; i < arrows.size(); i ++)
                {
                    EntityArrow arrow = arrows.get(i);
                    int id = arrow.getEntityId();
                    
                    if (!arrowOwners.containsKey(id))
                    {
                        EntityPlayer owner = world.getClosestPlayerToEntity(arrow, -1);
                        arrowOwners.put(id, Util.getUUIDString(owner.getUniqueID()));
                    }
                    
                    if (arrowOwners.get(id).equals(Util.getPlayerUUIDString()))
                    {
                        List<EntityArmorStand> hitArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(arrow.posX, arrow.posY, arrow.posZ, arrow.posX, arrow.posY, arrow.posZ).expand(3f, 3f, 3f));
                        for (int j = 0; j < hitArmorStands.size(); j ++)
                        {
                            EntityArmorStand armorStand = hitArmorStands.get(j);
                            DetectedWorm worm = DetectedWorm.getById(armorStand.getEntityId());
                            if (worm != null) worm.attack(scathaPro.variables.lastProjectileWeaponUsed);
                        }
                    }
                }
                
                for (int i = 0; i < arrowOwners.size(); i ++)
                {
                    int arrowID = new ArrayList<Integer>(arrowOwners.keySet()).get(i);
                    if (world.getEntityByID(arrowID) == null) arrowOwners.remove(arrowID);
                }
                
                // Fishing hooks
                List<EntityFishHook> fishHooks = world.getEntities(EntityFishHook.class, new Predicate<EntityFishHook>() {
                    @Override
                    public boolean apply(EntityFishHook input)
                    {
                        EntityPlayer player = mc != null ? Minecraft.getMinecraft().thePlayer : null;
                        return player != null ? input.angler == player : false;
                    }
                });
                
                for (int i = 0; i < fishHooks.size(); i ++)
                {
                    EntityFishHook hook = fishHooks.get(i);
                    List<EntityArmorStand> hookedArmorStands = world.getEntitiesWithinAABB(EntityArmorStand.class, new AxisAlignedBB(hook.posX, hook.posY, hook.posZ, hook.posX, hook.posY, hook.posZ).expand(3f, 3f, 3f));
                    for (int j = 0; j < hookedArmorStands.size(); j ++)
                    {
                        EntityArmorStand armorStand = hookedArmorStands.get(j);
                        DetectedWorm worm = DetectedWorm.getById(armorStand.getEntityId());
                        if (worm != null) worm.attack(scathaPro.variables.lastProjectileWeaponUsed);
                    }
                }
                
                
                // Bedrock wall detection
                
                int[] checkDirection = {0, 0};
                int facing = Util.getFacing(player);
                switch (facing)
                {
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
                
                if (bedrockFacingBefore >= 0 && bedrockFacingBefore != facing) bedrockDetectedBefore = false;
                bedrockFacingBefore = facing;
                
                boolean bedrockDetected = false;
                boolean viewBlockedLower = false, viewBlockedUpper = false;
                BlockPos playerPos = Util.entityBlockPos(player);
                
                for (int i = 0; i < 10; i ++)
                {
                    boolean bedrockFoundLower = false, bedrockFoundUpper = false;
                    
                    BlockPos lowerBlockPos = playerPos.add(checkDirection[0] * i, 0, checkDirection[1] * i);
                    
                    Block upperBlock = world.getBlockState(lowerBlockPos.add(0, 1, 0)).getBlock();
                    if (upperBlock == Blocks.bedrock)
                    {
                        bedrockFoundUpper = true;
                        if (viewBlockedUpper) bedrockDetected = true;
                    }
                    else if (upperBlock != Blocks.air) viewBlockedUpper = true;
                    
                    Block lowerBlock = world.getBlockState(lowerBlockPos).getBlock();
                    if (lowerBlock == Blocks.bedrock)
                    {
                        bedrockFoundLower = true;
                        if (viewBlockedLower && !bedrockFoundUpper) bedrockDetected = true;
                    }
                    else if (lowerBlock != Blocks.air) viewBlockedLower = true;
                    
                    if (bedrockFoundLower || bedrockFoundUpper) break;
                }
                
                if (bedrockDetected)
                {
                    if (!bedrockDetectedBefore)
                    {
                        bedrockDetectedBefore = true;
                        
                        if (lastBedrockDetectionTime < 0 || now - lastBedrockDetectionTime > 1500)
                        {
                            lastBedrockDetectionTime = now;
                            MinecraftForge.EVENT_BUS.post(new BedrockDetectedEvent());
                        }
                    }
                }
                else bedrockDetectedBefore = false;
                
                
                // Scatha pet drop detection
                
                ItemStack[] inventory = player.inventory.mainInventory;
                
                HashMap<Integer, Integer> currentScathaPets = new HashMap<Integer, Integer>();

                for (int i = 0; i < inventory.length; i++)
                {
                    if (i == 8) continue; // No need to check the Skyblock menu :)
                    
                    ItemStack item = inventory[i];
                    if (item == null) continue;
                    
                    String skyblockItemID = NBTUtil.getSkyblockItemID(item);
                    if (skyblockItemID == null || !skyblockItemID.equals("PET")) continue;
                    
                    NBTTagCompound skyblockNbt = NBTUtil.getSkyblockTagCompound(item);
                    if (skyblockNbt == null) continue;
                    
                    JsonObject petInfo = JsonUtil.parseObject(skyblockNbt.getString("petInfo"));
                    if (petInfo == null) continue;
                    
                    String petType = JsonUtil.getString(petInfo, "type");
                    if (petType == null || !petType.equals("SCATHA")) continue;
                    
                    int rarity = 0;
                    
                    String petTier = JsonUtil.getString(petInfo, "tier");
                    if (petTier != null)
                    {
                        if (petTier.equals("RARE")) rarity = 1;
                        else if (petTier.equals("EPIC")) rarity = 2;
                        else if (petTier.equals("LEGENDARY")) rarity = 3;
                    }
                    
                    Integer currentRarityAmount = currentScathaPets.get(rarity);
                    currentScathaPets.put(rarity, (currentRarityAmount != null ? currentRarityAmount : 0) + item.stackSize);
                }
                
                if (scathaPro.variables.previousScathaPets != null)
                {
                    int newScathaPet = -1;
                    
                    for (Integer rarityID : currentScathaPets.keySet())
                    {
                        int currentRarityCount = currentScathaPets.get(rarityID);
                        Integer previousRarityCount = scathaPro.variables.previousScathaPets.get(rarityID);
                        int difference = currentRarityCount - (previousRarityCount != null ? previousRarityCount : 0);
                        if (difference > 0 && rarityID > newScathaPet) newScathaPet = rarityID;
                    }
                    
                    if (newScathaPet >= 0)
                    {
                        PetDrop.Rarity rarity = PetDrop.Rarity.UNKNOWN;
                        
                        switch (newScathaPet)
                        {
                            case 1:
                                rarity = PetDrop.Rarity.RARE;
                                break;
                            case 2:
                                rarity = PetDrop.Rarity.EPIC;
                                break;
                            case 3:
                                rarity = PetDrop.Rarity.LEGENDARY;
                                break;
                        }

                        receivedPets.add(new PetDrop(rarity, now));
                    }
                }
                
                scathaPro.variables.previousScathaPets = currentScathaPets;
                
                for (int i = receivedPets.size() - 1; i >= 0; i --) {
                    PetDrop pet = receivedPets.get(i);
                    
                    if (now - pet.dropTime >= Constants.pingTreshold)
                    {
                        receivedPets.remove(i);
                        continue;
                    }
                    
                    if (scathaPro.variables.lastScathaKillTime >= 0 && now - scathaPro.variables.lastScathaKillTime < Constants.pingTreshold)
                    {
                        MinecraftForge.EVENT_BUS.post(new ScathaPetDropEvent(pet));
                        receivedPets.remove(i);
                    }
                }
                
                
                // Worm spawn cooldown
                
                if (scathaPro.variables.wormSpawnCooldownStartTime >= 0)
                {
                    if (now - scathaPro.variables.wormSpawnCooldownStartTime < Constants.wormSpawnCooldown)
                    {
                        wormSpawnCooldownRunningBefore = true;
                    }
                    else
                    {
                        if (wormSpawnCooldownRunningBefore) Alert.wormSpawnCooldownEnd.play();
                        wormSpawnCooldownRunningBefore = false;
                        scathaPro.variables.wormSpawnCooldownStartTime = -1;
                    }
                }
                else wormSpawnCooldownRunningBefore = false;
                    
            }
            
            
            // Dev check
            
            if (now - lastDeveloperCheckTime >= 1000)
            {
                checkForDev();
                
                lastDeveloperCheckTime = now;
            }
        }
    }
    
    private void checkForDev()
    {
        if (scathaPro.getAchievementManager().isAchievementUnlocked(Achievement.meet_developer)) return;
        
        NetHandlerPlayClient netHandler = mc.getNetHandler();
        if (netHandler == null) return;
        
        if (
            netHandler.getPlayerInfo(Constants.devUUID) != null
            ||
            (
                scathaPro.getConfig().getBoolean(Config.Key.devMode)
                &&
                netHandler.getPlayerInfo("JuCraft") != null
            )
        )
        {
            Achievement.meet_developer.unlock();
        }
    }
    
    private SkyblockArea checkForArea()
    {
        if (scathaPro.getConfig().getBoolean(Config.Key.devMode)) return SkyblockArea.CRYSTAL_HOLLOWS;
        
        if (mc.isSingleplayer()) return SkyblockArea.NONE;
        
        NetHandlerPlayClient netHandler = mc.getNetHandler();
        if (netHandler != null)
        {
            Collection<NetworkPlayerInfo> playerInfos = netHandler.getPlayerInfoMap();
            for (Iterator<NetworkPlayerInfo> iterator = playerInfos.iterator(); iterator.hasNext();)
            {
                NetworkPlayerInfo p = iterator.next();
                IChatComponent displayName = p.getDisplayName();
                if (displayName != null && displayName.getUnformattedText().contains("Area:"))
                {
                    if (displayName.getUnformattedText().contains("Crystal Hollows")) return SkyblockArea.CRYSTAL_HOLLOWS;
                    else return SkyblockArea.OTHER;
                }
            }
        }
        
        return null;
    }
    
    private void parseOpenedChest()
    {
        GuiScreen guiScreen = mc.currentScreen;
        if (guiScreen == null || !(guiScreen instanceof GuiChest)) return;
        GuiChest chestGui = (GuiChest) guiScreen;
        
        if (lastChestCheckedForKillInfo != null && lastChestCheckedForKillInfo == chestGui) return;
        
        IInventory chestInventory = ((ContainerChest) chestGui.inventorySlots).getLowerChestInventory();
        
        if (!chestInventory.hasCustomName())
        {
            lastChestCheckedForKillInfo = chestGui;
            return;
        }
        
        // Bestiary worms
        String wormBestiaryTitle = "Crystal Hollows " + MessageUtil.getUnicodeString("279C") + " Worm";
        
        if (chestInventory.getDisplayName().getUnformattedText().equals(wormBestiaryTitle))
        {
            int regularWormKills = parseWormKillsFromStack(chestInventory.getStackInSlot(21));
            int scathaKills = parseWormKillsFromStack(chestInventory.getStackInSlot(23));
            
            boolean killsUpdated = false;
            
            if (regularWormKills >= 0)
            {
                if (scathaPro.variables.regularWormKills != regularWormKills)
                {
                    scathaPro.variables.regularWormKills = regularWormKills;
                    scathaPro.getOverlay().updateWormKills();
                    killsUpdated = true;
                }
                else
                {
                    lastChestCheckedForKillInfo = chestGui;
                }
            }
            if (scathaKills >= 0)
            {
                if (scathaPro.variables.scathaKills != scathaKills)
                {
                    scathaPro.variables.scathaKills = scathaKills;
                    scathaPro.getOverlay().updateScathaKills();
                    killsUpdated = true;
                }
                else
                {
                    lastChestCheckedForKillInfo = chestGui;
                }
            }
            
            if (killsUpdated)
            {
                scathaPro.getPersistentData().saveWormKills();
                MessageUtil.sendModChatMessage("Updated overall worm kills from bestiary");
                lastChestCheckedForKillInfo = chestGui;
            }
        }
        else
        {
            lastChestCheckedForKillInfo = chestGui;
        }
    }
    
    private int parseWormKillsFromStack(ItemStack stack)
    {
        if (stack != null && stack.getTagCompound() != null)
        {
            NBTTagCompound displayTagCompound = stack.getTagCompound().getCompoundTag("display");
            if (displayTagCompound != null)
            {
                NBTTagList loreTagList = displayTagCompound.getTagList("Lore", 8);
                
                String killsLine = StringUtils.stripControlCodes(loreTagList.getStringTagAt(4));
                killsLine = killsLine.replace("Kills: ", "");
                killsLine = killsLine.replace(",", "");
                try
                {
                    int kills = Integer.parseInt(killsLine);
                    return kills;
                }
                catch (NumberFormatException ignored) {}
            }
        }
        
        return -1;
    }
}
