package namelessju.scathapro.events;

import namelessju.scathapro.miscellaneous.PetDrop;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ScathaPetDropEvent extends Event
{
    public final PetDrop petDrop;
    
    public ScathaPetDropEvent(PetDrop petDrop)
    {
        this.petDrop = petDrop;
    }
}
