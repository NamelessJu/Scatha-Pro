package com.namelessju.scathapro.events;

import com.namelessju.scathapro.miscellaneous.SkyblockArea;

import net.minecraftforge.fml.common.eventhandler.Event;

public class SkyblockAreaDetectedEvent extends Event
{
    public final SkyblockArea area;
    
    public SkyblockAreaDetectedEvent(SkyblockArea area)
    {
        this.area = area;
    }
}
