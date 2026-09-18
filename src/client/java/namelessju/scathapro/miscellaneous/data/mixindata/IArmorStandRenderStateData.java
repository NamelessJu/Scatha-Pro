package namelessju.scathapro.miscellaneous.data.mixindata;

import namelessju.scathapro.miscellaneous.data.enums.WormSegmentType;
import org.jspecify.annotations.Nullable;

public interface IArmorStandRenderStateData
{
    void scathaPro$setWormLifetimeLeft(float lifetimeLeft);
    float scathaPro$getWormLifetimeLeft();

    void scathaPro$setIsWorm(boolean isScatha);
    /** = either Stoneworm or Scatha */
    boolean scathaPro$isWorm();
    boolean scathaPro$isScatha();

    void scathaPro$setWormSegmentType(@Nullable WormSegmentType segmentType);
    @Nullable WormSegmentType scathaPro$getWormSegmentType();
}