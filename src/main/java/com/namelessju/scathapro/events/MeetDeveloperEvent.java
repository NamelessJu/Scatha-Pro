package com.namelessju.scathapro.events;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.fml.common.eventhandler.Event;

public class MeetDeveloperEvent extends Event
{
    public final NetworkPlayerInfo developer;
    
    public MeetDeveloperEvent(NetworkPlayerInfo developer)
    {
        this.developer = developer;
    }
}
