package namelessju.scathapro.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class CrystalHollowsDayStartedEvent extends Event
{
    public final int day;
    
    public CrystalHollowsDayStartedEvent(int day)
    {
        this.day = day;
    }
}
