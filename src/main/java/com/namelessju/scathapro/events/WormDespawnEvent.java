package com.namelessju.scathapro.events;

import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;

public class WormDespawnEvent extends WormEvent
{
    public WormDespawnEvent(DetectedWorm worm)
    {
        super(worm);
    }
}
