package namelessju.scathapro.entitydetection.detectedentity;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.sounds.instances.ScathaProMovingEntitySound;
import namelessju.scathapro.util.SkyblockItemUtil;
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
    public final boolean isScatha;
    private long lastAttackTime = -1;
    private long lastFireAspectAttackTime = -1;
    private int lastFireAspectLevel = 0;
    
    private final Set<String> hitWeapons = new HashSet<>();
    private boolean wasHitWithPerfectGemstoneGauntlet = false;
    private ScathaProMovingEntitySound scappaSound = null;
    
    public boolean lootsharePossible = false;
    
    public DetectedWorm(ScathaPro scathaPro, ArmorStand entity, boolean isScatha)
    {
        super(scathaPro, entity);
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
            playScappaSound(scathaPro);
        }
        
        ScathaProEvents.wormSpawnEvent.trigger(scathaPro,
            new ScathaProEvents.WormEventData(this)
        );
    }
    
    private void playScappaSound(ScathaPro scathaPro)
    {
        if (this.scappaSound != null) return;
        this.scappaSound = scathaPro.soundManager.play(new ScathaProMovingEntitySound(
            scathaPro, ScathaPro.getIdentifier("scappa"),
            1f, 1f, entity, false
        ));
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
                // check for direct kill
                boolean countAsKilled = getLastAttackTime() >= 0 && TimeUtil.now() - getLastAttackTime() < Constants.pingTreshold;
                
                if (!countAsKilled) // check for kill by fire aspect
                {
                    countAsKilled = isFireAspectActive() && (getMaxLifetime() < 0 || getCurrentLifetime() < getMaxLifetime());
                }
                if (!countAsKilled && this.lootsharePossible) // check for lootshare
                {
                    AABB playerDetectionAABB = AABB.ofSize(this.entity.position(), 60, 60, 60);
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
                
                if (countAsKilled)
                {
                    if (scappaSound != null) scappaSound.stop();
                    ScathaProEvents.wormKillEvent.trigger(scathaPro, new ScathaProEvents.WormEventData(this));
                    ScathaPro.LOGGER.debug("Worm left world, counted as kill");
                }
                break;
                
            case LIFETIME_ENDED:
                if (scappaSound != null) scappaSound.stop();
                ScathaProEvents.wormDespawnEvent.trigger(scathaPro, new ScathaProEvents.WormEventData(this));
                ScathaPro.LOGGER.debug("Worm left world, counted as despawn");
                break;
                
            case null, default:
                break;
        }
    }
    
    public void attack(@Nullable ItemStack weapon, @Nullable ArmorStand attackedArmorStand)
    {
        long now = TimeUtil.now();
        lastAttackTime = now;
        
        if (weapon != null)
        {
            SkyblockItemUtil.getData(weapon, skyblockData -> {
                skyblockData.getString(SkyblockItemUtil.KEY_ID).ifPresent(skyblockItemID -> {
                    hitWeapons.add(skyblockItemID);
                    
                    if (!wasHitWithPerfectGemstoneGauntlet && skyblockItemID.equals("GEMSTONE_GAUNTLET"))
                    {
                        skyblockData.getCompound(SkyblockItemUtil.KEY_GEMS).ifPresent(gems -> {
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
                
                skyblockData.getCompound(SkyblockItemUtil.KEY_ENCHANTMENTS).ifPresent(enchantments -> {
                    lastFireAspectLevel = enchantments.getInt("fire_aspect").orElse(0);
                    if (lastFireAspectLevel > 0) lastFireAspectAttackTime = now;
                });
            });
        }
        
        if (!lootsharePossible && attackedArmorStand != null
            && Constants.isWormSkull(attackedArmorStand.getItemBySlot(EquipmentSlot.HEAD), true))
        {
            lootsharePossible = true;
        }
        
        ScathaProEvents.wormHitEvent.trigger(scathaPro, new ScathaProEvents.WormHitEventData(this, weapon));
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
            
            return TimeUtil.now() - lastFireAspectAttackTime <= fireAspectDuration * 1000f + Constants.pingTreshold;
        }
        return false;
    }
    
    public boolean wasHitWithPerfectGemstoneGauntlet()
    {
        return wasHitWithPerfectGemstoneGauntlet;
    }
}
