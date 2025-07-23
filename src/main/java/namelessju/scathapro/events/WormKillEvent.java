package namelessju.scathapro.events;

import namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;

public class WormKillEvent extends WormEvent
{
    public WormKillEvent(DetectedWorm worm)
    {
        super(worm);
    }
}
