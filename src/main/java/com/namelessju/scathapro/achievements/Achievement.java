package com.namelessju.scathapro.achievements;

public enum Achievement {

    worm_kills_1("Long boi", "Kill a worm", 1),
    worm_kills_2("Two digits", "Kill 10 worms", 10),
    worm_kills_3("Continuing the grind...", "Kill 100 worms", 100),
    worm_kills_4("k", "Kill 1,000 worms", 1000),
    worm_kills_5("Over 9000", "Kill 10,000 worms", 10000),
    worm_kills_6("No life", "Kill 100,000 worms", 100000),
    
    scatha_kills_1("Scatha Farmer", "Become a scatha farmer by killing your first scatha", 1),
    scatha_kills_2("No pet yet?!", "Kill 10 scathas", 10),
    scatha_kills_3("MORE!!!", "Kill 100 scathas", 100),
    scatha_kills_4("Scatha Pro", "Kill 1,000 scathas", 1000),
    
    scatha_streak_1("This is getting out of hand", "Get 2 scatha spawns back to back", 2),
    scatha_streak_2("Oh baby a triple!", "Get 3 scatha spawns back to back", 3),
    scatha_streak_3("Four scatha clover", "Get 4 scatha spawns back to back", 4),
    scatha_streak_4("Scatha Magnet", "Get 5 scatha spawns back to back", 5),
    
    crystal_hollows_time_1("Time flies", "Spend 1 hour in a single Crystal Hollows lobby", 1),
    crystal_hollows_time_2("New Home", "Spend 3 hours in a single Crystal Hollows lobby", 3),
    crystal_hollows_time_3("Go touch some grass", "Spend 5 hours in a single Crystal Hollows lobby", 5),
    
    scatha_pet_drop_rare("Better than nothing", "Get a rare scatha pet drop", 1),
    scatha_pet_drop_epic("Mid", "Get an epic scatha pet drop", 1),
    scatha_pet_drop_legendary("Jackpot!", "Get a legendary scatha pet drop", 1);
    
    
    public final String name;
    public final String description;
    public final float goal;
    private float progress = 0f;
    
    Achievement(String name, String description, float goal) {
        this.name = name;
        this.description = description;
        this.goal = goal;
    }
    
    public String getID() {
        return this.name();
    }

    public void setProgress(float progress) {
        if (progress < goal && AchievementManager.getInstance().isAchievementUnlocked(this)) this.progress = goal;
        else this.progress = progress;
        if (this.progress >= goal) AchievementManager.getInstance().unlockAchievement(this);
    }
    public float getProgress() {
        return progress;
    }

    public static Achievement getByID(String id) {
        Achievement achievement = null;
        try {
            achievement = Achievement.valueOf(id);
        }
        catch (IllegalArgumentException e) {}
        return achievement;
    }
}
