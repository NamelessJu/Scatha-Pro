package namelessju.scathapro.events;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import namelessju.scathapro.util.TimeUtil;

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
