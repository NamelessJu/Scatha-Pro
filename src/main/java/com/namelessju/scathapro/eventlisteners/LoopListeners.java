package com.namelessju.scathapro.eventlisteners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.gson.JsonObject;
import com.namelessju.scathapro.Constants;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.achievements.Achievement;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedEntity;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import com.namelessju.scathapro.events.BedrockWallDetectedEvent;
import com.namelessju.scathapro.events.NewIRLDayStartedEvent;
import com.namelessju.scathapro.events.TickEvent.CrystalHollowsTickEvent;
import com.namelessju.scathapro.events.TickEvent.FirstCrystalHollowsTickEvent;
import com.namelessju.scathapro.events.TickEvent.FirstIngameTickEvent;
import com.namelessju.scathapro.events.TickEvent.FirstWorldTickEvent;
import com.namelessju.scathapro.events.ScathaPetDropEvent;
import com.namelessju.scathapro.events.SkyblockAreaDetectedEvent;
import com.namelessju.scathapro.gui.menus.FakeBanGui;
import com.namelessju.scathapro.managers.Config;
import com.namelessju.scathapro.miscellaneous.PetDrop;
import com.namelessju.scathapro.miscellaneous.enums.Rarity;
import com.namelessju.scathapro.miscellaneous.enums.SkyblockArea;
import com.namelessju.scathapro.overlay.elements.OverlayContainer;
import com.namelessju.scathapro.overlay.elements.OverlayElement.Alignment;
import com.namelessju.scathapro.overlay.elements.OverlayImage;
import com.namelessju.scathapro.overlay.elements.OverlayText;
import com.namelessju.scathapro.parsing.PlayerListParser;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.NBTUtil;
import com.namelessju.scathapro.util.TimeUtil;
import com.namelessju.scathapro.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
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
    
    
    private boolean fakeBanScreenPending = true;
    private boolean firstIngameTickPending = true;
    
    private long lastDeveloperCheckTime = -1;
    
    private int distanceToWallPrevious = -1;
    private long lastBedrockDetectionTime = -1;
    private boolean bedrockDetectedThisDirection = false;
    private int bedrockDirectionBefore = -1;
    
    private List<PetDrop> receivedPets = new ArrayList<PetDrop>();
    private HashMap<Integer, String> arrowOwners = new HashMap<Integer, String>();
    
    private boolean wormSpawnCooldownRunningBefore = false;
    
    private int dailyStatsCheckTickTimer = 0;
    
    
    public LoopListeners(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
        mc = scathaPro.getMinecraft();
        
        rotationAnglesOverlay = new OverlayContainer(0, 0, 0.75f);
        rotationAnglesOverlay.add(yawText = new OverlayText(EnumChatFormatting.OBFUSCATED + "?", Util.Color.WHITE, 10, -3, 1f));
        rotationAnglesOverlay.add(pitchText = new OverlayText(EnumChatFormatting.OBFUSCATED + "?", Util.Color.WHITE, 0, 10, 1f));
        pitchText.setAlignment(Alignment.CENTER);
        
        rotationLockOverlay = new OverlayImage("lock.png", 16, 16, -8, -20, 1f);
    }
    
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event)
    {
        if (event.type != ElementType.TEXT) return;
        
        // Overlay
        scathaPro.getOverlay().drawOverlayIfAllowed();
        
        EntityPlayer player = mc.thePlayer;
        if (player != null)
        {
            // Alert titles
            scathaPro.getAlertTitleOverlay().draw(event.partialTicks);
            
            
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
    
    private void updateRotationAngles(EntityPlayer player)
    {
        int decimalDigits = scathaPro.getConfig().getInt(Config.Key.rotationAnglesDecimalDigits);
        
        float yaw = player.rotationYaw % 360f;
        if (yaw < 0) yaw += 360f;
        String yawString = TextUtil.numberToString(yaw, decimalDigits, true);
        if (scathaPro.getConfig().getBoolean(Config.Key.rotationAnglesMinimalYaw))
        {
            int dotIndex = yawString.indexOf('.');
            if (dotIndex >= 0) yawString = yawString.substring(dotIndex - 1);
        }
        yawText.setText(TextUtil.contrastableGray() + yawString);
        
        if (scathaPro.getConfig().getBoolean(Config.Key.rotationAnglesYawOnly))
        {
            pitchText.setVisible(false);
        }
        else
        {
            pitchText.setVisible(true);
            pitchText.setText(TextUtil.contrastableGray() + TextUtil.numberToString(player.rotationPitch, decimalDigits, true));
        }
    }
    
    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END) return;

        if (scathaPro.variables.runAfterNextRender.size() > 0)
        {
            for (Runnable runnable : scathaPro.variables.runAfterNextRender)
            {
                runnable.run();
            }
            scathaPro.variables.runAfterNextRender.clear();
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) return;
        
        dailyStatsCheckTickTimer --;
        if (dailyStatsCheckTickTimer <= 0)
        {
            if (scathaPro.variables.lastPlayedDate == null || !scathaPro.variables.lastPlayedDate.equals(TimeUtil.today()))
            {
                MinecraftForge.EVENT_BUS.post(new NewIRLDayStartedEvent());
            }

            dailyStatsCheckTickTimer = 20;
        }
        
        if (scathaPro.variables.runNextTick.size() > 0)
        {
            for (Runnable runnable : scathaPro.variables.runNextTick)
            {
                runnable.run();
            }
            scathaPro.variables.runNextTick.clear();
        }
        
        if (scathaPro.variables.openGuiNextTick != null)
        {
            mc.displayGuiScreen(scathaPro.variables.openGuiNextTick);
            scathaPro.variables.openGuiNextTick = null;
        }
        
        if (scathaPro.variables.aprilFoolsJokeRevealTickTimer > 0)
        {
            scathaPro.variables.aprilFoolsJokeRevealTickTimer --;
            
            if (scathaPro.variables.aprilFoolsJokeRevealTickTimer <= 0)
            {
                Alert.scathaPetDrop.stopSound();
                TextUtil.displayTitle(TextUtil.getRainbowText("April Fools"), EnumChatFormatting.GRAY + "It's that day of the year...", 3, 60, 20);
                
                Achievement.april_fools.unlock();
                
                TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Hopefully the fake pet drop didn't disappoint you too much, sorry!\nKeep farming and you could get a real one very soon!");
                
                scathaPro.variables.overlayIconGooglyEyesUnlocked = true;
                TextUtil.sendChatDivider();
                TextUtil.sendModChatMessage(EnumChatFormatting.YELLOW + "Overlay icon googly eyes permanently unlocked!\n" + EnumChatFormatting.GRAY + "You can from now on toggle them freely under " + ScathaPro.DYNAMIC_MODNAME + " Settings > Miscellaneous.");
                TextUtil.sendChatDivider();
                
                scathaPro.variables.lastAprilFoolsJokeShownYear = TimeUtil.getCurrentYear();
                scathaPro.getPersistentData().saveMiscData();
            }
        }
        
        final EntityPlayer player = mc.thePlayer;
        final World world = player != null ? player.worldObj : null;
        if (world != null)
        {
            scathaPro.getAlertTitleOverlay().tick();
            
            long now = TimeUtil.now();
            
            if (fakeBanScreenPending && mc.currentScreen == null)
            {
                if (scathaPro.variables.cheaterDetected)
                {
                    mc.displayGuiScreen(new FakeBanGui("Savefile Manipulation", () -> {
                        Achievement.cheat.unlock();
                        TextUtil.displayTitle("", EnumChatFormatting.GREEN + "We do a little trolling", 5, 60, 40);
                    }));
                }
                
                fakeBanScreenPending = false;
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
            
            
            scathaPro.getChestGuiParsingManager().tick();
            
            
            if (scathaPro.isInCrystalHollows())
            {
                if (scathaPro.variables.firstCrystalHollowsTickPending && mc.currentScreen == null)
                {
                    MinecraftForge.EVENT_BUS.post(new FirstCrystalHollowsTickEvent());
                    scathaPro.variables.firstCrystalHollowsTickPending = false;
                }
                
                MinecraftForge.EVENT_BUS.post(new CrystalHollowsTickEvent(now));
                
                
                if (scathaPro.getOverlay().isOverlayDrawAllowed())
                {
                    scathaPro.getOverlay().updateRealtimeElements();
                }
                
                
                // Entity detection
                
                DetectedEntity.update(player);
                
                // Worm arrow hits
                
                ArrayList<Integer> arrowIds = new ArrayList<Integer>(arrowOwners.keySet());
                for (int i = 0; i < arrowOwners.size(); i ++)
                {
                    int arrowID = arrowIds.get(i);
                    if (world.getEntityByID(arrowID) == null) arrowOwners.remove(arrowID);
                }
                
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
                
                // Worm fishing hook hits
                
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
                
                
                // Bedrock detection
                
                boolean bedrockDetected = false;
                
                BlockPos playerBlockPos = Util.entityBlockPos(player);
                int distanceToWall = -1;
                // BlockPos checkBlockPosBottom = null;
                
                int playerDirection = Util.getDirection(player);
                switch (playerDirection)
                {
                    case 0: // -Z
                        distanceToWall = playerBlockPos.getZ() - Constants.crystalHollowsBoundsMin;
                        // checkBlockPosBottom = playerBlockPos.north();
                        break;
                    case 1: // +X
                        distanceToWall = Constants.crystalHollowsBoundsMax - playerBlockPos.getX();
                        // checkBlockPosBottom = playerBlockPos.east();
                        break;
                    case 2: // +Z
                        distanceToWall = Constants.crystalHollowsBoundsMax - playerBlockPos.getZ();
                        // checkBlockPosBottom = playerBlockPos.south();
                        break;
                    case 3: // -X
                        distanceToWall = playerBlockPos.getX() - Constants.crystalHollowsBoundsMin;
                        // checkBlockPosBottom = playerBlockPos.west();
                        break;
                }
                distanceToWall -= 1; // being next to the wall should be 0 distance
                
                if (bedrockDirectionBefore >= 0 && bedrockDirectionBefore != playerDirection)
                {
                    distanceToWallPrevious = -1;
                    bedrockDetectedThisDirection = false;
                }
                bedrockDirectionBefore = playerDirection;
                
                int triggerDistance = scathaPro.getConfig().getInt(Config.Key.bedrockWallAlertTriggerDistance);
                
                if (distanceToWallPrevious >= 0 && distanceToWallPrevious - distanceToWall == 1)
                {
                    if (distanceToWall < triggerDistance
                        /* ||
                        checkBlockPosBottom != null &&
                        (world.getBlockState(checkBlockPosBottom).getBlock() == Blocks.bedrock
                        || world.getBlockState(checkBlockPosBottom.up()).getBlock() == Blocks.bedrock) */
                    ) {
                        bedrockDetected = true;
                    }
                }
                if (distanceToWall >= triggerDistance) bedrockDetectedThisDirection = false;
                distanceToWallPrevious = distanceToWall;
                
                if (bedrockDetected && !bedrockDetectedThisDirection && (lastBedrockDetectionTime < 0 || now - lastBedrockDetectionTime > 1500))
                {
                    bedrockDetectedThisDirection = true;
                    
                    lastBedrockDetectionTime = now;
                    MinecraftForge.EVENT_BUS.post(new BedrockWallDetectedEvent());
                }
                
                
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
                        Rarity rarity = null;
                        
                        switch (newScathaPet)
                        {
                            case 1:
                                rarity = Rarity.RARE;
                                break;
                            case 2:
                                rarity = Rarity.EPIC;
                                break;
                            case 3:
                                rarity = Rarity.LEGENDARY;
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
            
            
            if (scathaPro.variables.anomalousDesireReadyTime >= 0L && now >= scathaPro.variables.anomalousDesireReadyTime)
            {
                if (scathaPro.variables.wormSpawnCooldownStartTime >= 0L && now - scathaPro.variables.wormSpawnCooldownStartTime < Constants.wormSpawnCooldown) // spawn cooldown still running
                {
                    // delay ability ready time until after cooldown runs out
                    scathaPro.variables.anomalousDesireReadyTime = scathaPro.variables.wormSpawnCooldownStartTime + Constants.wormSpawnCooldown + 1000;
                }
                else
                {
                    scathaPro.variables.anomalousDesireReadyTime = -1L;
                    if (scathaPro.isInCrystalHollows()) Alert.anomalous_desire_ready.play();
                }
            }

            if (scathaPro.variables.anomalousDesireStartTime >= 0L && now - scathaPro.variables.anomalousDesireStartTime >= Constants.anomalousDesireEffectDuration)
            {
                scathaPro.variables.anomalousDesireStartTime = -1L;
            }
            if (scathaPro.variables.anomalousDesireCooldownEndTime >= 0L && now >= scathaPro.variables.anomalousDesireCooldownEndTime)
            {
                scathaPro.variables.anomalousDesireCooldownEndTime = -1L;
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
        
        String areaName = PlayerListParser.parseArea();
        if (areaName != null)
        {
            if (areaName.equalsIgnoreCase("Crystal Hollows")) return SkyblockArea.CRYSTAL_HOLLOWS;
            return SkyblockArea.OTHER;            
        }
        return null;
    }
}
