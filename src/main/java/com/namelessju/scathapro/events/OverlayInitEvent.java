package com.namelessju.scathapro.events;

import com.namelessju.scathapro.overlay.OverlayContainer;

import net.minecraftforge.fml.common.eventhandler.Event;

public abstract class OverlayInitEvent extends Event {
    
    public final OverlayContainer overlay; 
    
    public OverlayInitEvent(OverlayContainer overlay) {
        this.overlay = overlay;
    }
    
    public static class Pre extends OverlayInitEvent {
        public Pre(OverlayContainer overlay) {
            super(overlay);
        }
    }
    
    public static class Post extends OverlayInitEvent {
        public Post(OverlayContainer overlay) {
            super(overlay);
        }
    }
}
