package namelessju.scathapro.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.UnlockedAchievements;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.files.framework.ObjectRootJsonFile;
import namelessju.scathapro.miscellaneous.data.enums.WitchesStew;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;
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

        public final WitchesStewsEatenValue witchesStewsEaten
            = addValue("profileStats.witchesStewsEaten", new WitchesStewsEatenValue());

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

    public static final class WitchesStewsEatenValue implements JsonValue
    {
        private static final JsonFile.EnumSerializer<WitchesStew> valueSerializer = new EnumSerializer<>(WitchesStew.class);

        private final Set<WitchesStew> unlockedStews = new HashSet<>();
        private int magicFind = -1;

        public void setEaten(WitchesStew stew, boolean eaten)
        {
            if (eaten) unlockedStews.add(stew);
            else unlockedStews.remove(stew);
            updateMagicFind();
        }

        public int getMagicFind()
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

            magicFind = unlockedStews.size();
        }

        @Override
        public void reset()
        {
            unlockedStews.clear();
            updateMagicFind();
        }

        @Override
        public boolean hasValue()
        {
            return !unlockedStews.isEmpty();
        }

        @Override
        public void loadFromJson(@Nullable JsonElement jsonElement)
        {
            unlockedStews.clear();

            if (jsonElement instanceof JsonArray jsonArray)
            {
                for (JsonElement arrayElement : jsonArray)
                {
                    if (arrayElement instanceof JsonPrimitive jsonPrimitive)
                    {
                        WitchesStew stew = valueSerializer.jsonToValue(jsonPrimitive);
                        if (stew != null) unlockedStews.add(stew);
                    }
                }
            }

            updateMagicFind();
        }

        @Override
        public @NonNull JsonElement getAsJson(@NonNull JsonFile<?> jsonFile)
        {
            if (!hasValue()) return JsonNull.INSTANCE;

            JsonArray jsonArray = new JsonArray();
            for (WitchesStew stew : unlockedStews)
            {
                jsonArray.add(valueSerializer.valueToJson(stew));
            }
            return jsonArray;
        }
    }
}