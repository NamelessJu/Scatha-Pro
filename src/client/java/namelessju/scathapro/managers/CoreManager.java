package namelessju.scathapro.managers;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.gui.menus.screens.FakeBanScreen;
import namelessju.scathapro.managers.detectors.BedrockWallDetector;
import namelessju.scathapro.managers.detectors.ObstacleDetector;
import namelessju.scathapro.managers.detectors.ProjectileWormHitDetector;
import namelessju.scathapro.managers.detectors.ScathaDropsDetector;
import namelessju.scathapro.managers.detectors.entities.EntityDetectionManager;
import namelessju.scathapro.managers.detectors.entities.detected.DetectedWorm;
import namelessju.scathapro.miscellaneous.data.enums.OldLobbyAlertTriggerMode;
import namelessju.scathapro.miscellaneous.data.enums.SkyBlockArea;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

public class CoreManager
{
    private final ScathaPro scathaPro;

    public final EntityDetectionManager entityDetectionManager;
    public final ProjectileWormHitDetector projectileWormHitDetector;
    private final BedrockWallDetector bedrockWallDetector;
    private final ObstacleDetector obstacleDetector;
    private final ScathaDropsDetector scathaDropsDetector;

    private @Nullable SkyBlockArea currentArea = null;

    private boolean firstLevelTickPending = true;
    private boolean firstCrystalHollowsTickPending = true;

    public long lastWorldJoinTime = -1L;
    public long lastWormSpawnTime = -1L;
    public long wormSpawnCooldownStartTime = -1;
    public long lastScathaKillTime = -1;
    public boolean lastScathaHitHadShuriken = false;
    private DetectedWorm.@Nullable WormKillHandler blackHoleWormKillHandler = null;
    private short blackHoleWormKillTicksLeft = 0;
    public @Nullable Vec2 lastScathaBlackHolePosition = null;

    public boolean crouchingBefore = false;
    public long lastCrouchStartTime = -1;

    public long lastPreAlertTime = -1L;
    /** Used for the high heat alert and doesn't get updated if the alert is disabled! */
    public int lastHeat = -1;
    public int lastOldLobbyAlertTriggerDay = -1;
    public int antiSleepAlertTickTimer = 0;
    private int nextAntiSleepAlertTriggerTickCount = -1;

    /** -1 = waiting for first time update packet; -2 = packet received, may now update this variable to the actual day */
    private int lastCrystalHollowsDay = -1;

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

    private boolean wormSpawnCooldownRunningBefore = false;

    private int newRealDayCheckTickTimer = 0;


    public CoreManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;

        entityDetectionManager = new EntityDetectionManager(scathaPro);
        projectileWormHitDetector = new ProjectileWormHitDetector(scathaPro);
        bedrockWallDetector = new BedrockWallDetector(scathaPro);
        obstacleDetector = new ObstacleDetector(scathaPro);
        scathaDropsDetector = new ScathaDropsDetector(scathaPro);
    }


    public void setSkyBlockArea(@Nullable SkyBlockArea area)
    {
        this.currentArea = area;
    }

    public boolean isInCrystalHollows()
    {
        return currentArea == SkyBlockArea.CRYSTAL_HOLLOWS || scathaPro.config.dev.devModeEnabled.get();
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
        lastWormSpawnTime = -1;
        wormSpawnCooldownStartTime = -1;
        lastScathaHitHadShuriken = false;
        blackHoleWormKillHandler = null;
        blackHoleWormKillTicksLeft = 0;
        lastScathaBlackHolePosition = null;
        lastHeat = -1;
        lastCrystalHollowsDay = -1;
        lastOldLobbyAlertTriggerDay = -1;
        crouchingBefore = false;
        lastCrouchStartTime = -1;
        tunnelVisionWastedForRecovery = false;
        antiSleepAlertTickTimer = 0;
        entityDetectionManager.reset();
        projectileWormHitDetector.reset();
        bedrockWallDetector.reset();
        obstacleDetector.reset();
        scathaDropsDetector.reset();
    }

    public void setRandomAntiSleepAlertTriggerMinutes()
    {
        int intervalMax = scathaPro.config.alerts.antiSleepAlertIntervalMax.get() * 20 * 60;
        int intervalMin = scathaPro.config.alerts.antiSleepAlertIntervalMin.get() * 20 * 60;
        nextAntiSleepAlertTriggerTickCount = intervalMin + (intervalMax > intervalMin ? Util.random.nextInt(intervalMax - intervalMin) : 0);
    }

    public void startWormSpawnCooldown(boolean forceRestart)
    {
        if (!forceRestart && wormSpawnCooldownStartTime >= Constants.pingThreshold) return;
        wormSpawnCooldownStartTime = TimeUtil.getEpochMilliseconds();
    }

    public void addRegularWormKill()
    {
        int currentKills = getProfileData().regularWormKills.get();
        if (currentKills >= 0) getProfileData().regularWormKills.set(currentKills + 1);
        scathaPro.secondaryStatsManager.addRegularWormKill();
    }

    public void addScathaKill()
    {
        int currentKills = getProfileData().scathaKills.get();
        if (currentKills >= 0) getProfileData().scathaKills.set(currentKills + 1);
        scathaPro.secondaryStatsManager.addScathaKill();
    }

    public void updateScathaFarmingStreak(boolean increase)
    {
        PersistentData.ProfileData profileData = getProfileData();
        LocalDate today = TimeUtil.today();

        boolean streakUpdated = false;
        boolean highScoreUpdated = false;

        int streak = profileData.scathaFarmingStreak.get();
        if (increase && (streak == 0 || today.minusDays(1).equals(profileData.lastScathaFarmedDate.get())))
        {
            streak ++;
            profileData.scathaFarmingStreak.set(streak);
            streakUpdated = true;

            if (streak > profileData.scathaFarmingStreakHighScore.get())
            {
                profileData.scathaFarmingStreakHighScore.set(streak);
                highScoreUpdated = true;
            }

            if (scathaPro.config.miscellaneous.dailyStreakMessagesEnabled.get())
            {
                scathaPro.chatManager.sendChatMessage(Component.empty().setStyle(ChatManager.HIGHLIGHT_STYLE)
                    .append("First Scatha kill of the day! You reached a daily Scatha farming streak of ")
                    .append(Component.empty().withStyle(ChatFormatting.GREEN)
                        .append(streak + " day" + (streak != 1 ? "s" : ""))
                        .append(highScoreUpdated ? Component.literal(" (new highscore!)").withStyle(ChatFormatting.GOLD) : Component.empty())
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
                        .append(
                            increase
                                ? Component.literal(" Restarting the streak from 1.").withStyle(ChatFormatting.YELLOW)
                                : Component.empty()
                        ));
                }
            }
        }

        if (streakUpdated)
        {
            if (increase) profileData.lastScathaFarmedDate.set(today);
            scathaPro.persistentData.save();
            ScathaProEvents.scathaFarmingStreakChangedEvent.trigger(
                new ScathaProEvents.ScathaFarmingStreakChangedEventData(
                    scathaPro, profileData.scathaFarmingStreak.get(), profileData.scathaFarmingStreakHighScore.get()
                )
            );
        }
    }

    public void startBlackHoleKill(DetectedWorm.WormKillHandler killHandler)
    {
        if (scathaPro.config.miscellaneous.dropsSlotMachineEnabled.get())
            scathaPro.scathaDropsSlotMachineManager.startPreRoll(Constants.blackHoleSuctionMaxTicksDuration, Constants.blackHoleSuctionMinTicksDuration);

        this.blackHoleWormKillHandler = killHandler;
        this.blackHoleWormKillTicksLeft = Constants.blackHoleSuctionMaxTicksDuration;
    }

    public boolean hasPendingBlackHoleKill()
    {
        return blackHoleWormKillTicksLeft > 0;
    }

    public void triggerBlackHoleKill()
    {
        blackHoleWormKillTicksLeft = 0;
        if (blackHoleWormKillHandler != null)
        {
            blackHoleWormKillHandler.handleKill();
            blackHoleWormKillHandler = null;
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

        if (blackHoleWormKillTicksLeft > 0)
        {
            blackHoleWormKillTicksLeft --;
            if (blackHoleWormKillTicksLeft <= 0) triggerBlackHoleKill();
        }
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

        scathaPro.alertManager.scathaPetDropAlert.stopSound(scathaPro.soundManager);
        scathaPro.itemPopupRenderer.clear();

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
        long now = TimeUtil.getEpochMilliseconds();
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
        ScathaProEvents.crystalHollowsTickEvent.trigger(
            new ScathaProEvents.CrystalHollowsTickEventData(scathaPro, isFirstTick)
        );

        if (lastCrystalHollowsDay != -1)
        {
            int day = (int) Math.floor(level.getDefaultClockTime() / 24000f);
            if (day > lastCrystalHollowsDay)
            {
                ScathaPro.LOGGER.debug("Crystal hollows day increased from {} to {}", lastCrystalHollowsDay, day);
                if (lastCrystalHollowsDay >= 0)
                {
                    ScathaProEvents.crystalHollowsDayStartedEvent.trigger(
                        new ScathaProEvents.CrystalHollowsDayStartedEventData(scathaPro, day)
                    );
                }
                else if (scathaPro.config.alerts.oldLobbyAlertEnabled.get()
                    && OldLobbyAlertTriggerMode.ON_JOIN.isActive(scathaPro.config)
                    && day >= scathaPro.config.alerts.oldLobbyAlertTriggerDay.get())
                {
                    scathaPro.alertManager.oldLobbyAlert.play(scathaPro);
                    lastOldLobbyAlertTriggerDay = day;
                }
                lastCrystalHollowsDay = day;
            }
        }


        // Detectors

        entityDetectionManager.tick(player);
        projectileWormHitDetector.detect(player);
        bedrockWallDetector.detect(player, now);
        obstacleDetector.detect(player);
        scathaDropsDetector.detect(player, now);


        // Worm spawn cooldown

        if (wormSpawnCooldownStartTime >= 0)
        {
            if (now - wormSpawnCooldownStartTime < Constants.wormSpawnCooldown)
            {
                wormSpawnCooldownRunningBefore = true;
            }
            else
            {
                if (wormSpawnCooldownRunningBefore)
                {
                    scathaPro.alertManager.wormSpawnCooldownEndAlert.play(scathaPro);
                }
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
                        Component.literal("We do a little trolling").withStyle(ChatFormatting.GREEN)
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
                tunnelVisionReadyTime = wormSpawnCooldownStartTime + Constants.wormSpawnCooldown + 2000;
            }
            else
            {
                tunnelVisionReadyTime = -1L;
                if (isInCrystalHollows())
                {
                    scathaPro.alertManager.tunnelVisionReadyAlert.play(scathaPro);
                }
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
                        // also checks itself but that's okay
                        if (alert.isSoundPlaying(scathaPro.soundManager))
                        {
                            isOtherAlertPlaying = true;
                            break;
                        }
                    }
                    if (!isOtherAlertPlaying)
                    {
                        scathaPro.alertManager.antiSleepAlert.play(scathaPro);
                    }
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