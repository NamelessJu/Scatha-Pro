package namelessju.scathapro.managers.detectors;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.detectors.entities.detected.DetectedWorm;
import namelessju.scathapro.util.SkyBlockItemUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ProjectileWormHitDetector
{
    private final ScathaPro scathaPro;

    private final HashMap<Integer, UUID> arrowOwners = new HashMap<>();
    private ItemStack lastProjectileWeaponUsed = null;

    public ProjectileWormHitDetector(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }

    public void detect(LocalPlayer player)
    {
        Level level = player.level();
        AABB projectileDetectionAABB = AABB.ofSize(player.position(), 100, 20, 100);

        // Arrows

        ArrayList<Integer> arrowIds = new ArrayList<>(arrowOwners.keySet());
        for (int i = 0; i < arrowOwners.size(); i ++)
        {
            int arrowID = arrowIds.get(i);
            if (level.getEntity(arrowID) == null) arrowOwners.remove(arrowID);
        }

        for (Arrow arrow : level.getEntitiesOfClass(
            Arrow.class, projectileDetectionAABB, arrow -> !arrow.onGround()
        ))
        {
            int id = arrow.getId();

            if (!arrowOwners.containsKey(id))
            {
                Player owner = level.getNearestPlayer(arrow, -1);
                if (owner != null) arrowOwners.put(id, owner.getUUID());
            }

            if (player.getUUID().equals(arrowOwners.get(id)))
            {
                List<ArmorStand> hitArmorStands = level.getEntitiesOfClass(ArmorStand.class, getProjectileHitCheckAABB(arrow.position()));
                for (ArmorStand armorStand : hitArmorStands)
                {
                    if (scathaPro.coreManager.entityDetectionManager.getDetectedEntityById(armorStand.getId()) instanceof DetectedWorm worm)
                    {
                        worm.attack(lastProjectileWeaponUsed, null);
                        ScathaPro.LOGGER.debug("Worm attacked with arrow");
                    }
                }
            }
        }

        // Fishing hooks

        for (FishingHook hook : level.getEntitiesOfClass(
            FishingHook.class, projectileDetectionAABB, hook -> hook.getPlayerOwner() == player
        ))
        {
            List<ArmorStand> hookedArmorStands = level.getEntitiesOfClass(ArmorStand.class, getProjectileHitCheckAABB(hook.position()));
            for (ArmorStand armorStand : hookedArmorStands)
            {
                if (scathaPro.coreManager.entityDetectionManager.getDetectedEntityById(armorStand.getId()) instanceof DetectedWorm worm)
                {
                    worm.attack(lastProjectileWeaponUsed, null);
                    ScathaPro.LOGGER.debug("Worm attacked with fishing hook");
                }
            }
        }
    }

    public void checkItemUsed(ItemStack itemStack)
    {
        if (!scathaPro.coreManager.isInCrystalHollows()) return;

        Item item = itemStack.getItem();
        if (item == Items.FISHING_ROD || item == Items.BOW)
        {
            lastProjectileWeaponUsed = itemStack;
            ScathaPro.LOGGER.debug("Projectile weapon {} used", item);

            hitNearbyWorms(itemStack);
        }
    }

    public void checkShortbowHitFired(ItemStack itemStack)
    {
        if (!scathaPro.coreManager.isInCrystalHollows()) return;
        if (itemStack.getItem() != Items.BOW) return;

        String skyBlockId = SkyBlockItemUtil.getItemID(itemStack);
        if (skyBlockId == null || (!skyBlockId.equals(Constants.ItemID.terminator) && !Constants.ItemID.isShortbow(skyBlockId))) return;

        lastProjectileWeaponUsed = itemStack;
        ScathaPro.LOGGER.debug("Short bow hit fired");
    }

    public void drawnBowReleased(ItemStack itemStack)
    {
        if (!scathaPro.coreManager.isInCrystalHollows()) return;
        lastProjectileWeaponUsed = itemStack;
        ScathaPro.LOGGER.debug("Bow released");
        hitNearbyWorms(itemStack);
    }

    private void hitNearbyWorms(ItemStack itemStack)
    {
        LocalPlayer player = scathaPro.minecraft.player;
        if (player == null) return;

        for (ArmorStand armorStand : player.level().getEntitiesOfClass(ArmorStand.class, getProjectileHitCheckAABB(player.position())))
        {
            if (scathaPro.coreManager.entityDetectionManager.getDetectedEntityById(armorStand.getId()) instanceof DetectedWorm worm)
            {
                worm.attack(lastProjectileWeaponUsed, null);
                ScathaPro.LOGGER.debug("Worm attacked with projectile weapon {} (used close to worm)", itemStack.getItem());
            }
        }
    }

    public void reset()
    {
        arrowOwners.clear();
        lastProjectileWeaponUsed = null;
    }

    private static AABB getProjectileHitCheckAABB(Vec3 position)
    {
        return AABB.ofSize(position, 8, 6, 8);
    }
}