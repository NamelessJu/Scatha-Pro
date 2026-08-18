package namelessju.scathapro.files;

import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.achievements.UnlockedAchievement;

public class PersistentDataUpdater
{
    public static void updateAfterLoading(PersistentData persistentData)
    {
        for (PersistentData.PlayerData playerData : persistentData.players)
        {
            for (PersistentData.ProfileData profileData : playerData.profiles)
            {
                UnlockedAchievement ogMaxBestiaryUnlockedAchievement = profileData.unlockedAchievements.getFor(Achievement.worm_bestiary_max);
                if (ogMaxBestiaryUnlockedAchievement != null
                    && !profileData.unlockedAchievements.isUnlocked(Achievement.worm_bestiary_max_v2))
                {
                    profileData.unlockedAchievements.add(new UnlockedAchievement(
                        Achievement.worm_bestiary_max_v2,
                        ogMaxBestiaryUnlockedAchievement.unlockTimestamp,
                        ogMaxBestiaryUnlockedAchievement.getRepeatCount()
                    ));
                }
            }
        }
    }
}