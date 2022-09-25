package com.namelessju.scathapro.achievements;

import net.minecraft.util.EnumChatFormatting;

public enum Achievement {
    
    /**********************
     *                    *
     *   SPOILER ALERT!   *
     *                    *
     **********************/
    
    achievements_unlocked_half("Halfway there...", "Unlock 50% of all non-hidden achievements", 50, Type.HIDDEN),
    achievements_unlocked_all("Completionist", "Unlock all non-hidden achievements", 100, Type.HIDDEN),
    
    worm_kills_1("Long boi", "Kill a worm", 1),
    worm_kills_2("Two digits", "Kill 10 worms", 10),
    worm_kills_3("Scatha farming byproduct", "Kill 100 worms", 100),
    worm_kills_4("There's a comma now", "Kill 1,000 worms", 1000),
    worm_kills_5("Free bestiary magic find", "Kill 10,000 worms", 10000),
    worm_kills_6("No life", "Kill 100,000 worms", 100000),
    
    scatha_kills_1("Scatha Farmer", "Become a Scatha farmer by killing your first Scatha", 1),
    scatha_kills_2("No pet yet?!", "Kill 10 Scathas", 10),
    scatha_kills_3("Continuing the grind...", "Kill 100 Scathas", 100),
    scatha_kills_4("k", "Kill 1,000 Scathas", 1000),
    scatha_kills_5("Over 9000", "Kill over 9,000 Scathas", 9001),
    scatha_kills_6("Scatha Pro", "Kill 20,000 Scathas", 20000),

    worm_kill_time_1("No time to waste", "Kill a worm less than one second after it spawned", 1, Type.SECRET),
    worm_kill_time_2("Yeah, I've got time!", "Kill a worm less than a second before it despawns", 29, Type.SECRET),
    
    worm_despawn("Bye, have a great time", "Let a worm despawn", 30, Type.SECRET),

    kill_weapons_regular_worm("Variety gamer", "Kill a regular worm using 5 different weapons", 5, Type.SECRET),
    kill_weapons_scatha("A fine collection", "Kill a Scatha using 10 different weapons", 10, Type.SECRET),
    
    hard_stone_mined_1("Rock solid", "Mine 1,000,000 hard stone", 1000000),
    hard_stone_mined_2("Stoned", "Mine 10,000,000 hard stone", 10000000),
    hard_stone_mined_3("Crystal hollowed", "Mine 100,000,000 hard stone", 100000000),
    
    crystal_hollows_time_1("Time flies", "Spend 1 hour in a single Crystal Hollows lobby", 1),
    crystal_hollows_time_2("New Home", "Spend 3 hours in a single Crystal Hollows lobby", 3),
    crystal_hollows_time_3("Go touch some grass", "Spend 5 hours in a single Crystal Hollows lobby", 5),

    scatha_spawn_time("Any%", "Spawn a Scatha in the 1st minute after joining a lobby", 1, Type.SECRET),
    
    lobby_kills_1("They keep coming", "Kill 25 worms in a single lobby", 25),
    lobby_kills_2("Scatha grinding session", "Kill 50 worms in a single lobby", 50),
    lobby_kills_3("Lobby emptied", "Kill 100 worms in a single lobby", 100),
    
    scatha_kill_sneak("Sneak 100", "Kill a Scatha while sneaking the whole time", 1, Type.SECRET),

    scatha_hit_dirt("Bully Maguire", "Put some dirt in a Scatha's eye", 1, Type.SECRET),
    scatha_kill_terminator("I'll be back!", "Kill a Scatha with a Terminator", 1, Type.HIDDEN),
    
    regular_worm_streak_1("Still perfectly normal", "Get 7 regular worm spawns in a row", 7),
    regular_worm_streak_2("Unlucky number", "Get 13 regular worm spawns in a row", 13),
    regular_worm_streak_3("RNGesus is on vacation", "Get 20 regular worm spawns in a row", 20),
    
    scatha_streak_1("This is getting out of hand", "Get 2 Scatha spawns back to back", 2),
    scatha_streak_2("Oh baby a triple!", "Get 3 Scatha spawns back to back", 3),
    scatha_streak_3("Four Scatha clover", "Get 4 Scatha spawns back to back", 4),
    scatha_streak_4("Scatha Magnet", "Get 5 Scatha spawns back to back", 5),

    scatha_spawn_chbottom("Hot Scatha farming place", "Spawn a Scatha at the bottom of the Crystal Hollows", 1),
    scatha_spawn_chtop("Reach for the sky", "Spawn a Scatha at the top of the Crystal Hollows", 1, Type.SECRET),
    
    scatha_pet_drop_1_rare("Better than nothing", "Get a rare Scatha pet drop", 1),
    scatha_pet_drop_1_epic("Mid", "Get an epic Scatha pet drop", 1),
    scatha_pet_drop_1_legendary("Jackpot!", "Get a legendary Scatha pet drop", 1),
    
    scatha_pet_drop_2_rare("Pocket money", "Get 3 rare Scatha pet drops", 3),
    scatha_pet_drop_2_epic("3pic", "Get 3 epic Scatha pet drops", 3),
    scatha_pet_drop_2_legendary("Golden trio", "Get 3 legendary Scatha pet drops", 3),
    
    scatha_pet_drop_3_rare("I'm blue da ba dee da ba di", "Get 10 rare Scatha pet drops", 10),
    scatha_pet_drop_3_epic("Epic Scatha gamer", "Get 10 epic Scatha pet drops", 10),
    scatha_pet_drop_3_legendary("Scatha billionaire", "Get 10 legendary Scatha pet drops", 10),

    scatha_pet_drop_each("Full scathadex", "Drop a Scatha pet of each rarity", 3),

    scatha_pet_drop_any_1("Scathavenger", "Drop 10 Scatha pets of any rarity", 10),
    scatha_pet_drop_any_2("Scathaddiction", "Drop 50 Scatha pets of any rarity", 50),

    scatha_pet_drop_mode_normal("Default", "Drop a Scatha pet in normal mode", 1),
    scatha_pet_drop_mode_meme("Stonks", "Drop a Scatha pet in meme mode", 1),
    scatha_pet_drop_mode_anime("Scatha-Chan", "Drop a Scatha pet in anime mode", 1),

    scatha_pet_drop_b2b("Sold your soul to RNGesus", "Drop two Scatha pets back to back", 2, Type.HIDDEN),
    
    meet_developer("The Creator", "Be in a lobby with Scatha-Pro's developer", 1, Type.SECRET),
    
    cheat("Cheater", "Obviously modify your Scatha-Pro savefile", 1, Type.HIDDEN);
    
    
    public enum Type {
        NORMAL(null),
        SECRET(EnumChatFormatting.AQUA + "Secret"),
        HIDDEN(EnumChatFormatting.RED.toString() + EnumChatFormatting.ITALIC + "HIDDEN"),
        LEGACY(EnumChatFormatting.DARK_PURPLE.toString() + EnumChatFormatting.BOLD + "LEGACY");
        
        public final String string;
        
        private Type(String string) {
            this.string = string;
        }
        
        @Override
        public String toString() {
            return string != null ? string : "";
        }
    }
    
    
    public final String name;
    public final String description;
    public final float goal;
    public final Type type;
    private float progress = 0f;
    
    Achievement(String name, String description, float goal, Type type) {
        this.name = name;
        this.description = description;
        this.goal = goal;
        this.type = type;
    }
    Achievement(String name, String description, float goal) {
        this(name, description, goal, Type.NORMAL);
    }
    
    public String getID() {
        return this.name();
    }

    public void setProgress(float progress) {
        if (progress < goal && AchievementManager.instance.isAchievementUnlocked(this)) this.progress = goal;
        else this.progress = progress;
        if (this.progress >= goal) AchievementManager.instance.unlockAchievement(this);
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
