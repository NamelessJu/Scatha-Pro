package namelessju.scathapro.miscellaneous.data.mixindata;

import namelessju.scathapro.managers.detectors.entities.detected.DetectedWorm;
import namelessju.scathapro.miscellaneous.data.enums.WormSegmentType;
import org.jspecify.annotations.Nullable;

public interface IWormArmorStandData
{
    void scathapro$setWorm(DetectedWorm worm);
    @Nullable DetectedWorm scathapro$getWorm();

    void scathapro$setWormSegmentType(WormSegmentType segmentType);
    @Nullable WormSegmentType scathapro$getWormSegmentType();

    void scathapro$setIsWormNametag(boolean isWormNametag);
    boolean scathapro$isWormNametag();
}