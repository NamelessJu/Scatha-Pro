package com.namelessju.scathapro.achievements;

import com.namelessju.scathapro.ScathaPro;

import net.minecraft.util.EnumChatFormatting;

public enum Achievement
{
    /**********************
     *                    *
     *   SPOILER ALERT!   *
     *                    *
     **********************/
    
    achievements_unlocked_half("Whoa, we're halfway there...", "Unlock 50% of all non-hidden achievements", 50, Type.HIDDEN),
    achievements_unlocked_all("Completionist", "Unlock all non-hidden achievements", 100, Type.HIDDEN),
    
    worm_kills_1("Long boi", "Kill a worm", 1),
    worm_kills_2("Two digits", "Kill 10 worms", 10),
    worm_kills_3("Bestiary magic find", "Kill 120 worms", 120),
    worm_bestiary_max("Worm expert", "Complete the worm bestiary (400 worm kills)", 400),
    worm_kills_4("k", "Kill 1,000 worms", 1000),
    worm_kills_5("Worm annihilator", "Kill 10,000 worms", 10000),
    worm_kills_6("Don't stop me now", "Kill 25,000 worms", 25000, Type.BONUS),
    
    scatha_kills_1("Scatha Farmer", "Become a Scatha farmer by killing your first Scatha", 1),
    scatha_kills_2("No pet yet?!", "Kill 10 Scathas", 10),
    scatha_kills_3("Continuing the grind...", "Kill 100 Scathas", 100),
    scatha_kills_4("Scatha-Pro", "Kill 1,000 Scathas", 1000),
    scatha_kills_5("The show must go on!", "Kill 10,000 Scathas", 10000, Type.BONUS),
    
    scatha_farming_streak_1("Daily Scatha grind", "Farm Scathas for 3 days in a row", 3),
    scatha_farming_streak_2("No day off", "Farm Scathas for 5 days in a row", 5),
    scatha_farming_streak_3("Scatha week", "Farm Scathas for 7 days in a row", 7),
    scatha_farming_streak_4("A fortnight of Scathas", "Farm Scathas for 14 days in a row", 14, Type.BONUS),
    scatha_farming_streak_5("Scatha month", "Farm Scathas for 30 days in a row", 30, Type.BONUS),
    scatha_farming_streak_business_days("Barry's business days", "Farm Scathas every day for a whole business week", 5),
    scatha_farming_streak_weekend("Weekend well spent", "Farm Scathas on both days of a weekend", 2),
    
    scatha_pet_drop_1_rare("Better than nothing", "Get a rare Scatha pet drop", 1),
    scatha_pet_drop_2_rare("Pocket money", "Get 3 rare Scatha pet drops", 3),
    scatha_pet_drop_3_rare("I'm blue da ba dee da ba di", "Get 10 rare Scatha pet drops", 10),
    
    scatha_pet_drop_1_epic("Mid", "Get an epic Scatha pet drop", 1),
    scatha_pet_drop_2_epic("3pic", "Get 3 epic Scatha pet drops", 3),
    scatha_pet_drop_3_epic("Epic Scatha farmer", "Get 10 epic Scatha pet drops", 10, Type.BONUS),
    
    scatha_pet_drop_1_legendary("Jackpot!", "Get a legendary Scatha pet drop", 1),
    scatha_pet_drop_2_legendary("Golden trio", "Get 3 legendary Scatha pet drops", 3, Type.BONUS),
    scatha_pet_drop_3_legendary("Scatha billionaire", "Get 10 legendary Scatha pet drops", 10, Type.BONUS),
    
    scatha_pet_drop_each("Full Scathadex", "Drop a Scatha pet of each rarity", 3),
    
    scatha_pet_drop_any_1("Scathavenger", "Drop 10 Scatha pets of any rarity", 10),
    scatha_pet_drop_any_2("Scathaddiction", "Drop 25 Scatha pets of any rarity", 25),
    scatha_pet_drop_any_3("Scathabundance", "Drop 50 Scatha pets of any rarity", 50, Type.BONUS),
    scatha_pet_drop_any_4("Scathascended", "Drop 100 Scatha pets of any rarity", 100, Type.BONUS),
    
    scatha_pet_drop_mode_normal("The default experience", "Drop a Scatha pet in vanilla mode", 1),
    scatha_pet_drop_mode_meme("Stonks", "Drop a Scatha pet in meme mode", 1),
    scatha_pet_drop_mode_anime("Scatha-Chan", "Drop a Scatha pet in anime mode", 1),
    scatha_pet_drop_mode_custom("Scatha tinkerer", "Drop a Scatha pet in an active custom mode", 1),

    scatha_pet_drop_super_secret_setting("Super Secret Scatha", "Drop a Scatha pet in a Super Secret Setting", 1, Type.BONUS),
    
    scatha_pet_drop_b2b("Sold your soul to RNGesus", "Drop two Scatha pets back to back", 2, Type.HIDDEN),
    
    hard_stone_mined_1("Rock solid", "Mine 1,000,000 hard stone", 1000000, Type.LEGACY),
    hard_stone_mined_2("Stoned", "Mine 10,000,000 hard stone", 10000000, Type.LEGACY),
    hard_stone_mined_3("Crystal hollowed", "Mine 100,000,000 hard stone", 100000000, Type.LEGACY),
    
    crystal_hollows_time_1("Time flies", "Spend 1 hour in a single Crystal Hollows lobby", 1),
    crystal_hollows_time_2("New home", "Spend 3 hours in a single Crystal Hollows lobby", 3),
    crystal_hollows_time_3("Touch some grass", "Spend 5 hours in a single Crystal Hollows lobby", 5, Type.BONUS),
    
    worm_kill_time_1("No time to waste", "Kill a worm less than a second after it spawned", 1, Type.SECRET),
    worm_kill_time_2("Yeah, I've got time!", "Kill a worm less than 3 seconds before it despawns", 1, Type.SECRET),
    worm_despawn("Bye, have a great time", "Let a worm despawn", 1, Type.SECRET),
    
    kill_weapons_regular_worm("Variety gamer", "Kill a regular worm using 5 different weapons", 5, Type.SECRET), // TODO: new name?
    kill_weapons_scatha("A fine collection", "Kill a Scatha using 10 different weapons", 10, Type.SECRET),
    
    scatha_spawn_time("Any%", "Spawn a Scatha in the 1st minute after joining a lobby", 1, Type.SECRET),
    
    lobby_kills_1("And they don't stop coming", "Kill 25 worms in a single lobby", 25),
    lobby_kills_2("Scatha grinding session", "Kill 50 worms in a single lobby", 50),
    lobby_kills_3("Lobby cleared", "Kill 100 worms in a single lobby", 100, Type.BONUS),
    
    scatha_kill_sneak("Sneak 100", "Spawn and kill a Scatha while sneaking the entire time", 1, Type.SECRET),
    scatha_kill_highground("Obi Wan would be proud", "Kill a Scatha from high ground", 1, Type.SECRET),
    scatha_hit_dirt("Bully Maguire", "Put some dirt in a Scatha's eye", 1, Type.SECRET),
    scatha_kill_juju("Juju Farmer", "Kill a Scatha with a Juju Shortbow", 1, Type.HIDDEN),
    scatha_kill_terminator("I'll be back!", "Kill a Scatha with a Terminator", 1, Type.HIDDEN),
    
    regular_worm_streak_1("Still perfectly normal", "Get 7 regular worm spawns in a row (in 1 lobby)", 7),
    regular_worm_streak_2("Unlucky number", "Get 13 regular worm spawns in a row (in 1 lobby)", 13),
    regular_worm_streak_3("Scathas are on vacation", "Get 20 regular worm spawns in a row (in 1 lobby)", 20),
    
    scatha_streak_1("This is getting out of hand", "Get 2 Scatha spawns back to back (in 1 lobby)", 2),
    scatha_streak_2("Oh baby a triple!", "Get 3 Scatha spawns back to back (in 1 lobby)", 3),
    scatha_streak_3("Four Scatha clover", "Get 4 Scatha spawns back to back (in 1 lobby)", 4),
    scatha_streak_4("Scatha Magnet", "Get 5 Scatha spawns back to back (in 1 lobby)", 5),
    
    scatha_spawn_chbottom("Hot Scatha farming place", "Spawn a Scatha at the bottom of the Crystal Hollows", 1),
    scatha_spawn_heat_burning("This is fine.", "Spawn a Scatha while being at or above 90 heat", 1, Type.SECRET),
    scatha_spawn_chtop("Reach for the sky", "Spawn a Scatha at the top of the Crystal Hollows", 1, Type.SECRET),
    scatha_spawn_scatha_helmet("Scatha impostor", "Wear a Scatha pet on your head and spawn a Scatha", 1, Type.SECRET),
    
    easter_egg_overlay_title("Scappa", "Get the easter egg overlay title", 1, Type.HIDDEN),
    
    meet_developer("The Creator", "Be in a lobby with " + ScathaPro.MODNAME + "'s developer", 1, Type.HIDDEN),
    
    cheat("Cheater", "Put impossible values into the " + ScathaPro.MODNAME + " savefile", 1, Type.HIDDEN);
    
    
    public enum Type
    {
        NORMAL(null, null, Visibility.VISIBLE),
        SECRET("Secret", EnumChatFormatting.AQUA.toString(), Visibility.TITLE_ONLY),
        HIDDEN("HIDDEN", EnumChatFormatting.RED.toString(), Visibility.HIDDEN),
        BONUS("BONUS", EnumChatFormatting.YELLOW.toString(), Visibility.HIDDEN),
        LEGACY("LEGACY", EnumChatFormatting.DARK_PURPLE.toString(), Visibility.HIDDEN);
        
        public enum Visibility
        {
            VISIBLE, TITLE_ONLY, HIDDEN;
        }
        
        public final String typeName;
        public final String formatting;
        public Visibility visibility;
        
        private Type(String typeName, String formatting, Visibility visibility)
        {
            this.typeName = typeName;
            this.formatting = formatting;
            this.visibility = visibility;
        }
        
        public String getFormattedName()
        {
            if (typeName == null) return null;
            return (formatting != null ? formatting : "") + typeName;
        }
        
        @Override
        public String toString()
        {
            return typeName != null ? typeName : "Unnamed achievement type";
        }
    }
    
    
    public final String achievementName;
    public final String description;
    public final float goal;
    public final Type type;
    private float progress = 0f;

    Achievement(String name, String description, float goal)
    {
        this(name, description, goal, Type.NORMAL);
    }
    
    Achievement(String name, String description, float goal, Type type)
    {
        this.achievementName = name;
        this.description = description;
        this.goal = goal;
        this.type = type;
    }
    
    public String getID()
    {
        return this.name();
    }
    
    public void setProgress(float progress)
    {
        if (ScathaPro.getInstance().getAchievementManager().isAchievementUnlocked(this))
        {
            this.progress = goal;
            return;
        }
        
        if (progress >= goal)
        {
            this.progress = goal;
            ScathaPro.getInstance().getAchievementManager().unlockAchievement(this);
        }
        else this.progress = progress;
    }
    
    public float getProgress()
    {
        return progress;
    }
    
    public void unlock()
    {
        setProgress(goal);
    }

    public static Achievement getByID(String id)
    {
        try
        {
            return Achievement.valueOf(id);
        }
        catch (IllegalArgumentException e) {}
        return null;
    }
}
