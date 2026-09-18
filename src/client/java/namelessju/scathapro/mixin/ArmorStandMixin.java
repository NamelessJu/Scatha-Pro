package namelessju.scathapro.mixin;

import namelessju.scathapro.managers.detectors.entities.detected.DetectedWorm;
import namelessju.scathapro.miscellaneous.data.enums.WormSegmentType;
import namelessju.scathapro.miscellaneous.data.mixindata.IWormArmorStandData;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.ref.WeakReference;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin implements IWormArmorStandData
{
    @Unique
    private @Nullable WeakReference<DetectedWorm> worm;
    @Unique
    private @Nullable WormSegmentType wormSegmentType;
    @Unique
    private boolean isWormNametag = false;

    @Override
    public void scathaPro$setWorm(DetectedWorm worm)
    {
        this.worm = new WeakReference<>(worm);
    }

    @Override
    public @Nullable DetectedWorm scathaPro$getWorm()
    {
        return worm != null ? worm.get() : null;
    }

    @Override
    public void scathaPro$setWormSegmentType(WormSegmentType segmentType)
    {
        this.wormSegmentType = segmentType;
    }

    @Override
    public @Nullable WormSegmentType scathaPro$getWormSegmentType()
    {
        return wormSegmentType;
    }

    @Override
    public void scathaPro$setIsWormNametag(boolean isWormNametag)
    {
        this.isWormNametag = isWormNametag;
    }

    @Override
    public boolean scathaPro$isWormNametag()
    {
        return isWormNametag;
    }
}