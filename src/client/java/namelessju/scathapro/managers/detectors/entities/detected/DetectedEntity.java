package namelessju.scathapro.managers.detectors.entities.detected;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class DetectedEntity
{
    protected final @NonNull ScathaPro scathaPro;
    protected boolean canBeBlackHoled = false;
    private @Nullable Vec2 blackHolePosition = null;

    public final long spawnTime;
    public @NonNull ArmorStand entity;

    public DetectedEntity(@NonNull ScathaPro scathaPro, @NonNull ArmorStand entity)
    {
        this.scathaPro = scathaPro;
        this.entity = entity;
        this.spawnTime = TimeUtil.getEpochMilliseconds();
    }

    public abstract long getMaxLifetime();

    public void onRegistration() {}
    public void tick() {}
    public void onChangedEntity() {}
    public void onLeaveWorld(@Nullable LeaveWorldReason leaveWorldReason, @NonNull LocalPlayer player) {}

    public long getCurrentLifetime()
    {
        return TimeUtil.getEpochMilliseconds() - spawnTime;
    }

    public boolean canBeBlackHoled()
    {
        return canBeBlackHoled;
    }
    public void setBlackHolePosition(@NonNull Vec3 blackHolePosition)
    {
        this.blackHolePosition = Util.getHorizontal(blackHolePosition);
    }
    public @Nullable Vec2 getBlackHolePosition()
    {
        return blackHolePosition;
    }

    public enum LeaveWorldReason
    {
        LIFETIME_ENDED, KILLED, BLACK_HOLE, LEFT_SIMULATION_DISTANCE
    }
}