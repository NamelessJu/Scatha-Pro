package namelessju.scathapro.miscellaneous.data.mixindata;

import namelessju.scathapro.managers.detectors.entities.detected.DetectedWorm;
import namelessju.scathapro.miscellaneous.data.enums.WormSegmentType;
import org.jspecify.annotations.Nullable;

public interface IWormArmorStandData
{
    void scathaPro$setWorm(DetectedWorm worm);
    @Nullable DetectedWorm scathaPro$getWorm();

    void scathaPro$setWormSegmentType(WormSegmentType segmentType);
    @Nullable WormSegmentType scathaPro$getWormSegmentType();

    void scathaPro$setIsWormNametag(boolean isWormNametag);
    boolean scathaPro$isWormNametag();
}