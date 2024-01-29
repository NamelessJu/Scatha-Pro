package com.namelessju.scathapro.events;

import com.namelessju.scathapro.achievements.Achievement;

import net.minecraftforge.fml.common.eventhandler.Event;

public class AchievementUnlockedEvent extends Event
{
    public final Achievement achievement;
    
    public AchievementUnlockedEvent(Achievement achievement)
    {
        this.achievement = achievement;
    }
}
