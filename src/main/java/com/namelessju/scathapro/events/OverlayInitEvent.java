package com.namelessju.scathapro.events;

import com.namelessju.scathapro.overlay.elements.OverlayContainer;

import net.minecraftforge.fml.common.eventhandler.Event;

public class OverlayInitEvent extends Event
{
    public final OverlayContainer overlay; 
    
    public OverlayInitEvent(OverlayContainer overlay)
    {
        this.overlay = overlay;
    }
}
