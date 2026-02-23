package namelessju.scathapro.files;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.achievements.UnlockedAchievement;
import namelessju.scathapro.achievements.UnlockedAchievements;
import namelessju.scathapro.files.framework.JsonFile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class PersistentData extends JsonFile
{
    public PersistentData(ScathaPro scathaPro)
    {
        super(scathaPro, Path.of("persistentDataV2.json"), false);
    }
    
    
    public final JsonValueArrayValue<PlayerData> players = root.addValue("players", new JsonValueArrayValue<>(() -> new PlayerData(null)));
    public final static class PlayerData extends ObjectValue
    {
        public PlayerData(UUID playerUUID)
        {
            this.playerUUID.set(playerUUID);
        }
        
        public final PrimitiveValueNullable<UUID> playerUUID = addPrimitiveNullable("playerUUID", new Serializer<>()
        {
            @Override
            public @Nullable UUID jsonToValue(@NonNull JsonPrimitive jsonPrimitive)
            {
                if (jsonPrimitive.isString())
                {
                    String uuidString = jsonPrimitive.getAsString();
                    if (uuidString.length() == 32)
                    {
                        uuidString = uuidString.substring(0, 8) + "-" + uuidString.substring(8, 12) + "-" + uuidString.substring(12, 16)
                            + "-" + uuidString.substring(16, 20) + "-" + uuidString.substring(20, 32);
                    }
                    try
                    {
                        return UUID.fromString(uuidString);
                    }
                    catch (IllegalArgumentException ignored) {}
                }
                return null;
            }
            
            @Override
            public @NonNull JsonPrimitive valueToJson(@NonNull UUID value)
            {
                return new JsonPrimitive(value.toString());
            }
        });
        
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
        
        public final PrimitiveValueNullable<Integer> scathaKillsAtLastDrop
            = addPrimitiveNullable("dryStreak.scathaKillsAtLastDrop", INTEGER_SERIALIZER);
        public final BooleanValue isPetDropDryStreakInvalidated
            = addValue("dryStreak.isDryStreakInvalidated", new BooleanValue(false));
        
        public final PrimitiveValueNullable<LocalDate> lastPlayedDate = addPrimitiveNullable("realTime.lastPlayedDate", DATE_SERIALIZER);
        public final WormStatsToday wormStatsToday = addValue("realTime.wormStatsToday", new WormStatsToday());
        public static class WormStatsToday extends ObjectValue
        {
            private WormStatsToday() {}
            public final PrimitiveValueWithDefault<Integer> regularWormKills
                = addPrimitiveWithDefault("wormKills.regularWorms", INTEGER_SERIALIZER, 0);
            public final PrimitiveValueWithDefault<Integer> scathaKills
                = addPrimitiveWithDefault("wormKills.scathas", INTEGER_SERIALIZER, 0);
            public final PrimitiveValueWithDefault<Integer> scathaSpawnStreak
                = addPrimitiveWithDefault("scathaSpawnStreak", INTEGER_SERIALIZER, 0);
        }
        public final PrimitiveValueNullable<LocalDate> lastScathaFarmedDate
            = addPrimitiveNullable("realTime.lastScathaFarmedDate", DATE_SERIALIZER);
        public final PrimitiveValueWithDefault<Integer> scathaFarmingStreak
            = addPrimitiveWithDefault("realTime.scathaFarmingStreak.current", INTEGER_SERIALIZER, 0);
        public final PrimitiveValueWithDefault<Integer> scathaFarmingStreakHighscore
            = addPrimitiveWithDefault("realTime.scathaFarmingStreak.highscore", INTEGER_SERIALIZER, 0);
        
        public final PrimitiveValueNullable<Float> magicFind
            = addPrimitiveNullable("profileStats.magicFind", FLOAT_SERIALIZER);
        public final PrimitiveValueNullable<Float> wormBestiaryMagicFind
            = addPrimitiveNullable("profileStats.wormBestiaryMagicFind", FLOAT_SERIALIZER);
        public final PrimitiveValueNullable<Float> petLuck
            = addPrimitiveNullable("profileStats.petLuck", FLOAT_SERIALIZER);
        
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
    
}
