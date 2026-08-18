package namelessju.scathapro.miscellaneous.data.mixindata;

import namelessju.scathapro.miscellaneous.data.enums.WormSegmentType;
import org.jspecify.annotations.Nullable;

public interface IArmorStandRenderStateData
{
    void scathapro$setWormLifetimeLeft(float lifetimeLeft);
    float scathapro$getWormLifetimeLeft();

    void scathapro$setIsWorm(boolean isScatha);
    /** = either Stoneworm or Scatha */
    boolean scathapro$isWorm();
    boolean scathapro$isScatha();

    void scathapro$setWormSegmentType(@Nullable WormSegmentType segmentType);
    @Nullable WormSegmentType scathapro$getWormSegmentType();
}