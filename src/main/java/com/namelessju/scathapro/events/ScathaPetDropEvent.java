package com.namelessju.scathapro.events;

import com.namelessju.scathapro.objects.PetDrop;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ScathaPetDropEvent extends Event {
    
    public final PetDrop petDrop;
    
    public ScathaPetDropEvent(PetDrop petDrop) {
        this.petDrop = petDrop;
    }
    
}
