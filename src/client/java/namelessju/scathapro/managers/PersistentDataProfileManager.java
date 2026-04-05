package namelessju.scathapro.managers;

import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.achievements.UnlockedAchievement;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

public class PersistentDataProfileManager
{
    private final ScathaPro scathaPro;
    
    private PersistentData.@NonNull ProfileData currentProfileData = new PersistentData.ProfileData(null);
    private UUID currentPlayerUUID = null;
    private boolean cheaterDetected = false;
    
    public PersistentDataProfileManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public void init()
    {
        updateCurrentPlayerProfile();
    }
    
    public PersistentData.@NonNull ProfileData getCurrentProfileData()
    {
        return currentProfileData;
    }
    
    public void updateCurrentPlayerProfile()
    {
        UUID playerUUID = scathaPro.minecraft.getUser().getProfileId();
        String profileID = null; // TODO: actual multiple profiles support
        //noinspection ConstantValue
        if (Objects.equals(playerUUID, currentPlayerUUID)
            && Objects.equals(profileID, currentProfileData.profileID.get())) return;
        
        currentPlayerUUID = playerUUID;
        
        // Note: this mustn't save the persistent data as
        // this gets run before the backup might be made
        
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
            ScathaPro.LOGGER.debug("No matching player data found, appending new instance");
        }
        
        PersistentData.ProfileData profileData = null;
        for (PersistentData.ProfileData profileDataEntry : playerData.profiles)
        {
            if (Objects.equals(profileDataEntry.profileID.get(), profileID))
            {
                ScathaPro.LOGGER.debug("Profile data with ID {} found", profileID);
                profileData = profileDataEntry;
                break;
            }
        }
        if (profileData == null)
        {
            profileData = new PersistentData.ProfileData(profileID);
            playerData.profiles.add(profileData);
            ScathaPro.LOGGER.debug("No matching profile data found, appending new instance");
        }
        
        currentProfileData = profileData;
        
        detectCheater();
        
        scathaPro.achievementLogicManager.updateAchievementsAfterDataLoading();
    }
    
    private void detectCheater()
    {
        // TODO: might not trigger correctly if profile was switched
        //  -> only a problem if can only trigger on first level join
        cheaterDetected = false;
        
        PersistentData.ProfileData profileData = getCurrentProfileData();
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
        PersistentData.ProfileData profileData = getCurrentProfileData();
        float totalMagicFind = -1f;
        
        totalMagicFind = tryAddStatValue(totalMagicFind, profileData.globalMagicFind.getOr(-1f));
        totalMagicFind = tryAddStatValue(totalMagicFind, profileData.wormBestiaryMagicFind.getOr(-1f));
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
        float petLuck = getCurrentProfileData().petLuck.getOr(-1f);
        return totalMagicFind >= 0f && petLuck >= 0f ? totalMagicFind + petLuck : -1f;
    }
    
    public MutableComponent getGlobalMagicFindComponent(boolean addSymbol)
    {
        return getStatComponent(
            getCurrentProfileData().globalMagicFind.getOr(-1f),
            ChatFormatting.AQUA, String.valueOf(UnicodeSymbol.magicFind),
            addSymbol
        );
    }
    
    public MutableComponent getBestiaryMagicFindComponent(boolean addSymbol)
    {
        return getStatComponent(
            getCurrentProfileData().wormBestiaryMagicFind.getOr(-1f),
            ChatFormatting.AQUA, String.valueOf(UnicodeSymbol.magicFind),
            addSymbol
        );
    }
    
    public MutableComponent getWitchesStewMagicFindComponent(boolean addSymbol)
    {
        return getStatComponent(
            getCurrentProfileData().witchesStewsEaten.getMagicFind(),
            ChatFormatting.AQUA, String.valueOf(UnicodeSymbol.magicFind),
            addSymbol
        );
    }
    
    public MutableComponent getTotalMagicFindComponent(boolean allowShuriken, boolean addSymbol)
    {
        return getStatComponent(
            getTotalMagicFind(allowShuriken),
            ChatFormatting.AQUA, String.valueOf(UnicodeSymbol.magicFind),
            addSymbol
        );
    }
    
    public MutableComponent getPetLuckComponent(boolean addSymbol)
    {
        return getStatComponent(
            getCurrentProfileData().petLuck.getOr(-1f),
            ChatFormatting.LIGHT_PURPLE, String.valueOf(UnicodeSymbol.petLuck),
            addSymbol
        );
    }
    
    private MutableComponent getStatComponent(float value, ChatFormatting color, String symbol, boolean addSymbol)
    {
        MutableComponent component = Component.empty().withStyle(color);
        if (addSymbol) component.append(symbol + " ");
        return component.append(TextUtil.numberToComponentOrObf(
            value, 2, false, RoundingMode.HALF_UP)
        );
    }
    
    public MutableComponent getEffectiveMagicFindComponent(boolean allowShuriken)
    {
        return Component.empty().withStyle(ChatFormatting.BLUE).append(
            TextUtil.numberToComponentOrObf(getEffectiveMagicFind(allowShuriken), 2, false, RoundingMode.HALF_UP)
        );
    }
}
