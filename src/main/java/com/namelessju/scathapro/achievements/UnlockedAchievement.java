package com.namelessju.scathapro.achievements;

public class UnlockedAchievement {
    public final Achievement achievement;
    public final long unlockedAtTimestamp;
    
    public UnlockedAchievement(Achievement achievement, long unlockedAtTimestamp) {
        this.achievement = achievement;
        this.unlockedAtTimestamp = unlockedAtTimestamp;
    }
}
