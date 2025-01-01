package com.namelessju.scathapro.events;

import com.namelessju.scathapro.achievements.UnlockedAchievement;

import net.minecraftforge.fml.common.eventhandler.Event;

public class AchievementUnlockedEvent extends Event
{
    public final UnlockedAchievement unlockedAchievement;
    
    public AchievementUnlockedEvent(UnlockedAchievement unlockedAchievement)
    {
        this.unlockedAchievement = unlockedAchievement;
    }
}
