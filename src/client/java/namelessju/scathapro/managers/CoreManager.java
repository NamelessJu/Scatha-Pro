package namelessju.scathapro.managers;

import com.google.gson.JsonObject;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.entitydetection.detectedentity.DetectedWorm;
import namelessju.scathapro.gui.menus.screens.FakeBanScreen;
import namelessju.scathapro.miscellaneous.data.enums.OldLobbyAlertTriggerMode;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import namelessju.scathapro.miscellaneous.data.enums.SkyblockArea;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.miscellaneous.data.PetDrop;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CoreManager
{
    private final ScathaPro scathaPro;
    
    private @Nullable SkyblockArea currentArea = null;
    
    // TODO:
    // try to make as many of these fields private
    // by implementing fitting methods
    
    public boolean firstLevelTickPending = true;
    public boolean firstCrystalHollowsTickPending = true;
    
    public long lastWorldJoinTime = -1L;
    public long lastWormSpawnTime = -1L;
    public long wormSpawnCooldownStartTime = -1;
    public long lastWormKillTime = -1;
    public long lastScathaKillTime = -1;
    public long lastPetDropTime = -1;
    
    public HashMap<Rarity, Integer> previousScathaPets = null;
    public ItemStack lastProjectileWeaponUsed = null;
    
    public boolean crouchingBefore = false;
    public long lastCrouchStartTime = -1;
    
    public long lastPreAlertTime = -1L;
    /** Used for the high heat alert and doesn't get updated if the alert is disabled! */
    public int lastHeat = -1;
    public int lastOldLobbyAlertTriggerDay = -1;
    public int antiSleepAlertTickTimer = 0;
    public int nextAntiSleepAlertTriggerTickCount = -1;
    
    /** -1 = waiting for first time update packet; -2 = packet received, may now update this variable to the actual day */
    public int lastCrystalHollowsDay = -1;
    
    /** The time when the ability should be used (after spawn cooldown!) */
    public long tunnelVisionReadyTime = -1;
    /** The time when the ability is actually available again */
    public long tunnelVisionCooldownEndTime = -1;
    public long tunnelVisionStartTime = -1;
    public boolean tunnelVisionWastedForRecovery = false;
    
    public float avgMoneyCalcMagicFind = -1f;
    public float avgMoneyCalcPetLuck = -1f;
    public float avgMoneyCalcScathaRate = -1f;
    
    public boolean scappaModeActiveTemp = false;
    
    public byte aprilFoolsJokeRevealTickTimer = 0;
    
    
    private boolean fakeBanScreenPending = true;
    private boolean firstIngameTickPending = true;
    
    private long lastDeveloperCheckTime = -1;
    
    private int distanceToWallPrevious = -1;
    private long lastBedrockDetectionTime = -1;
    private boolean bedrockDetectedThisDirection = false;
    private Direction bedrockDirectionBefore = null;
    
    private final List<PetDrop> receivedPets = new ArrayList<>();
    private final HashMap<Integer, UUID> arrowOwners = new HashMap<>();
    
    private boolean wormSpawnCooldownRunningBefore = false;
    
    private int newRealDayCheckTickTimer = 0;
    
    
    public CoreManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    
    public void setSkyblockArea(@Nullable SkyblockArea area)
    {
        this.currentArea = area;
    }
    
    public boolean isInCrystalHollows()
    {
        return currentArea == SkyblockArea.CRYSTAL_HOLLOWS || scathaPro.config.dev.devModeEnabled.get();
    }
    
    public boolean isScappaModeActive()
    {
        return getProfileData().scappaModeUnlocked.get()
            && (scappaModeActiveTemp || scathaPro.config.unlockables.scappaModeEnabled.get());
    }
    
    public void resetForNewLobby()
    {
        firstLevelTickPending = true;
        firstCrystalHollowsTickPending = true;
        currentArea = null;
        previousScathaPets = null;
        lastWormSpawnTime = -1;
        wormSpawnCooldownStartTime = -1;
        lastHeat = -1;
        lastCrystalHollowsDay = -1;
        lastOldLobbyAlertTriggerDay = -1;
        crouchingBefore = false;
        tunnelVisionWastedForRecovery = false;
        antiSleepAlertTickTimer = 0;
        scathaPro.secondaryWormStatsManager.perLobbyStats.reset();
    }
    
    public void setRandomAntiSleepAlertTriggerMinutes()
    {
        int intervalMax = scathaPro.config.alerts.antiSleepAlertIntervalMax.get() * 20 * 60;
        int intervalMin = scathaPro.config.alerts.antiSleepAlertIntervalMin.get() * 20 * 60;
        nextAntiSleepAlertTriggerTickCount = intervalMin + (intervalMax > intervalMin ? Util.random.nextInt(intervalMax - intervalMin) : 0);
    }
    
    public void startWormSpawnCooldown(boolean forceRestart)
    {
        if (!forceRestart && wormSpawnCooldownStartTime >= Constants.pingTreshold) return;
        wormSpawnCooldownStartTime = TimeUtil.now();
    }
    
    public void addRegularWormKill()
    {
        int currentKills = getProfileData().regularWormKills.get();
        if (currentKills >= 0) getProfileData().regularWormKills.set(currentKills + 1);
        scathaPro.secondaryWormStatsManager.addRegularWormKill();
    }
    
    public void addScathaKill()
    {
        int currentKills = getProfileData().scathaKills.get();
        if (currentKills >= 0) getProfileData().scathaKills.set(currentKills + 1);
        scathaPro.secondaryWormStatsManager.addScathaKill();
    }
    
    public void updateScathaFarmingStreak(boolean increase)
    {
        PersistentData.ProfileData profileData = getProfileData();
        LocalDate today = TimeUtil.today();
        
        boolean streakUpdated = false;
        boolean highscoreUpdated = false;
        
        int streak = profileData.scathaFarmingStreak.get();
        if (increase && (streak == 0 || today.minusDays(1).equals(profileData.lastScathaFarmedDate.get())))
        {
            streak ++;
            profileData.scathaFarmingStreak.set(streak);
            streakUpdated = true;
            
            if (streak > profileData.scathaFarmingStreakHighscore.get())
            {
                profileData.scathaFarmingStreakHighscore.set(streak);
                highscoreUpdated = true;
            }
            
            if (scathaPro.config.miscellaneous.dailyStreakMessagesEnabled.get())
            {
                scathaPro.chatManager.sendChatMessage(Component.empty().setStyle(ChatManager.HIGHLIGHT_STYLE)
                    .append("First Scatha kill of the day! You reached a daily Scatha farming streak of ")
                    .append(Component.empty().withStyle(ChatFormatting.GREEN)
                        .append(streak + " day" + (streak != 1 ? "s" : ""))
                        .append(highscoreUpdated ? Component.literal(" (new highscore!)").withStyle(ChatFormatting.GOLD) : Component.empty())
                        .append(".")
                    ));
            }
        }
        else if (!profileData.lastScathaFarmedDate.hasValue() || !(
                    today.equals(profileData.lastScathaFarmedDate.get())
                    || !increase && today.minusDays(1).equals(profileData.lastScathaFarmedDate.get()) ))
        {
            int targetValue = increase ? 1 : 0;
            if (streak != targetValue)
            {
                streak = targetValue;
                profileData.scathaFarmingStreak.set(streak);
                streakUpdated = true;
                
                if (scathaPro.config.miscellaneous.dailyStreakMessagesEnabled.get() && profileData.lastScathaFarmedDate.hasValue())
                {
                    scathaPro.chatManager.sendCrystalHollowsMessage(Component.empty().withStyle(ChatFormatting.RED)
                        .append("You broke your daily Scatha farming streak!")
                        .append(increase ? Component.literal(" Restarting the streak from 1.").withStyle(ChatFormatting.YELLOW) : Component.empty()));
                }
            }
        }
        
        if (streakUpdated)
        {
            if (increase) profileData.lastScathaFarmedDate.set(today);
            scathaPro.persistentData.save();
            ScathaProEvents.scathaFarmingStreakChangedEvent.trigger(scathaPro,
                new ScathaProEvents.ScathaFarmingStreakChangedEventData(
                    profileData.scathaFarmingStreak.get(),
                    profileData.scathaFarmingStreakHighscore.get()
                )
            );
        }
    }
    
    
    public void tick()
    {
        PersistentData.ProfileData profileData = getProfileData();
        
        tickRealDayCheck(profileData);
        tickAprilFoolsReveal(profileData);
        
        final LocalPlayer player = scathaPro.minecraft.player;
        final Level level = player != null ? player.level() : null;
        if (level != null) tickLevel(player, level);
    }
    
    private void tickRealDayCheck(PersistentData.ProfileData profileData)
    {
        newRealDayCheckTickTimer--;
        if (newRealDayCheckTickTimer <= 0)
        {
            LocalDate lastPlayedDate = profileData.lastPlayedDate.get();
            if (lastPlayedDate == null || !lastPlayedDate.equals(TimeUtil.today()))
            {
                ScathaProEvents.realDayStartedEvent.trigger(scathaPro);
            }
            
            newRealDayCheckTickTimer = 20;
        }
    }
    
    private void tickAprilFoolsReveal(PersistentData.ProfileData profileData)
    {
        if (aprilFoolsJokeRevealTickTimer <= 0) return; // timer isn't running
        aprilFoolsJokeRevealTickTimer--;
        if (aprilFoolsJokeRevealTickTimer > 0) return; // timer isn't finished
        
        scathaPro.alertManager.scathaPetDropAlert.stopSound();
        
        scathaPro.minecraft.gui.setTimes(3, 60, 20);
        scathaPro.minecraft.gui.setSubtitle(Component.literal("It's that day of the year...").withStyle(ChatFormatting.GRAY));
        scathaPro.minecraft.gui.setTitle(TextUtil.getRainbowText("April Fools"));
        
        scathaPro.chatManager.sendChatMessage(Component.literal(
            "Hopefully the fake pet drop didn't disappoint you too much, sorry!\nKeep farming and you could get a real one very soon!"
        ).withStyle(ChatFormatting.GRAY));
        
        scathaPro.chatManager.sendChatDivider();
        scathaPro.chatManager.sendChatMessage(Component.empty()
            .append(Component.literal("Overlay icon googly eyes permanently unlocked!\n").withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(
                "You can from now on toggle them freely under " + scathaPro.getModDisplayName()
                    + " Settings > Miscellaneous, as well as disable this joke for future years!"
            ).withStyle(ChatFormatting.GRAY))
        );
        scathaPro.chatManager.sendChatDivider();
        
        profileData.lastAprilFoolsJokeShownYear.set((int) TimeUtil.getCurrentYear());
        profileData.overlayIconGooglyEyesUnlocked.set(true);
        scathaPro.persistentData.save();
        
        Achievement.april_fools.unlock();
    }
    
    private void tickLevel(LocalPlayer player, Level level)
    {
        long now = TimeUtil.now();
        boolean isInCrystalHollows = isInCrystalHollows();
        
        if (scathaPro.minecraft.screen == null)
        {
            tickNoScreenOpen();
        }
        
        if (isInCrystalHollows)
        {
            tickCrystalHollows(player, level, now);
        }
        
        tickAntiSleepAlert();
        tickTunnelVisionReadyAlert(now);
        
        tickDevCheck(now);
    }
    
    private void tickCrystalHollows(LocalPlayer player, Level level, long now)
    {
        boolean isFirstTick = false;
        if (firstCrystalHollowsTickPending && scathaPro.minecraft.screen == null)
        {
            firstCrystalHollowsTickPending = false;
            isFirstTick = true;
            scathaPro.chatManager.sendCachedCrystalHollowsMessages();
        }
        ScathaProEvents.crystalHollowsTickEvent.trigger(scathaPro,
            new ScathaProEvents.CrystalHollowsTickEventData(isFirstTick)
        );
        
        if (lastCrystalHollowsDay != -1)
        {
            int day = (int) Math.floor(level.getDayTime() / 24000f);
            if (day > lastCrystalHollowsDay)
            {
                ScathaPro.LOGGER.debug("Crystal hollows day increased from {} to {}", lastCrystalHollowsDay, day);
                if (lastCrystalHollowsDay >= 0)
                {
                    ScathaProEvents.crystalHollowsDayStartedEvent.trigger(scathaPro,
                        new ScathaProEvents.CrystalHollowsDayStartedEventData(day)
                    );
                }
                else if (scathaPro.config.alerts.oldLobbyAlertEnabled.get()
                    && OldLobbyAlertTriggerMode.ON_JOIN.isActive(scathaPro.config)
                    && day >= scathaPro.config.alerts.oldLobbyAlertTriggerDay.get())
                {
                    scathaPro.alertManager.oldLobbyAlert.play();
                    lastOldLobbyAlertTriggerDay = day;
                }
                lastCrystalHollowsDay = day;
            }
        }
        
        
        // Entity detection
        
        scathaPro.entityDetectionManager.tick(player);
        
        
        AABB projectileDetectionAABB = AABB.ofSize(player.position(), 100, 20, 100);
        
        // Worm arrow hits
        
        ArrayList<Integer> arrowIds = new ArrayList<>(arrowOwners.keySet());
        for (int i = 0; i < arrowOwners.size(); i ++)
        {
            int arrowID = arrowIds.get(i);
            if (level.getEntity(arrowID) == null) arrowOwners.remove(arrowID);
        }
        
        List<Arrow> arrows = level.getEntitiesOfClass(
            Arrow.class,
            projectileDetectionAABB,
            arrow -> !arrow.onGround()
        );
        
        for (Arrow arrow : arrows)
        {
            int id = arrow.getId();
            
            if (!arrowOwners.containsKey(id))
            {
                Player owner = level.getNearestPlayer(arrow, -1);
                if (owner != null) arrowOwners.put(id, owner.getUUID());
            }
            
            if (player.getUUID().equals(arrowOwners.get(id)))
            {
                List<ArmorStand> hitArmorStands = level.getEntitiesOfClass(ArmorStand.class, AABB.ofSize(arrow.position(), 6, 6, 6));
                for (ArmorStand armorStand : hitArmorStands)
                {
                    if (scathaPro.entityDetectionManager.getById(armorStand.getId()) instanceof DetectedWorm worm)
                    {
                        worm.attack(lastProjectileWeaponUsed, null);
                        ScathaPro.LOGGER.debug("Worm attacked with bow");
                    }
                }
            }
        }
        
        // Worm fishing hook hits
        
        List<FishingHook> fishingHooks = level.getEntitiesOfClass(FishingHook.class, projectileDetectionAABB,
            hook -> hook.getPlayerOwner() == player);
        for (FishingHook hook : fishingHooks)
        {
            List<ArmorStand> hookedArmorStands = level.getEntitiesOfClass(ArmorStand.class, AABB.ofSize(hook.position(), 6, 6, 6));
            for (ArmorStand armorStand : hookedArmorStands)
            {
                if (scathaPro.entityDetectionManager.getById(armorStand.getId()) instanceof DetectedWorm worm)
                {
                    worm.attack(lastProjectileWeaponUsed, null);
                    ScathaPro.LOGGER.debug("Worm attacked with fishing rod");
                }
            }
        }
        
        
        // Bedrock detection
        
        boolean bedrockDetected = false;
        
        BlockPos playerBlockPos = player.blockPosition();
        Direction playerDirection = player.getDirection();
        int distanceToWall = switch (playerDirection)
        {
            case NORTH -> playerBlockPos.getZ() - Constants.crystalHollowsBoundsMin;
            case EAST -> Constants.crystalHollowsBoundsMax - playerBlockPos.getX();
            case SOUTH -> Constants.crystalHollowsBoundsMax - playerBlockPos.getZ();
            case WEST -> playerBlockPos.getX() - Constants.crystalHollowsBoundsMin;
            default -> -1;
        };
        distanceToWall -= 1; // being next to the wall should be 0 distance
        
        if (bedrockDirectionBefore != null && bedrockDirectionBefore != playerDirection)
        {
            distanceToWallPrevious = -1;
            bedrockDetectedThisDirection = false;
        }
        bedrockDirectionBefore = playerDirection;
        
        int triggerDistance = scathaPro.config.alerts.bedrockWallAlertTriggerDistance.get();
        
        if (distanceToWallPrevious >= 0 && distanceToWallPrevious - distanceToWall == 1)
        {
            if (distanceToWall < triggerDistance) bedrockDetected = true;
        }
        if (distanceToWall >= triggerDistance) bedrockDetectedThisDirection = false;
        distanceToWallPrevious = distanceToWall;
        
        if (bedrockDetected && !bedrockDetectedThisDirection && (lastBedrockDetectionTime < 0 || now - lastBedrockDetectionTime > 1500))
        {
            bedrockDetectedThisDirection = true;
            lastBedrockDetectionTime = now;
            
            ScathaProEvents.bedrockWallDetectedEvent.trigger(scathaPro);
        }
        
        
        // Scatha pet drop detection
        
        HashMap<Rarity, Integer> currentScathaPets = new HashMap<>();
        NonNullList<ItemStack> nonEquipmentItems = player.getInventory().getNonEquipmentItems();
        for (int i = 0; i < nonEquipmentItems.size(); i++)
        {
            if (i == 8) continue; // No need to check the Skyblock menu
            
            ItemStack item = nonEquipmentItems.get(i);
            SkyblockItemUtil.getData(item, skyblockData -> {
                if (!Util.optionalValueEquals(skyblockData.getString(SkyblockItemUtil.KEY_ID), "PET"))
                    return;
                skyblockData.getString(SkyblockItemUtil.KEY_PETINFO).ifPresent(petInfo -> {
                    if (JsonUtil.parseJson(petInfo) instanceof JsonObject petInfoParsed)
                    {
                        String petType = JsonUtil.getString(petInfoParsed, "type");
                        if (petType == null || !petType.equals("SCATHA")) return;
                        
                        String petTier = JsonUtil.getString(petInfoParsed, "tier");
                        Rarity rarity = switch (petTier)
                        {
                            case "RARE" -> Rarity.RARE;
                            case "EPIC" -> Rarity.EPIC;
                            case "LEGENDARY" -> Rarity.LEGENDARY;
                            case null, default -> Rarity.UNKNOWN;
                        };
                        
                        currentScathaPets.compute(rarity, (key, currentRarityAmount)
                            -> (currentRarityAmount != null ? currentRarityAmount : 0) + item.getCount());
                    }
                });
            });
        }
        
        if (previousScathaPets != null)
        {
            Rarity newScathaPetRarity = null;
            for (Rarity rarity : currentScathaPets.keySet())
            {
                int currentRarityCount = currentScathaPets.get(rarity);
                Integer previousRarityCount = previousScathaPets.get(rarity);
                int difference = currentRarityCount - (previousRarityCount != null ? previousRarityCount : 0);
                if (difference > 0 && (newScathaPetRarity == null || rarity.ordinal() > newScathaPetRarity.ordinal()))
                {
                    newScathaPetRarity = rarity;
                }
            }
            if (newScathaPetRarity != null)
            {
                receivedPets.add(new PetDrop(newScathaPetRarity, now));
            }
        }
        
        previousScathaPets = currentScathaPets;
        
        for (int i = receivedPets.size() - 1; i >= 0; i --) {
            PetDrop pet = receivedPets.get(i);
            
            if (now - pet.dropTime >= Constants.pingTreshold)
            {
                receivedPets.remove(i);
                continue;
            }
            
            if (lastScathaKillTime >= 0 && now - lastScathaKillTime < Constants.pingTreshold)
            {
                receivedPets.remove(i);
                
                ScathaProEvents.scathaPetDropEvent.trigger(scathaPro,
                    new ScathaProEvents.ScathaPetDropEventData(pet)
                );
            }
        }
        
        
        // Worm spawn cooldown
        
        if (wormSpawnCooldownStartTime >= 0)
        {
            if (now - wormSpawnCooldownStartTime < Constants.wormSpawnCooldown)
            {
                wormSpawnCooldownRunningBefore = true;
            }
            else
            {
                if (wormSpawnCooldownRunningBefore) scathaPro.alertManager.wormSpawnCooldownEndAlert.play();
                wormSpawnCooldownRunningBefore = false;
                wormSpawnCooldownStartTime = -1;
            }
        }
        else wormSpawnCooldownRunningBefore = false;
    }
    
    private void tickNoScreenOpen()
    {
        if (fakeBanScreenPending)
        {
            fakeBanScreenPending = false;
            
            if (scathaPro.persistentDataProfileManager.isProfileDataCheated())
            {
                scathaPro.minecraft.setScreen(new FakeBanScreen(ScathaPro.MOD_NAME + " Savefile Manipulation", () -> {
                    scathaPro.minecraft.gui.setTimes(5, 60, 40);
                    scathaPro.minecraft.gui.setSubtitle(Component.empty());
                    scathaPro.minecraft.gui.setTitle(
                        Component.literal("We do a little trolling")
                        .withStyle(ChatFormatting.GREEN)
                    );
                    
                    Achievement.cheat.unlock();
                }));
                return;
            }
        }
        
        if (firstIngameTickPending)
        {
            firstIngameTickPending = false;
            ScathaProEvents.firstSessionIngameTickEvent.trigger(scathaPro);
        }
        
        if (firstLevelTickPending)
        {
            firstLevelTickPending = false;
            scathaPro.chatManager.sendCachedMessages();
            ScathaProEvents.firstLevelTickEvent.trigger(scathaPro);
        }
    }
    
    private void tickTunnelVisionReadyAlert(long now)
    {
        if (tunnelVisionReadyTime >= 0L && now >= tunnelVisionReadyTime)
        {
            // spawn cooldown still running?
            if (wormSpawnCooldownStartTime >= 0L && now - wormSpawnCooldownStartTime < Constants.wormSpawnCooldown)
            {
                // delay ability ready time until after cooldown runs out
                tunnelVisionReadyTime = wormSpawnCooldownStartTime + Constants.wormSpawnCooldown + 1000;
            }
            else
            {
                tunnelVisionReadyTime = -1L;
                if (isInCrystalHollows()) scathaPro.alertManager.tunnelVisionReadyAlert.play();
            }
        }
        if (tunnelVisionStartTime >= 0L && now - tunnelVisionStartTime >= Constants.tunnelVisionEffectDuration)
        {
            tunnelVisionStartTime = -1L;
        }
        if (tunnelVisionCooldownEndTime >= 0L && now >= tunnelVisionCooldownEndTime)
        {
            tunnelVisionCooldownEndTime = -1L;
        }
    }
    
    private void tickAntiSleepAlert()
    {
        if (scathaPro.config.alerts.antiSleepAlertEnabled.get())
        {
            antiSleepAlertTickTimer ++;
            if (antiSleepAlertTickTimer >= nextAntiSleepAlertTriggerTickCount)
            {
                if (nextAntiSleepAlertTriggerTickCount > 0)
                {
                    boolean isOtherAlertPlaying = false;
                    for (Alert alert : scathaPro.alertManager)
                    {
                        if (alert.isSoundPlaying()) // also checks itself but that's okay
                        {
                            isOtherAlertPlaying = true;
                            break;
                        }
                    }
                    if (!isOtherAlertPlaying) scathaPro.alertManager.antiSleepAlert.play();
                    antiSleepAlertTickTimer = 0;
                }
                
                setRandomAntiSleepAlertTriggerMinutes();
            }
        }
    }
    
    private void tickDevCheck(long now)
    {
        if (now - lastDeveloperCheckTime < 1000) return;
        
        if (scathaPro.getProfileData().unlockedAchievements.isUnlocked(Achievement.meet_developer)) return;
        
        ClientPacketListener connection = scathaPro.minecraft.getConnection();
        if (connection == null) return;
        if (connection.getPlayerInfo(Constants.devUUID) != null
            || (scathaPro.config.dev.devModeEnabled.get()
                && connection.getPlayerInfo("NamelessJu") != null)
        )
        {
            Achievement.meet_developer.unlock();
        }
        
        lastDeveloperCheckTime = now;
    }
    
    
    private PersistentData.ProfileData getProfileData()
    {
        return scathaPro.getProfileData();
    }
}
