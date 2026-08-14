package namelessju.scathapro.managers.detectors.entities.detected;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.miscellaneous.data.enums.WormSegmentType;
import namelessju.scathapro.miscellaneous.data.mixindata.IWormArmorStandData;
import namelessju.scathapro.sounds.instances.ScathaProMovingEntitySound;
import namelessju.scathapro.util.SkyBlockItemUtil;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class DetectedWorm extends DetectedEntity
{
    private static final int[] referencePassingTickCounts = new int[] {0, 20, 60, 120};


    public final boolean isScatha;

    public boolean lootsharePossible = false;

    private int ticks = 0;
    private final Set<String> hitWeapons = new HashSet<>();
    private long lastAttackTime = -1;
    private long lastFireAspectAttackTime = -1;
    private int lastFireAspectLevel = 0;
    private boolean wasHitWithPerfectGemstoneGauntlet = false;
    private ScathaProMovingEntitySound scappaSound = null;

    public DetectedWorm(ScathaPro scathaPro, ArmorStand entity, boolean isScatha)
    {
        super(scathaPro, entity);
        this.canBeBlackHoled = true;
        this.isScatha = isScatha;
    }

    @Override
    public long getMaxLifetime()
    {
        return Constants.wormLifetime;
    }

    @Override
    public void onRegistration()
    {
        if (isScatha && scathaPro.coreManager.isScappaModeActive())
        {
            playScappaSound();
        }

        if (entity instanceof IWormArmorStandData data)
        {
            data.scathapro$setWorm(this);
            data.scathapro$setIsWormNametag(true);
        }

        ScathaProEvents.wormSpawnEvent.trigger(new ScathaProEvents.WormEventData(scathaPro, this));
    }

    @Override
    public void tick()
    {
        for (int tickCount : referencePassingTickCounts)
        {
            if (tickCount == ticks) passReferenceToNearbySegments();
        }

        ticks ++;
    }

    @Override
    public void onChangedEntity()
    {
        if (this.scappaSound != null) this.scappaSound.entity = this.entity;
    }

    @Override
    public void onLeaveWorld(LeaveWorldReason leaveWorldReason, @NonNull LocalPlayer player)
    {
        switch (leaveWorldReason)
        {
            case KILLED:
                if (scappaSound != null) scappaSound.stop();
                ScathaPro.LOGGER.debug("Worm left world near player");
                WormKillHandler.handleKill(this, player, false);
                break;

            case BLACK_HOLE:
                if (scappaSound != null) scappaSound.stop();
                ScathaPro.LOGGER.debug("Worm left world near black hole");
                scathaPro.coreManager.lastScathaHitHadShuriken = false;
                scathaPro.coreManager.startBlackHoleKill(new WormKillHandler(this, player, true));
                break;

            case LIFETIME_ENDED:
                if (scappaSound != null) scappaSound.stop();
                ScathaProEvents.wormDespawnEvent.trigger(new ScathaProEvents.WormEventData(scathaPro, this));
                ScathaPro.LOGGER.debug("Worm left world, counted as despawn");
                break;

            case null, default:
                break;
        }
    }

    private void passReferenceToNearbySegments()
    {
        int referencesPassed = 0;
        for (ArmorStand armorStand : entity.level().getEntitiesOfClass(ArmorStand.class,
            AABB.ofSize(entity.position(), 16, 4, 16),
            armorStand -> armorStand != entity
        )) {
            IWormArmorStandData segmentData = ((IWormArmorStandData) armorStand);
            if (segmentData.scathapro$getWorm() != null) continue;

            WormSegmentType segmentType = Constants.getPlayerHeadWormSegmentType(armorStand.getItemBySlot(EquipmentSlot.HEAD));
            if (segmentType != null)
            {
                segmentData.scathapro$setWorm(this);
                segmentData.scathapro$setWormSegmentType(segmentType);
                referencesPassed ++;
            }
        }
        ScathaPro.LOGGER.debug("DetectedWorm reference (is Scatha: {}) passed to {} nearby segment armor stands", isScatha, referencesPassed);
    }

    private void playScappaSound()
    {
        if (this.scappaSound != null) return;
        this.scappaSound = scathaPro.soundManager.play(new ScathaProMovingEntitySound(
            scathaPro, ScathaPro.getIdentifier("scappa"),
            1f, 1f, entity, false
        ));
    }

    public void attack(@Nullable ItemStack weapon, @Nullable ArmorStand attackedArmorStand)
    {
        long now = TimeUtil.getEpochMilliseconds();
        lastAttackTime = now;

        if (weapon != null)
        {
            SkyBlockItemUtil.getData(weapon, skyBlockData -> {
                skyBlockData.getString(SkyBlockItemUtil.KEY_ID).ifPresent(skyBlockItemID -> {
                    hitWeapons.add(skyBlockItemID);

                    if (!wasHitWithPerfectGemstoneGauntlet && skyBlockItemID.equals(Constants.ItemID.gemstoneGauntlet))
                    {
                        skyBlockData.getCompound(SkyBlockItemUtil.KEY_GEMS).ifPresent(gems -> {
                            boolean perfect = true;
                            for (String gemSlotId : new String[]{"JADE_0", "AMBER_0", "SAPPHIRE_0", "AMETHYST_0", "TOPAZ_0"})
                            {
                                if (!"PERFECT".equals(gems.getString(gemSlotId).orElse(null)))
                                {
                                    perfect = false;
                                    break;
                                }
                            }
                            if (perfect) wasHitWithPerfectGemstoneGauntlet = true;
                        });
                    }
                });

                skyBlockData.getCompound(SkyBlockItemUtil.KEY_ENCHANTMENTS).ifPresent(enchantments -> {
                    lastFireAspectLevel = enchantments.getInt("fire_aspect").orElse(0);
                    if (lastFireAspectLevel > 0) lastFireAspectAttackTime = now;
                });
            });
        }

        if (!lootsharePossible && attackedArmorStand != null
            && Constants.getPlayerHeadWormSegmentType(
                attackedArmorStand.getItemBySlot(EquipmentSlot.HEAD), true
            ) == WormSegmentType.HEAD
        )
        {
            lootsharePossible = true;
        }

        ScathaProEvents.wormHitEvent.trigger(new ScathaProEvents.WormHitEventData(scathaPro, this, weapon));
    }

    public long getLastAttackTime()
    {
        return lastAttackTime;
    }

    public String[] getHitWeapons()
    {
        return hitWeapons.toArray(new String[] {});
    }

    public int getHitWeaponsCount()
    {
        return hitWeapons.size();
    }

    public boolean isFireAspectActive()
    {
        if (lastFireAspectLevel > 0)
        {
            float fireAspectDuration = switch (lastFireAspectLevel)
            {
                case 1 -> 3f;
                case 2, 3 -> 4f;
                default -> 0f;
            };

            return TimeUtil.getEpochMilliseconds() - lastFireAspectAttackTime <= fireAspectDuration * 1000f + Constants.pingThreshold;
        }
        return false;
    }

    public boolean wasHitWithPerfectGemstoneGauntlet()
    {
        return wasHitWithPerfectGemstoneGauntlet;
    }

    public static final class WormKillHandler
    {
        private final @NonNull DetectedWorm worm;
        private final @NonNull LocalPlayer player;
        private final boolean wasBlackHoled;

        private WormKillHandler(@NonNull DetectedWorm worm, @NonNull LocalPlayer player, boolean wasBlackHoled)
        {
            this.worm = worm;
            this.player = player;
            this.wasBlackHoled = wasBlackHoled;
        }

        public void handleKill()
        {
            WormKillHandler.handleKill(worm, player, wasBlackHoled);
        }

        public static void handleKill(@NonNull DetectedWorm worm, @NonNull LocalPlayer player, boolean wasBlackHoled)
        {
            // check for direct kill
            boolean countAsKilled;

            if (wasBlackHoled) countAsKilled = true;
            else
            {
                countAsKilled = worm.getLastAttackTime() >= 0 && TimeUtil.getEpochMilliseconds() - worm.getLastAttackTime() < Constants.pingThreshold;

                if (!countAsKilled) // check for kill by fire aspect
                {
                    countAsKilled = worm.isFireAspectActive() && (worm.getMaxLifetime() < 0 || worm.getCurrentLifetime() < worm.getMaxLifetime());
                }
                if (!countAsKilled && worm.lootsharePossible) // check for lootshare
                {
                    AABB playerDetectionAABB = AABB.ofSize(worm.entity.position(), 60, 60, 60);
                    if (playerDetectionAABB.contains(player.position()))
                    {
                        int nearbyOtherPlayerCount = player.level().getEntities(player, playerDetectionAABB, entity -> entity instanceof Player).size();
                        if (nearbyOtherPlayerCount > 0)
                        {
                            ScathaPro.LOGGER.debug("Worm treated as lootshared");
                            countAsKilled = true;
                        }
                    }
                }
            }

            if (countAsKilled)
            {
                ScathaProEvents.wormKillEvent.trigger(new ScathaProEvents.WormKillEventData(worm.scathaPro, worm, wasBlackHoled));
                ScathaPro.LOGGER.debug("Worm killed (black hole: {})", wasBlackHoled);
            }
        }
    }
}