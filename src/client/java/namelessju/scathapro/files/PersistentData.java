package namelessju.scathapro.files;

import com.google.gson.*;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.UnlockedAchievements;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.files.framework.ObjectRootJsonFile;
import namelessju.scathapro.miscellaneous.data.MagicFindSource;
import namelessju.scathapro.miscellaneous.data.enums.ShardsAttribute;
import namelessju.scathapro.miscellaneous.data.enums.WitchesStew;
import namelessju.scathapro.util.JsonUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PersistentData extends ObjectRootJsonFile
{
    public PersistentData(ScathaPro scathaPro)
    {
        super(scathaPro, scathaPro.getSaveDirectoryPath().resolve("persistentDataV2.json").toFile(), false);
    }


    public final JsonValueArrayValue<PlayerData> players = root.addValue("players", new JsonValueArrayValue<>(() -> new PlayerData(null)));
    public final static class PlayerData extends ObjectValue
    {
        public PlayerData(UUID playerUUID)
        {
            this.playerUUID.set(playerUUID);
        }

        public final PrimitiveValueNullable<UUID> playerUUID = addPrimitiveNullable("playerUUID", UUID_SERIALIZER);

        public final JsonValueArrayValue<ProfileData> profiles = addValue("profiles", new JsonValueArrayValue<>(() -> new ProfileData(null)));
    }

    public final static class ProfileData extends ObjectValue
    {
        public ProfileData(String profileID)
        {
            this.profileID.set(profileID);
        }

        public final PrimitiveValueNullable<String> profileID = addPrimitiveNullable("profileID", STRING_SERIALIZER);


        public final PrimitiveValueWithDefault<Integer> regularWormKills
            = addPrimitiveWithDefault("wormKills.regularWorms", INTEGER_SERIALIZER, 0);
        public final PrimitiveValueWithDefault<Integer> scathaKills
            = addPrimitiveWithDefault("wormKills.scathas", INTEGER_SERIALIZER, 0);

        public final PrimitiveValueWithDefault<Integer> rarePetDrops
            = addPrimitiveWithDefault("petDrops.rare", INTEGER_SERIALIZER, 0);
        public final PrimitiveValueWithDefault<Integer> epicPetDrops
            = addPrimitiveWithDefault("petDrops.epic", INTEGER_SERIALIZER, 0);
        public final PrimitiveValueWithDefault<Integer> legendaryPetDrops
            = addPrimitiveWithDefault("petDrops.legendary", INTEGER_SERIALIZER, 0);

        public final PrimitiveValueWithDefault<Integer> blockBransDropped
            = addPrimitiveWithDefault("extraDrops.blockBrans", INTEGER_SERIALIZER, 0);

        public final PrimitiveValueNullable<Integer> scathaKillsAtLastDrop
            = addPrimitiveNullable("dryStreak.scathaKillsAtLastDrop", INTEGER_SERIALIZER);
        public final BooleanValue isPetDropDryStreakInvalidated
            = addValue("dryStreak.isDryStreakInvalidated", new BooleanValue(false));

        public final PrimitiveValueNullable<LocalDate> lastPlayedDate = addPrimitiveNullable("realTime.lastPlayedDate", DATE_SERIALIZER);
        public final StatsToday statsToday = addValue("realTime.wormStatsToday", new StatsToday());
        public static class StatsToday extends ObjectValue
        {
            private StatsToday() {}
            public final PrimitiveValueWithDefault<Integer> regularWormKills
                = addPrimitiveWithDefault("wormKills.regularWorms", INTEGER_SERIALIZER, 0);
            public final PrimitiveValueWithDefault<Integer> scathaKills
                = addPrimitiveWithDefault("wormKills.scathas", INTEGER_SERIALIZER, 0);
            public final PrimitiveValueWithDefault<Integer> scathaSpawnStreak
                = addPrimitiveWithDefault("scathaSpawnStreak", INTEGER_SERIALIZER, 0);
            public final PrimitiveValueWithDefault<Integer> blockBransDropped
                = addPrimitiveWithDefault("blockBransDropped", INTEGER_SERIALIZER, 0);
        }
        public final PrimitiveValueNullable<LocalDate> lastScathaFarmedDate
            = addPrimitiveNullable("realTime.lastScathaFarmedDate", DATE_SERIALIZER);
        public final PrimitiveValueWithDefault<Integer> scathaFarmingStreak
            = addPrimitiveWithDefault("realTime.scathaFarmingStreak.current", INTEGER_SERIALIZER, 0);
        public final PrimitiveValueWithDefault<Integer> scathaFarmingStreakHighScore
            = addPrimitiveWithDefault("realTime.scathaFarmingStreak.highscore", INTEGER_SERIALIZER, 0);

        public final PrimitiveValueNullable<Float> globalMagicFind
            = addPrimitiveNullable("profileStats.magicFind", FLOAT_SERIALIZER);
        public final PrimitiveValueNullable<Float> wormBestiaryMagicFind
            = addPrimitiveNullable("profileStats.wormBestiaryMagicFind", FLOAT_SERIALIZER);
        public final PrimitiveValueNullable<Float> petLuck
            = addPrimitiveNullable("profileStats.petLuck", FLOAT_SERIALIZER);

        public final MagicFindSourceValue<WitchesStew> witchesStewsEaten
            = addValue("profileStats.witchesStewsEaten", new MagicFindSourceValue<>(WitchesStew.class, false));
        public final MagicFindSourceValue<ShardsAttribute> attributes
            = addValue("profileStats.attributes", new MagicFindSourceValue<>(ShardsAttribute.class, true));

        public final BooleanValue scappaModeUnlocked = addBoolean("misc.unlockables.scappaModeUnlocked", false);
        public final BooleanValue overlayIconGooglyEyesUnlocked = addBoolean("misc.unlockables.overlayIconGooglyEyesUnlocked", false);

        public final PrimitiveValueNullable<Integer> lastAprilFoolsJokeShownYear
            = addPrimitiveNullable("misc.lastAprilFoolsJokeShownYear", INTEGER_SERIALIZER);

        public final UnlockedAchievements unlockedAchievements
            = addValue("unlockedAchievements", new UnlockedAchievements());
    }


    public final PrimitiveValueNullable<String> lastUsedModVersion
        = root.addPrimitiveNullable("lastUsedModVersion", STRING_SERIALIZER);

    public final PrimitiveValueNullable<Float> avgMoneyCalcScathaPriceRare
        = root.addPrimitiveNullable("averageMoneyCalculator.scathaPriceRare", FLOAT_SERIALIZER);
    public final PrimitiveValueNullable<Float> avgMoneyCalcScathaPriceEpic
        = root.addPrimitiveNullable("averageMoneyCalculator.scathaPriceEpic", FLOAT_SERIALIZER);
    public final PrimitiveValueNullable<Float> avgMoneyCalcScathaPriceLegendary
        = root.addPrimitiveNullable("averageMoneyCalculator.scathaPriceLegendary", FLOAT_SERIALIZER);


    private static final JsonValue.Serializer<LocalDate, JsonPrimitive> DATE_SERIALIZER = new JsonValue.Serializer<>()
    {
        @Override
        public @Nullable LocalDate jsonToValue(@NonNull JsonPrimitive jsonPrimitive)
        {
            if (jsonPrimitive.isString())
            {
                try
                {
                    return LocalDate.parse(jsonPrimitive.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
                }
                catch (DateTimeParseException ignored) {}
            }
            return null;
        }

        @Override
        public @NonNull JsonPrimitive valueToJson(@NonNull LocalDate value)
        {
            return new JsonPrimitive(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
    };

    @NullMarked
    public static final class MagicFindSourceValue<T extends Enum<T> & MagicFindSource> implements JsonValue
    {
        private final JsonFile.EnumSerializer<T> valueSerializer;
        private final boolean hasLevels;
        private final Map<T, Integer> unlockedValues = new HashMap<>();
        private float magicFind = -1;

        public MagicFindSourceValue(Class<T> type, boolean hasLevels)
        {
            valueSerializer = new EnumSerializer<>(type);
            this.hasLevels = hasLevels;
        }

        public float getMagicFind()
        {
            return magicFind;
        }

        private void updateMagicFind()
        {
            if (!hasValue())
            {
                magicFind = -1;
                return;
            }

            magicFind = 0;
            for (Map.Entry<T, Integer> entry : unlockedValues.entrySet())
            {
                if (hasLevels)
                {
                    if (entry.getValue() < 1) continue;
                    magicFind += entry.getKey().getMagicFind(entry.getValue());
                }
                else magicFind += entry.getKey().getMagicFind(1);
            }
        }

        public void setUnlocked(T value)
        {
            if (hasLevels) throw new IllegalStateException("Cannot set value as unlocked without setting a level for a magic find source that has levels");
            unlockedValues.put(value, -1);
            updateMagicFind();
        }

        public void setUnlocked(T value, int level)
        {
            if (!hasLevels) throw new IllegalStateException("Cannot set a level for a magic find source that has no levels");
            if (level < 1) throw new IllegalArgumentException("Magic find source level cannot be less than 1");
            int currentLevel = unlockedValues.getOrDefault(value, -1);
            if (level <= currentLevel) return;
            unlockedValues.put(value, level);
            updateMagicFind();
        }

        public void removeUnlocked(T value)
        {
            unlockedValues.remove(value);
            updateMagicFind();
        }

        public @Nullable Integer getLevel(T value)
        {
            return unlockedValues.get(value);
        }

        @Override
        public void reset()
        {
            unlockedValues.clear();
            updateMagicFind();
        }

        @Override
        public boolean hasValue()
        {
            return !unlockedValues.isEmpty();
        }

        @Override
        public void loadFromJson(@Nullable JsonElement jsonElement)
        {
            unlockedValues.clear();

            if (jsonElement instanceof JsonArray jsonArray)
            {
                for (JsonElement arrayElement : jsonArray)
                {
                    if (hasLevels)
                    {
                        if (arrayElement instanceof JsonObject jsonObject)
                        {
                            JsonPrimitive valuePrimitive = JsonUtil.getJsonPrimitive(jsonObject, "type");
                            if (valuePrimitive == null) continue;
                            T value = valueSerializer.jsonToValue(valuePrimitive);
                            if (value == null) continue;
                            Integer level = JsonUtil.getInt(jsonObject, "level");
                            if (level == null) continue;
                            unlockedValues.put(value, level);
                        }
                    }
                    else
                    {
                        if (arrayElement instanceof JsonPrimitive jsonPrimitive)
                        {
                            T value = valueSerializer.jsonToValue(jsonPrimitive);
                            if (value != null) unlockedValues.put(value, -1);
                        }
                    }
                }
            }

            updateMagicFind();
        }

        @Override
        public JsonElement getAsJson(JsonFile<?> jsonFile)
        {
            if (!hasValue()) return JsonNull.INSTANCE;

            JsonArray jsonArray = new JsonArray();
            for (Map.Entry<T, Integer> entry : unlockedValues.entrySet())
            {
                if (hasLevels)
                {
                    JsonObject object = new JsonObject();
                    object.add("type", valueSerializer.valueToJson(entry.getKey()));
                    object.add("level", new JsonPrimitive(entry.getValue()));
                    jsonArray.add(object);
                }
                else jsonArray.add(valueSerializer.valueToJson(entry.getKey()));
            }
            return jsonArray;
        }
    }
}