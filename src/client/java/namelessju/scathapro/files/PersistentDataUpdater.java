package namelessju.scathapro.files;

import com.google.gson.JsonElement;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.achievements.UnlockedAchievement;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

public class PersistentDataUpdater implements BiConsumer<@Nullable JsonElement, PersistentData>
{
    @Override
    public void accept(@Nullable JsonElement jsonElement, PersistentData persistentData)
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

        // NO SAVING, gets called before backups are made
    }
}