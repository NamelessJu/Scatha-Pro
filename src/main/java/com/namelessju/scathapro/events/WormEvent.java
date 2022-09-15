package com.namelessju.scathapro.events;

import com.namelessju.scathapro.objects.Worm;

import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class WormEvent extends Event {
    
    public final Worm worm;
    
    public WormEvent(Worm worm) {
        this.worm = worm;
    }
    
}
