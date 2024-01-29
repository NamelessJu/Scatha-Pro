package com.namelessju.scathapro.events;

import net.minecraftforge.fml.common.eventhandler.Event;

public class ModUpdateEvent extends Event
{
    public final String previousVersion;
    public final String newVersion;
    
    public ModUpdateEvent(String previousVersion, String newVersion)
    {
        this.previousVersion = previousVersion;
        this.newVersion = newVersion;
    }
}
