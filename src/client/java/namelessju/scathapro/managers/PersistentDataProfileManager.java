package namelessju.scathapro.managers;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.UnlockedAchievement;
import namelessju.scathapro.events.framework.DataEvent;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.miscellaneous.data.enums.HypixelEnvironment;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

public class PersistentDataProfileManager
{
    private final ScathaPro scathaPro;

    private PersistentData.@NonNull PlayerData currentPlayerData = new PersistentData.PlayerData(null);
    private PersistentData.@NonNull ProfileData currentProfileData = new PersistentData.ProfileData(null, null);
    private @Nullable UUID currentPlayerUUID = null;
    private @Nullable HypixelEnvironment lastHypixelEnvironment = null;
    private @Nullable UUID lastProfileId = null;
    private boolean cheaterDetected = false;

    private final DataEvent<EventData> onProfileChangedEvent = new DataEvent<>();

    public PersistentDataProfileManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }

    public void init()
    {
        scathaPro.hypixelContextManager.contextChangedEvent.addListener(
            _ -> updateCurrentPlayerProfile(true)
        );

        // This is run before automatic backups are potentially made
        // -> do not allow persistent data saving
        updateCurrentPlayerProfile(false);
    }

    public PersistentData.@NonNull PlayerData currentPlayerData()
    {
        return currentPlayerData;
    }

    public PersistentData.@NonNull ProfileData currentProfileData()
    {
        return currentProfileData;
    }

    public void updateCurrentPlayerProfile(boolean allowPersistentDataSaving)
    {
        UUID playerUUID = scathaPro.minecraft.getUser().getProfileId();
        HypixelEnvironment hypixelEnvironment = scathaPro.hypixelContextManager.environment();
        UUID profileID = scathaPro.hypixelContextManager.profileId();

        boolean playerChanged = !Objects.equals(playerUUID, currentPlayerUUID);
        // Note: profile changes to null every time a level is left
        boolean profileChanged = !Objects.equals(profileID, currentProfileData.profileID.get());
        if (!playerChanged
            && Objects.equals(hypixelEnvironment, currentProfileData.hypixelEnvironment.get())
            && !profileChanged) return;

        boolean shouldSavePersistentData = false;

        // Load player data
        PersistentData.PlayerData playerData = null;
        for (PersistentData.PlayerData playerDataEntry : scathaPro.persistentData.players)
        {
            if (playerUUID.equals(playerDataEntry.playerUUID.get()))
            {
                ScathaPro.LOGGER.debug("Player data with UUID {} found", playerUUID);
                playerData = playerDataEntry;
                break;
            }
        }
        if (playerData == null)
        {
            playerData = new PersistentData.PlayerData(playerUUID);
            scathaPro.persistentData.players.add(playerData);
            shouldSavePersistentData = true;
            ScathaPro.LOGGER.debug("No matching player data found, appended new instance");
        }


        // Cache & save last used profile
        if (profileChanged && hypixelEnvironment != null && profileID != null)
        {
            if (hypixelEnvironment == HypixelEnvironment.PRODUCTION)
            {
                currentPlayerData.lastUsedProdProfileId.set(profileID);
                shouldSavePersistentData = true;
                ScathaPro.LOGGER.debug("Saved last used production profile ID ({})", profileID);
            }
            lastHypixelEnvironment = hypixelEnvironment;
            lastProfileId = profileID;
            ScathaPro.LOGGER.debug("Cached last used profile ({}) & environment ({})", profileID, hypixelEnvironment);
        }

        // Load last used PROD profile
        if ((playerChanged || profileChanged && profileID == null) && playerData.lastUsedProdProfileId.get() != null)
        {
            lastHypixelEnvironment = HypixelEnvironment.PRODUCTION;
            lastProfileId = playerData.lastUsedProdProfileId.get();
            ScathaPro.LOGGER.debug("Loaded last used production profile ID ({})", lastProfileId);
        }

        // Fall back to last used profile if none is active
        if (hypixelEnvironment == null && lastHypixelEnvironment != null)
        {
            hypixelEnvironment = lastHypixelEnvironment;
            ScathaPro.LOGGER.debug("Fell back to last used Hypixel environment ({})", hypixelEnvironment);
        }
        if (profileID == null && lastProfileId != null)
        {
            profileID = lastProfileId;
            ScathaPro.LOGGER.debug("Fell back to last used profile ID ({})", profileID);
        }


        // Load profile data
        PersistentData.ProfileData profileData = null;
        if (hypixelEnvironment == null || profileID == null)
        {
            profileData = new PersistentData.ProfileData(null, null);
            ScathaPro.LOGGER.debug("Hypixel environment (= {}) or SkyBlock profile ID (= {}) is null, using dummy ProfileData", hypixelEnvironment, profileID);
        }
        else
        {
            for (PersistentData.ProfileData profileDataEntry : playerData.profiles)
            {
                if (Objects.equals(profileDataEntry.profileID.get(), profileID)
                    && Objects.equals(profileDataEntry.hypixelEnvironment.get(), hypixelEnvironment))
                {
                    ScathaPro.LOGGER.debug("Profile data with ID {} and environment {} found", profileID, hypixelEnvironment);
                    profileData = profileDataEntry;
                    break;
                }
            }
            if (profileData == null)
            {
                profileData = new PersistentData.ProfileData(profileID, hypixelEnvironment);
                playerData.profiles.add(profileData);
                shouldSavePersistentData = true;
                ScathaPro.LOGGER.debug("No matching profile data found, appended new instance with profile ID {} and environment {}", profileID, hypixelEnvironment);
            }
        }

        // Clear events on previous data
        currentPlayerData.visit((_, value) -> {
            if (value instanceof JsonFile.ValueEvents valueEvents)
            {
                valueEvents.clearAllListeners();
            }
        });

        currentPlayerUUID = playerUUID;
        currentPlayerData = playerData;
        currentProfileData = profileData;

        ScathaPro.LOGGER.debug(
            "Updated profile data to: player {}, Hypixel environment {}, profile {}",
            playerUUID, profileData.hypixelEnvironment.get(), profileData.profileID.get()
        );

        detectCheater();

        if (shouldSavePersistentData && allowPersistentDataSaving)
        {
            scathaPro.persistentData.save();
        }

        onProfileChangedEvent.trigger(new EventData(scathaPro, currentProfileData));
    }

    public void forceUpdate()
    {
        currentPlayerUUID = null;
        currentPlayerData = new PersistentData.PlayerData(null);
        currentProfileData = new PersistentData.ProfileData(null, null);
        updateCurrentPlayerProfile(true);
    }

    private void detectCheater()
    {
        cheaterDetected = false;

        PersistentData.ProfileData profileData = currentProfileData();
        long now = TimeUtil.getEpochMilliseconds();

        if (
            profileData.rarePetDrops.get() > Constants.maxLegitPetDropsAmount || profileData.rarePetDrops.get() < 0
            || profileData.epicPetDrops.get() > Constants.maxLegitPetDropsAmount || profileData.epicPetDrops.get() < 0
            || profileData.legendaryPetDrops.get() > Constants.maxLegitPetDropsAmount || profileData.legendaryPetDrops.get() < 0
        ) {
            cheaterDetected = true;
            return;
        }

        for (UnlockedAchievement unlockedAchievement : profileData.unlockedAchievements.getAll())
        {
            if (unlockedAchievement.unlockTimestamp > now
                || (unlockedAchievement.unlockTimestamp != -1L && unlockedAchievement.unlockTimestamp < 1640991600000L))
            {
                cheaterDetected = true;
                return;
            }
            if (unlockedAchievement.getRepeatCount() < 0)
            {
                cheaterDetected = true;
                return;
            }
        }

        int lastAprilFoolsJokeShownYear = profileData.lastAprilFoolsJokeShownYear.getOr(-1);
        if (lastAprilFoolsJokeShownYear >= 0 && (lastAprilFoolsJokeShownYear <= 2024 || lastAprilFoolsJokeShownYear >= 3000)
            || lastAprilFoolsJokeShownYear < -1)
        {
            cheaterDetected = true;
        }
    }

    public boolean isProfileDataCheated()
    {
        return cheaterDetected;
    }


    public float getTotalMagicFind(boolean allowShuriken)
    {
        PersistentData.ProfileData profileData = currentProfileData();
        float totalMagicFind = -1f;

        totalMagicFind = tryAddStatValue(totalMagicFind, profileData.globalMagicFind.getOr(-1f));
        totalMagicFind = tryAddStatValue(totalMagicFind, profileData.wormBestiaryMagicFind.getOr(-1f));
        totalMagicFind = tryAddStatValue(totalMagicFind, profileData.attributes.getMagicFind());
        totalMagicFind = tryAddStatValue(totalMagicFind, profileData.witchesStewsEaten.getMagicFind());

        if (allowShuriken && scathaPro.coreManager.lastScathaHitHadShuriken)
        {
            totalMagicFind = Constants.applyShurikenMagicFind(totalMagicFind);
        }

        return totalMagicFind;
    }

    private float tryAddStatValue(float current, float value)
    {
        if (value < 0f) return current;

        if (current >= 0) current += value;
        else current = value;
        return current;
    }

    public float getEffectiveMagicFind(boolean allowShuriken)
    {
        float totalMagicFind = getTotalMagicFind(allowShuriken);
        float petLuck = currentProfileData().petLuck.getOr(-1f);
        return totalMagicFind >= 0f && petLuck >= 0f ? totalMagicFind + petLuck : -1f;
    }

    public MutableComponent getGlobalMagicFindComponent(boolean addSymbol)
    {
        return getStatComponent(
            currentProfileData().globalMagicFind.getOr(-1f),
            TextColor.AQUA, String.valueOf(UnicodeSymbol.magicFind),
            addSymbol
        );
    }

    public MutableComponent getBestiaryMagicFindComponent(boolean addSymbol)
    {
        return getStatComponent(
            currentProfileData().wormBestiaryMagicFind.getOr(-1f),
            TextColor.AQUA, String.valueOf(UnicodeSymbol.magicFind),
            addSymbol
        );
    }

    public MutableComponent getWitchesStewMagicFindComponent(boolean addSymbol)
    {
        return getStatComponent(
            currentProfileData().witchesStewsEaten.getMagicFind(),
            TextColor.AQUA, String.valueOf(UnicodeSymbol.magicFind),
            addSymbol
        );
    }

    public MutableComponent getAttributesMagicFindComponent(boolean addSymbol)
    {
        return getStatComponent(
            currentProfileData().attributes.getMagicFind(),
            TextColor.AQUA, String.valueOf(UnicodeSymbol.magicFind),
            addSymbol
        );
    }

    public MutableComponent getTotalMagicFindComponent(boolean allowShuriken, boolean addSymbol)
    {
        return getStatComponent(
            getTotalMagicFind(allowShuriken),
            TextColor.AQUA, String.valueOf(UnicodeSymbol.magicFind),
            addSymbol
        );
    }

    public MutableComponent getPetLuckComponent(boolean addSymbol)
    {
        return getStatComponent(
            currentProfileData().petLuck.getOr(-1f),
            TextColor.LIGHT_PURPLE, String.valueOf(UnicodeSymbol.petLuck),
            addSymbol
        );
    }

    private MutableComponent getStatComponent(float value, TextColor color, String symbol, boolean addSymbol)
    {
        MutableComponent component = Component.empty()
            //? if >= 26.2 {
            .withColor(color);
            //? } else {
            /*.withStyle(color);
            *///? }
        if (addSymbol) component.append(symbol + " ");
        return component.append(TextUtil.numberToComponentOrObf(
            value, 2, false, RoundingMode.HALF_UP)
        );
    }

    public MutableComponent getEffectiveMagicFindComponent(boolean allowShuriken)
    {
        return Component.empty().withColor(TextColor.BLUE).append(
            TextUtil.numberToComponentOrObf(getEffectiveMagicFind(allowShuriken), 2, false, RoundingMode.HALF_UP)
        );
    }


    public void onProfileChanged(DataEvent.Listener<EventData> listener)
    {
        onProfileChangedEvent.addListener(listener);
    }

    public void removeOnProfileChanged(DataEvent.Listener<EventData> listener)
    {
        onProfileChangedEvent.removeListener(listener);
    }

    public record EventData(@NonNull ScathaPro scathaPro, PersistentData.@NonNull ProfileData profileData) {}
}