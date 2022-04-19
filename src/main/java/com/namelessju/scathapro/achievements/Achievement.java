package com.namelessju.scathapro.achievements;

public enum Achievement {
    
    // Scatha kills
    
    scathaFarmer("Scatha Farmer", "Become a scatha farmer by killing your first scatha", 1),
    firstSteps("First Steps", "Kill 5 scathas", 5),
    scathaHunter("Scatha Hunter", "Kill 100 scathas", 100),
    leaveNoScathaAlive("Leave no scatha alive", "Kill 500 scathas", 500),
    scathaPro("Scatha Pro", "Kill 1,000 scathas", 1000),
    
    // Worm kills
    
    poorWorms("Poor worms", "Kill 10 worms (regular or scathas)", 10),
    wormKiller("Worm Killer", "Kill 100 worms (regular or scathas)", 100),
    wormSlayer("Worm Slayer", "Kill 1,000 worms (regular or scathas)", 1000),
    wormAnnihilator("Worm Annihilator", "Kill 10,000 worms (regular or scathas)", 10000),
    
    // Back to back scathas
    
    anotherOne("Another one", "Get 2 scatha spawns back to back", 2),
    ohBabyATriple("Oh baby a triple!", "Get 3 scatha spawns back to back", 3),
    justOneMore("Just one more", "Get 4 scatha spawns back to back", 4),
    scathaMagnet("Scatha Magnet", "Get 5 scatha spawns back to back", 5),
    
    // Crystal Hollows time
    
    timeFlies("Time flies", "Spend 1 hour in a single Crystal Hollows lobby", 1),
    newHome("New Home", "Spend 3 hours in a single Crystal Hollows lobby", 3),
    touchGrass("Go touch some grass", "Spend 5 hours in a single Crystal Hollows lobby", 5),
    
    // Scatha Pets
    
    betterThanNothing("Better than nothing", "Get a rare scatha pet drop", 1),
    poggers("Poggers", "Get an epic scatha pet drop", 1),
    jackpot("Jackpot!", "Get a legendary scatha pet drop", 1);
    
    
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
