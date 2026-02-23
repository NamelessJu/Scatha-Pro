package namelessju.scathapro.achievements;

import namelessju.scathapro.ScathaPro;
import org.jspecify.annotations.NonNull;

public enum Achievement
{
    /**********************
     *                    *
     *   SPOILER ALERT!   *
     *                    *
     **********************/
    
    achievements_unlocked_half(AchievementCategory.PROGRESS, "Whoa, we're halfway there...", "Unlock 50% of all non-hidden achievements", 50, AchievementType.HIDDEN),
    achievements_unlocked_all(AchievementCategory.PROGRESS, "Completionist", "Unlock all non-hidden achievements", 100, AchievementType.HIDDEN),
    
    worm_kills_1(AchievementCategory.KILL_AMOUNTS, "Long boi", "Kill a worm", 1),
    worm_kills_2(AchievementCategory.KILL_AMOUNTS, "Two digits", "Kill 10 worms", 10),
    worm_kills_3(AchievementCategory.KILL_AMOUNTS, "Bestiary magic find", "Kill 120 worms", 120),
    worm_bestiary_max(AchievementCategory.KILL_AMOUNTS, "Worm expert", "Complete the worm bestiary", 400),
    worm_kills_4(AchievementCategory.KILL_AMOUNTS, "k", "Kill 1,000 worms", 1000),
    worm_kills_5(AchievementCategory.KILL_AMOUNTS, "Worm annihilator", "Kill 10,000 worms", 10_000),
    worm_kills_6(AchievementCategory.KILL_AMOUNTS, "Endangered species", "Kill 25,000 worms", 25_000, AchievementType.BONUS),
    worm_kills_7(AchievementCategory.KILL_AMOUNTS, "Very big number", "Kill 100,000 worms", 100_000, AchievementType.BONUS),
    
    scatha_kills_1(AchievementCategory.KILL_AMOUNTS, "Scatha Farmer", "Become a Scatha farmer by killing your first Scatha", 1),
    scatha_kills_2(AchievementCategory.KILL_AMOUNTS, "No pet yet?!", "Kill 10 Scathas", 10),
    scatha_kills_3(AchievementCategory.KILL_AMOUNTS, "Rookie numbers", "Kill 100 Scathas", 100),
    scatha_kills_4(AchievementCategory.KILL_AMOUNTS, "Scatha pro", "Kill 1,000 Scathas", 1000),
    scatha_kills_5(AchievementCategory.KILL_AMOUNTS, "The show must go on!", "Kill 10,000 Scathas", 10_000, AchievementType.BONUS),
    scatha_kills_repeatable(AchievementCategory.KILL_AMOUNTS, "The grind never stops", "Kill 1,000 more Scathas", 1000, AchievementType.BONUS, true),
    
    scatha_pet_drop_1_rare(AchievementCategory.SCATHA_PET_DROPS, "Better than nothing", "Get a rare Scatha pet drop", 1),
    scatha_pet_drop_2_rare(AchievementCategory.SCATHA_PET_DROPS, "Pocket money", "Get 3 rare Scatha pet drops", 3),
    scatha_pet_drop_3_rare(AchievementCategory.SCATHA_PET_DROPS, "I'm blue da ba dee da ba di", "Get 10 rare Scatha pet drops", 10),
    
    scatha_pet_drop_1_epic(AchievementCategory.SCATHA_PET_DROPS, "Mid", "Get an epic Scatha pet drop", 1),
    scatha_pet_drop_2_epic(AchievementCategory.SCATHA_PET_DROPS, "3pic", "Get 3 epic Scatha pet drops", 3),
    scatha_pet_drop_3_epic(AchievementCategory.SCATHA_PET_DROPS, "Epic Scatha farmer", "Get 10 epic Scatha pet drops", 10, AchievementType.BONUS),
    
    scatha_pet_drop_1_legendary(AchievementCategory.SCATHA_PET_DROPS, "Jackpot!", "Get a legendary Scatha pet drop", 1),
    scatha_pet_drop_2_legendary(AchievementCategory.SCATHA_PET_DROPS, "Golden trio", "Get 3 legendary Scatha pet drops", 3, AchievementType.BONUS),
    scatha_pet_drop_3_legendary(AchievementCategory.SCATHA_PET_DROPS, "Scatha billionaire", "Get 10 legendary Scatha pet drops", 10, AchievementType.BONUS),
    
    scatha_pet_drop_each(AchievementCategory.SCATHA_PET_DROPS, "Full Scathadex", "Drop a Scatha pet of each rarity", 3),
    
    scatha_pet_drop_any_1(AchievementCategory.SCATHA_PET_DROPS, "Scathavenger", "Drop 10 Scatha pets of any rarity", 10),
    scatha_pet_drop_any_2(AchievementCategory.SCATHA_PET_DROPS, "Scathaddiction", "Drop 25 Scatha pets of any rarity", 25),
    scatha_pet_drop_any_3(AchievementCategory.SCATHA_PET_DROPS, "Scathabundance", "Drop 50 Scatha pets of any rarity", 50, AchievementType.BONUS),
    scatha_pet_drop_any_4(AchievementCategory.SCATHA_PET_DROPS, "Scathascended", "Drop 100 Scatha pets of any rarity", 100, AchievementType.BONUS),
    scatha_pet_drop_any_repeatable(AchievementCategory.SCATHA_PET_DROPS, "Another dozen Scathas", "Drop 12 more Scatha pets of any rarity", 12, AchievementType.BONUS, true),
    
    scatha_pet_drop_dry_streak_1(AchievementCategory.SCATHA_PET_DROPS, "Aw dang it", "Go 100 Scatha kills without dropping a pet", 100, AchievementType.SECRET),
    scatha_pet_drop_dry_streak_2(AchievementCategory.SCATHA_PET_DROPS, "404 Scatha pet not found", "Go 404 Scatha kills without dropping a pet", 404, AchievementType.HIDDEN, true),

    scatha_pet_drop_super_secret_setting(AchievementCategory.SCATHA_PET_DROPS, "Super Secret Scatha", "Drop a Scatha pet in a Super Secret Setting", 1, AchievementType.LEGACY),
    scatha_pet_drop_b2b(AchievementCategory.SCATHA_PET_DROPS, "Sold your soul to RNGesus", "Drop two Scatha pets back to back", 2, AchievementType.HIDDEN, true),
    
    scatha_pet_drop_mode_normal(AchievementCategory.ALERT_MODES, "The default experience", "Drop a Scatha pet in vanilla mode", 1),
    scatha_pet_drop_mode_meme(AchievementCategory.ALERT_MODES, "Stonks", "Drop a Scatha pet in meme mode", 1),
    scatha_pet_drop_mode_anime(AchievementCategory.ALERT_MODES, "Scatha-Chan", "Drop a Scatha pet in anime mode", 1),
    scatha_pet_drop_mode_custom(AchievementCategory.ALERT_MODES, "Scatha tinkerer", "Drop a Scatha pet in an active custom mode", 1),
    
    hard_stone_mined_1(AchievementCategory.HARD_STONE, "Rock solid", "Mined 1,000,000 hard stone", 1_000_000, AchievementType.LEGACY),
    hard_stone_mined_2(AchievementCategory.HARD_STONE, "Stoned", "Mined 10,000,000 hard stone", 10_000_000, AchievementType.LEGACY),
    hard_stone_mined_3(AchievementCategory.HARD_STONE, "Crystal hollowed", "Mined 100,000,000 hard stone", 100_000_000, AchievementType.LEGACY),
    
    crystal_hollows_time_1(AchievementCategory.LOBBY_TIMER, "Time flies", "Spend 1 hour in a single Crystal Hollows lobby", 1),
    crystal_hollows_time_2(AchievementCategory.LOBBY_TIMER, "New home", "Spend 3 hours in a single Crystal Hollows lobby", 3),
    crystal_hollows_time_3(AchievementCategory.LOBBY_TIMER, "Touch some grass", "Spend 5 hours in a single Crystal Hollows lobby", 5, AchievementType.BONUS, true),
    
    worm_kill_time_1(AchievementCategory.WORM_LIFETIME, "No time to waste", "Kill a worm less than a second after it spawned", 1, AchievementType.SECRET),
    worm_kill_time_2(AchievementCategory.WORM_LIFETIME, "Yeah, I've got time!", "Kill a worm less than 3 seconds before it despawns", 1, AchievementType.SECRET),
    worm_despawn(AchievementCategory.WORM_LIFETIME, "Bye, have a great time", "Let a worm despawn", 1, AchievementType.SECRET),
    
    lobby_kills_1(AchievementCategory.TIMEFRAME_KILLS, "And they don't stop coming", "Kill 25 worms in a single lobby", 25),
    lobby_kills_2(AchievementCategory.TIMEFRAME_KILLS, "Scatha grinding session", "Kill 50 worms in a single lobby", 50),
    lobby_kills_3(AchievementCategory.TIMEFRAME_KILLS, "Lobby cleared", "Kill 100 worms in a single lobby", 100, AchievementType.BONUS, true),
    
    day_kills_1(AchievementCategory.TIMEFRAME_KILLS, "A good day for killing worms", "Kill 50 worms in a single real life day", 50),
    day_kills_2(AchievementCategory.TIMEFRAME_KILLS, "On today's agenda: Scatha farming", "Kill 100 worms in a single real life day", 100),
    day_kills_3(AchievementCategory.TIMEFRAME_KILLS, "Full time job", "Kill 250 worms in a single real life day", 250, AchievementType.BONUS, true),
    
    scatha_farming_streak_1(AchievementCategory.SCATHA_FARMING_STREAK, "Daily Scatha grind", "Farm Scathas for 3 days in a row", 3),
    scatha_farming_streak_2(AchievementCategory.SCATHA_FARMING_STREAK, "Growing the streak", "Farm Scathas for 5 days in a row", 5),
    scatha_farming_streak_3(AchievementCategory.SCATHA_FARMING_STREAK, "Scathas for a week", "Farm Scathas for 7 days in a row", 7),
    scatha_farming_streak_4(AchievementCategory.SCATHA_FARMING_STREAK, "A fortnight of Scathas", "Farm Scathas for 14 days in a row", 14, AchievementType.BONUS),
    scatha_farming_streak_5(AchievementCategory.SCATHA_FARMING_STREAK, "No day off", "Farm Scathas for 30 days in a row", 30, AchievementType.BONUS),
    scatha_farming_streak_business_days(AchievementCategory.SCATHA_FARMING_STREAK, "Barry's business days", "Farm Scathas every day for a whole business week", 5),
    scatha_farming_streak_weekend(AchievementCategory.SCATHA_FARMING_STREAK, "Weekend well spent", "Farm Scathas on both days of a weekend", 2),
    
    regular_worm_streak_1(AchievementCategory.SPAWN_STREAK, "Still perfectly normal", "Get 7 regular worm spawns in a row (in a single lobby)", 7),
    regular_worm_streak_2(AchievementCategory.SPAWN_STREAK, "Unlucky number", "Get 13 regular worm spawns in a row (in a single lobby)", 13),
    regular_worm_streak_3(AchievementCategory.SPAWN_STREAK, "Scathas are on vacation", "Get 20 regular worm spawns in a row (in a single lobby)", 20, AchievementType.NORMAL, true),
    
    scatha_streak_1(AchievementCategory.SPAWN_STREAK, "This is getting out of hand", "Get 2 consecutive Scatha spawns (in a single lobby)", 2),
    scatha_streak_2(AchievementCategory.SPAWN_STREAK, "Oh baby a triple!", "Get 3 consecutive Scatha spawns (in a single lobby)", 3),
    scatha_streak_3(AchievementCategory.SPAWN_STREAK, "Four Scatha clover", "Get 4 consecutive Scatha spawns (in a single lobby)", 4),
    scatha_streak_4(AchievementCategory.SPAWN_STREAK, "Scatha Magnet", "Get 5 consecutive Scatha spawns (in a single lobby)", 5, AchievementType.NORMAL, true),
    
    scatha_spawn_chbottom(AchievementCategory.SCATHA_SPAWNS, "Hot Scatha farming place", "Spawn a Scatha at the bottom of the Crystal Hollows", 1),
    scatha_spawn_heat_burning(AchievementCategory.SCATHA_SPAWNS, "This is fine.", "Spawn a Scatha while being at or above 99 heat", 1, AchievementType.SECRET),
    scatha_spawn_chtop(AchievementCategory.SCATHA_SPAWNS, "Reach for the sky", "Spawn a Scatha at the top of the Crystal Hollows", 1, AchievementType.SECRET),
    scatha_spawn_time(AchievementCategory.SCATHA_SPAWNS, "Any%", "Spawn a Scatha in the 1st minute after joining a lobby", 1, AchievementType.SECRET),
    scatha_spawn_time_cooldown_end(AchievementCategory.SCATHA_SPAWNS, "Don't keep me waiting", "Spawn a Scatha in less than 3s after the spawn cooldown ends", 1, AchievementType.SECRET),
    scatha_spawn_scatha_helmet(AchievementCategory.SCATHA_SPAWNS, "Scatha impostor", "Wear a Scatha pet on your head and spawn a Scatha", 1, AchievementType.SECRET),
    
    kill_weapons_regular_worm(AchievementCategory.WORM_HIT_KILL, "One for each segment", "Kill a regular worm using 5 different \"weapons\"", 5, AchievementType.SECRET),
    kill_weapons_scatha(AchievementCategory.WORM_HIT_KILL, "A fine collection", "Kill a Scatha using 10 different \"weapons\"", 10, AchievementType.SECRET),
    kill_perfect_gemstone_gauntlet(AchievementCategory.WORM_HIT_KILL, "In the snap of a finger", "Kill a worm using a golden Gemstone Gauntlet", 10, AchievementType.HIDDEN),
    scatha_kill_sneak(AchievementCategory.WORM_HIT_KILL, "Sneak 100", "Spawn and kill a Scatha while sneaking the entire time", 1, AchievementType.SECRET),
    scatha_kill_highground(AchievementCategory.WORM_HIT_KILL, "Obi Wan would be proud", "Kill a Scatha from high ground", 1, AchievementType.SECRET),
    scatha_hit_dirt(AchievementCategory.WORM_HIT_KILL, "Bully Maguire", "Put some dirt in a Scatha's eye", 1, AchievementType.SECRET),
    scatha_kill_gemstone(AchievementCategory.WORM_HIT_KILL, "Return to sender", "Kill a Scatha with one of the gemstones they can drop", 1, AchievementType.SECRET),
    scatha_kill_juju(AchievementCategory.WORM_HIT_KILL, "Juju Farmer", "Kill a Scatha with a Juju Shortbow", 1, AchievementType.HIDDEN),
    scatha_kill_terminator(AchievementCategory.WORM_HIT_KILL, "I'll be back!", "Kill a Scatha with a Terminator", 1, AchievementType.HIDDEN),
    
    // DO NOT RENAME! (save file depends on these IDs)
    anomalous_desire_waste(AchievementCategory.ABILITY, "Wasted Vision", "Waste most of the Tunnel Vision ability during the worm spawn cooldown", 1, AchievementType.SECRET),
    anomalous_desire_recover(AchievementCategory.ABILITY, "Resourceful Vision", "Spawn a worm with Tunnel Vision after wasting more than half of it during the worm spawn cooldown", 1, AchievementType.SECRET),
    
    easter_egg_overlay_title(AchievementCategory.MISCELLANEOUS, "Scappa (pre-release ver.)", "Got the easter egg overlay title (1/200 chance per game start)", 1, AchievementType.LEGACY),
    scappa_mode(AchievementCategory.MISCELLANEOUS, "Scappa", "Unlock Scappa mode (1/250 chance per Scatha kill)", 1, AchievementType.HIDDEN),
    april_fools(AchievementCategory.MISCELLANEOUS, "April Fools", "Kill a Scatha on April 1st", 1, AchievementType.HIDDEN),
    update_mod_to_v2(AchievementCategory.MISCELLANEOUS, "New version, who dis?", "Update " + ScathaPro.MOD_NAME + " from 1.8.9 to latest version", 1, AchievementType.HIDDEN),
    play_mod_pre_release(AchievementCategory.MISCELLANEOUS, "Scatha Tester", "Play on a " + ScathaPro.MOD_NAME + " pre-release", 1, AchievementType.HIDDEN),
    meet_developer(AchievementCategory.MISCELLANEOUS, "The Creator", "Be in a lobby with the " + ScathaPro.MOD_NAME + " developer", 1, AchievementType.HIDDEN),
    cheat(AchievementCategory.MISCELLANEOUS, "Cheater", "Put impossible values into the " + ScathaPro.MOD_NAME + " savefile", 1, AchievementType.HIDDEN);
    
    
    
    public final @NonNull String id;
    public final @NonNull String achievementName;
    public final @NonNull String description;
    public final @NonNull AchievementCategory category;
    public final @NonNull AchievementType type;
    public final float goal;
    public final boolean isRepeatable;
    
    private float progress = 0f;
    
    Achievement(@NonNull AchievementCategory category, @NonNull String name, @NonNull String description, float goal)
    {
        this(category, name, description, goal, AchievementType.NORMAL);
    }
    
    Achievement(@NonNull AchievementCategory category, @NonNull String name, @NonNull String description, float goal, @NonNull AchievementType type)
    {
        this(category, name, description, goal, type, false);
    }
    
    Achievement(@NonNull AchievementCategory category, @NonNull String name, @NonNull String description,
                float goal, @NonNull AchievementType type, boolean repeatable)
    {
        this.id = name();
        this.category = category;
        this.achievementName = name;
        this.description = description;
        this.goal = goal;
        this.type = type;
        this.isRepeatable = repeatable;
    }
    
    // TODO: refactor, logic is split between achievement and achievement manager
    
    public void setProgress(float newProgress)
    {
        setProgress(newProgress, true);
    }
    
    public void setProgress(float newProgress, boolean allowUnlocking)
    {
        if (allowUnlocking && (!this.isRepeatable || this.progress < goal) && newProgress >= goal)
        {
            unlock();
        }
        
        this.progress = newProgress;
    }
    
    /**
     * @param startValue The value at which to start counting the progress
     * @param value The value to apply to the repeating progress
     */
    public void setRepeatingProgress(float startValue, float value, boolean allowUnlocking)
    {
        float progressValue = value - startValue;
        
        int goalReachedCount = (int) Math.max(progressValue / this.goal, 0f);
        
        UnlockedAchievement unlockedAchievement = ScathaPro.getInstance().getProfileData().unlockedAchievements.getFor(this);
        if (unlockedAchievement != null && unlockedAchievement.getRepeatCount() > goalReachedCount - 1)
        {
            setProgress(0f, false);
            return;
        }
        
        if (allowUnlocking && goalReachedCount >= 1)
        {
            ScathaPro.getInstance().achievementManager.unlockAchievement(this, goalReachedCount);
        }
        
        setProgress(progressValue > 0f ? progressValue % this.goal : 0f, false);
    }
    
    public float getProgress()
    {
        return progress;
    }
    
    /**
     * Progress clamped between 0 and goal
     */
    public float getClampedProgress()
    {
        return Math.clamp(progress, 0f, goal);
    }
    
    public void unlock()
    {
        ScathaPro.getInstance().achievementManager.unlockAchievement(this);
    }
    
    public static Achievement getByID(String id)
    {
        try
        {
            return Achievement.valueOf(id);
        }
        catch (IllegalArgumentException ignored) {}
        return null;
    }
}
