package com.namelessju.scathapro.events;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import com.namelessju.scathapro.util.TimeUtil;

public class WormSpawnEvent extends WormEvent
{
    public final long timeSincePreviousSpawn;
    
    public WormSpawnEvent(DetectedWorm worm)
    {
        super(worm);
        
        if (ScathaPro.getInstance().variables.lastWormSpawnTime >= 0L)
        {
            timeSincePreviousSpawn = (TimeUtil.now() - ScathaPro.getInstance().variables.lastWormSpawnTime);
        }
        else timeSincePreviousSpawn = -1L;
    }
}
