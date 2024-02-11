package com.namelessju.scathapro.events;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.fml.common.eventhandler.Event;

public class MeetDeveloperEvent extends Event
{
    public final GameProfile gameProfile;
    
    public MeetDeveloperEvent(GameProfile gameProfile)
    {
        this.gameProfile = gameProfile;
    }
}
