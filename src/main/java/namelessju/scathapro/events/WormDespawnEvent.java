package namelessju.scathapro.events;

import namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;

public class WormDespawnEvent extends WormEvent
{
    public WormDespawnEvent(DetectedWorm worm)
    {
        super(worm);
    }
}
