package namelessju.scathapro.events;

import namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class WormEvent extends Event
{
    public final DetectedWorm worm;
    
    public WormEvent(DetectedWorm worm)
    {
        this.worm = worm;
    }
}
