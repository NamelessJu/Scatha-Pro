package com.namelessju.scathapro.events;

import com.namelessju.scathapro.objects.Goblin;

import net.minecraftforge.fml.common.eventhandler.Event;

public class GoblinSpawnEvent extends Event {

    public final Goblin goblin;
    
    public GoblinSpawnEvent(Goblin goblin) {
        this.goblin = goblin;
    }
}
