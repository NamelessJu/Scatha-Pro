package com.namelessju.scathapro.events;

import com.namelessju.scathapro.entitydetection.detectedentities.DetectedGoblin;

import net.minecraftforge.fml.common.eventhandler.Event;

public class GoblinSpawnEvent extends Event
{
    public final DetectedGoblin goblin;
    
    public GoblinSpawnEvent(DetectedGoblin goblin)
    {
        this.goblin = goblin;
    }
}
