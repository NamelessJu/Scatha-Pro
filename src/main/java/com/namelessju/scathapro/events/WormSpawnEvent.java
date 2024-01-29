package com.namelessju.scathapro.events;

import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;

public class WormSpawnEvent extends WormEvent
{
    public WormSpawnEvent(DetectedWorm worm)
    {
        super(worm);
    }
}
