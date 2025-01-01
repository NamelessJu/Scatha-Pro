package com.namelessju.scathapro.events;

import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;

public class WormKillEvent extends WormEvent
{
    public WormKillEvent(DetectedWorm worm)
    {
        super(worm);
    }
}
