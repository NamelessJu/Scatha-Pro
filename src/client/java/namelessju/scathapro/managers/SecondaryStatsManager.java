package namelessju.scathapro.managers;

import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.miscellaneous.data.enums.SecondaryWormStatsType;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public class SecondaryStatsManager
{
    private final Map<SecondaryWormStatsType, SecondaryStats> statTypes = HashMap.newHashMap(3);

    public final SecondaryStats perLobbyStats = register(SecondaryWormStatsType.PER_LOBBY, new SimpleSecondaryStats());
    public final SecondaryStats perSessionStats = register(SecondaryWormStatsType.PER_SESSION, new SimpleSecondaryStats());
    public final SecondaryStats perDayStats;

    public SecondaryStatsManager(PersistentDataProfileManager profileManager)
    {
        perDayStats = register(SecondaryWormStatsType.PER_DAY, new StatsToday(profileManager));
    }

    private SecondaryStats register(SecondaryWormStatsType type, SecondaryStats stats)
    {
        statTypes.put(type, stats);
        return stats;
    }

    public SecondaryStats getStatsByType(SecondaryWormStatsType type)
    {
        return statTypes.get(type);
    }

    public void addRegularWormSpawn()
    {
        for (SecondaryStats stats : statTypes.values())
        {
            int spawnStreak = stats.getScathaSpawnStreak();
            if (spawnStreak > 0) spawnStreak = 0;
            spawnStreak --;
            stats.setScathaSpawnStreak(spawnStreak);
        }
    }

    public void addScathaSpawn()
    {
        for (SecondaryStats stats : statTypes.values())
        {
            int spawnStreak = stats.getScathaSpawnStreak();
            if (spawnStreak < 0) spawnStreak = 0;
            spawnStreak ++;
            stats.setScathaSpawnStreak(spawnStreak);
        }
    }

    public void addRegularWormKill()
    {
        for (SecondaryStats stats : statTypes.values())
        {
            stats.setRegularWormKills(stats.getRegularWormKills() + 1);
        }
    }

    public void addScathaKill()
    {
        for (SecondaryStats stats : statTypes.values())
        {
            stats.setScathaKills(stats.getScathaKills() + 1);
        }
    }

    public void addBlockBran()
    {
        for (SecondaryStats stats : statTypes.values())
        {
            stats.setBlockBransDropped(stats.getBlockBransDropped() + 1);
        }
    }

    public abstract static class SecondaryStats
    {
        public abstract int getRegularWormKills();
        public abstract void setRegularWormKills(int value);
        public abstract int getScathaKills();
        public abstract void setScathaKills(int value);
        /** positive = Scatha streak; negative = regular worm streak */
        public abstract int getScathaSpawnStreak();
        /** positive = Scatha streak; negative = regular worm streak */
        public abstract void setScathaSpawnStreak(int value);
        public abstract int getBlockBransDropped();
        public abstract void setBlockBransDropped(int value);

        public final void reset()
        {
            setRegularWormKills(0);
            setScathaKills(0);
            setScathaSpawnStreak(0);
            setBlockBransDropped(0);
        }
    }

    private static class SimpleSecondaryStats extends SecondaryStats
    {
        private int regularWormKills = 0;
        private int scathaKills = 0;
        private int scathaSpawnStreak = 0;
        private int blockBrans = 0;

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

        @Override
        public int getBlockBransDropped()
        {
            return blockBrans;
        }

        @Override
        public void setBlockBransDropped(int value)
        {
            blockBrans = value;
        }
    }

    private static class StatsToday extends SecondaryStats
    {
        private final @NonNull PersistentDataProfileManager profileManager;

        public StatsToday(@NonNull PersistentDataProfileManager profileManager)
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

        @Override
        public int getBlockBransDropped()
        {
            return getPersistentStats().blockBransDropped.get();
        }

        @Override
        public void setBlockBransDropped(int value)
        {
            getPersistentStats().blockBransDropped.set(value);
        }

        private PersistentData.ProfileData.@NonNull StatsToday getPersistentStats()
        {
            return profileManager.getCurrentProfileData().statsToday;
        }
    }
}