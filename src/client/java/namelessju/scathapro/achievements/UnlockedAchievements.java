package namelessju.scathapro.achievements;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import namelessju.scathapro.files.framework.JsonFile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;

public class UnlockedAchievements implements JsonFile.JsonValue
{
    private final HashMap<String, UnlockedAchievement> unlockedAchievements = new HashMap<>();
    
    public @Nullable UnlockedAchievement getFor(@NonNull Achievement achievement)
    {
        return getById(achievement.id);
    }
    
    public @Nullable UnlockedAchievement getById(@NonNull String achievementId)
    {
        return unlockedAchievements.get(achievementId);
    }
    
    public Collection<UnlockedAchievement> getAll()
    {
        return unlockedAchievements.values();
    }
    
    public boolean isUnlocked(Achievement achievement)
    {
        return unlockedAchievements.containsKey(achievement.id);
    }
    
    public void add(UnlockedAchievement unlockedAchievement)
    {
        unlockedAchievements.put(unlockedAchievement.achievement.id, unlockedAchievement);
    }
    
    public boolean remove(Achievement achievement)
    {
        return unlockedAchievements.remove(achievement.id) != null;
    }
    
    @Override
    public boolean hasValue()
    {
        return !unlockedAchievements.isEmpty();
    }
    
    @Override
    public void reset()
    {
        unlockedAchievements.clear();
    }
    
    @Override
    public void loadFromJson(@Nullable JsonElement jsonElement)
    {
        reset();
        
        if (!(jsonElement instanceof JsonArray jsonArray)) return;
        for (JsonElement arrayElement : jsonArray)
        {
            UnlockedAchievement unlockedAchievement = ACHIEVEMENTS_SERIALIZER.jsonToValue(arrayElement);
            if (unlockedAchievement != null)
            {
                add(unlockedAchievement);
            }
        }
    }
    
    @Override
    public @NonNull JsonElement getAsJson(@NonNull JsonFile jsonFile)
    {
        JsonArray jsonArray = new JsonArray();
        for (UnlockedAchievement unlockedAchievement : getAll())
        {
            jsonArray.add(ACHIEVEMENTS_SERIALIZER.valueToJson(unlockedAchievement));
        }
        return jsonArray;
    }
    
    private static final JsonFile.JsonValue.Serializer<UnlockedAchievement, JsonElement> ACHIEVEMENTS_SERIALIZER = new JsonFile.JsonValue.Serializer<>() {
        private final JsonFile.ObjectChildSerializer<Achievement> achievementSerializer
            = new JsonFile.ObjectChildSerializer<>("achievementID", new JsonFile.EnumSerializer<>(Achievement.class));
        private final JsonFile.ObjectChildSerializer<Long> unlockTimestampSerializer
            = new JsonFile.ObjectChildSerializer<>("unlockedAt", JsonFile.LONG_SERIALIZER);
        private final JsonFile.ObjectChildSerializer<Integer> repeatCountSerializer
            = new JsonFile.ObjectChildSerializer<>("repeatCount", JsonFile.INTEGER_SERIALIZER);
        
        @Override
        public @Nullable UnlockedAchievement jsonToValue(@NonNull JsonElement jsonElement)
        {
            if (!(jsonElement instanceof JsonObject jsonObject)) return null;
            if (!(achievementSerializer.deserialize(jsonObject) instanceof Achievement achievement)) return null;
            Long unlockTimestamp = unlockTimestampSerializer.deserialize(jsonObject);
            if (unlockTimestamp == null) unlockTimestamp = -1L;
            Integer repeatCount = repeatCountSerializer.deserialize(jsonObject);
            return repeatCount != null
                ? new UnlockedAchievement(achievement, unlockTimestamp, repeatCount)
                : new UnlockedAchievement(achievement, unlockTimestamp);
        }
        
        @Override
        public @NonNull JsonElement valueToJson(@NonNull UnlockedAchievement value)
        {
            JsonObject jsonObject = new JsonObject();
            achievementSerializer.serialize(jsonObject, value.achievement);
            unlockTimestampSerializer.serialize(jsonObject, value.unlockTimestamp);
            repeatCountSerializer.serialize(jsonObject, value.getRepeatCount());
            return jsonObject;
        }
    };
}