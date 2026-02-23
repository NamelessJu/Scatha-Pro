package namelessju.scathapro.entitydetection.detectedentity;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class DetectedEntity
{
    protected final @NonNull ScathaPro scathaPro;
    
    public final long spawnTime;
    public @NonNull ArmorStand entity;
    
    public DetectedEntity(@NonNull ScathaPro scathaPro, @NonNull ArmorStand entity)
    {
        this.scathaPro = scathaPro;
        this.entity = entity;
        this.spawnTime = TimeUtil.now();
    }
    
    public abstract long getMaxLifetime();
    
    public void onRegistration() {}
    public void onChangedEntity() {}
    public void onLeaveWorld(@Nullable LeaveWorldReason leaveWorldReason, @NonNull LocalPlayer player) {}
    
    
    public long getCurrentLifetime()
    {
        return TimeUtil.now() - spawnTime;
    }
    
    
    public enum LeaveWorldReason
    {
        LIFETIME_ENDED, KILLED, LEFT_SIMULATION_DISTANCE
    }
}
