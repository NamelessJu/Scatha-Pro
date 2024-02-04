package com.namelessju.scathapro.events;

import com.namelessju.scathapro.entitydetection.detectedentities.DetectedEntity;

import net.minecraftforge.fml.common.eventhandler.Event;

public class DetectedEntityRegisteredEvent extends Event
{
    public final DetectedEntity detectedEntity;
    
    public DetectedEntityRegisteredEvent(DetectedEntity detectedEntity)
    {
        this.detectedEntity = detectedEntity;
    }
}
