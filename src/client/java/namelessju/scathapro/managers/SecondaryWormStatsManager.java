package namelessju.scathapro.managers;

import namelessju.scathapro.miscellaneous.data.enums.SecondaryWormStatsType;
import namelessju.scathapro.files.PersistentData;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class SecondaryWormStatsManager
{
    private final Map<SecondaryWormStatsType, SecondaryWormStats> statTypes = HashMap.newHashMap(3);
    
    public final SecondaryWormStats perLobbyStats = register(SecondaryWormStatsType.PER_LOBBY, new SimpleSecondaryWormStats());
    public final SecondaryWormStats perSessionStats = register(SecondaryWormStatsType.PER_SESSION, new SimpleSecondaryWormStats());
    public final SecondaryWormStats perDayStats;
    
    public SecondaryWormStatsManager(PersistentDataProfileManager profileManager)
    {
        perDayStats = register(SecondaryWormStatsType.PER_DAY, new WormStatsToday(profileManager));
    }
    
    private SecondaryWormStats register(SecondaryWormStatsType type, SecondaryWormStats stats)
    {
        statTypes.put(type, stats);
        return stats;
    }
    
    public SecondaryWormStats getStatsByType(SecondaryWormStatsType type)
    {
        return statTypes.get(type);
    }
    
    public void addRegularWormSpawn()
    {
        for (SecondaryWormStats stats : statTypes.values())
        {
            int spawnStreak = stats.getScathaSpawnStreak();
            if (spawnStreak > 0) spawnStreak = 0;
            spawnStreak --;
            stats.setScathaSpawnStreak(spawnStreak);
        }
    }
    
    public void addScathaSpawn()
    {
        for (SecondaryWormStats stats : statTypes.values())
        {
            int spawnStreak = stats.getScathaSpawnStreak();
            if (spawnStreak < 0) spawnStreak = 0;
            spawnStreak ++;
            stats.setScathaSpawnStreak(spawnStreak);
        }
    }
    
    public void addRegularWormKill()
    {
        for (SecondaryWormStats stats : statTypes.values())
        {
            stats.setRegularWormKills(stats.getRegularWormKills() + 1);
        }
    }
    
    public void addScathaKill()
    {
        for (SecondaryWormStats stats : statTypes.values())
        {
            stats.setScathaKills(stats.getScathaKills() + 1);
        }
    }
    
    public abstract static class SecondaryWormStats
    {
        public abstract int getRegularWormKills();
        public abstract void setRegularWormKills(int value);
        public abstract int getScathaKills();
        public abstract void setScathaKills(int value);
        /** positive = Scatha streak; negative = regular worm streak */
        public abstract int getScathaSpawnStreak();
        /** positive = Scatha streak; negative = regular worm streak */
        public abstract void setScathaSpawnStreak(int value);
        
        public final void reset()
        {
            setRegularWormKills(0);
            setScathaKills(0);
            setScathaSpawnStreak(0);
        }
    }
    
    private static class SimpleSecondaryWormStats extends SecondaryWormStats
    {
        private int regularWormKills = 0;
        private int scathaKills = 0;
        private int scathaSpawnStreak = 0;
        
        @Override
        public int getRegularWormKills()
        {
            return regularWormKills;
        }
        
        @Override
        public void setRegularWormKills(int value)
        {
            regularWormKills = value;
        }
        
        @Override
        public int getScathaKills()
        {
            return scathaKills;
        }
        
        @Override
        public void setScathaKills(int value)
        {
            scathaKills = value;
        }
        
        @Override
        public int getScathaSpawnStreak()
        {
            return scathaSpawnStreak;
        }
        
        @Override
        public void setScathaSpawnStreak(int value)
        {
            scathaSpawnStreak = value;
        }
    }
    
    private static class WormStatsToday extends SecondaryWormStats
    {
        private final @NonNull PersistentDataProfileManager profileManager;
        
        public WormStatsToday(@NonNull PersistentDataProfileManager profileManager)
        {
            this.profileManager = profileManager;
        }
        
        @Override
        public int getRegularWormKills()
        {
            return getPersistentStats().regularWormKills.get();
        }
        
        @Override
        public void setRegularWormKills(int value)
        {
            getPersistentStats().regularWormKills.set(value);
        }
        
        @Override
        public int getScathaKills()
        {
            return getPersistentStats().scathaKills.get();
        }
        
        @Override
        public void setScathaKills(int value)
        {
            getPersistentStats().scathaKills.set(value);
        }
        
        @Override
        public int getScathaSpawnStreak()
        {
            return getPersistentStats().scathaSpawnStreak.get();
        }
        
        @Override
        public void setScathaSpawnStreak(int value)
        {
            getPersistentStats().scathaSpawnStreak.set(value);
        }
        
        private PersistentData.ProfileData.@NonNull WormStatsToday getPersistentStats()
        {
            return profileManager.getCurrentProfileData().wormStatsToday;
        }
    }
}
