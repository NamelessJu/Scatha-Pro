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
    }
    
    private void detectCheater()
    {
        // TODO: might not trigger correctly if profile was switched
        //  -> only a problem if can only trigger on first level join
        cheaterDetected = false;
        
        PersistentData.ProfileData profileData = getCurrentProfileData();
        long now = TimeUtil.now();
        
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
    
    
    public float getTotalMagicFind()
    {
        float totalMagicFind = -1f;;
        float magicFind = getCurrentProfileData().magicFind.getOr(-1f);
        if (magicFind >= 0) totalMagicFind = magicFind;
        float wormBestiaryMagicFind = getCurrentProfileData().wormBestiaryMagicFind.getOr(-1f);
        if (wormBestiaryMagicFind >= 0)
        {
            if (totalMagicFind >= 0) totalMagicFind += wormBestiaryMagicFind;
            else totalMagicFind = wormBestiaryMagicFind;
        }
        return totalMagicFind;
    }
    
    public float getEffectiveMagicFind()
    {
        float totalMagicFind = getTotalMagicFind();
        float petLuck = getCurrentProfileData().petLuck.getOr(-1f);
        return totalMagicFind >= 0f && petLuck >= 0f ? totalMagicFind + petLuck : -1f;
    }
    
    public MutableComponent getMagicFindComponent(boolean addSymbol)
    {
        MutableComponent component = Component.empty().withStyle(ChatFormatting.AQUA);
        if (addSymbol) component.append(UnicodeSymbol.magicFind + " ");
        return component.append(TextUtil.numberToComponentOrObf(
            getCurrentProfileData().magicFind.getOr(-1f),
            2, false, RoundingMode.FLOOR)
        );
    }
    
    public MutableComponent getBestiaryMagicFindString(boolean addSymbol)
    {
        MutableComponent component = Component.empty().withStyle(ChatFormatting.AQUA);
        if (addSymbol) component.append(UnicodeSymbol.magicFind + " ");
        return component.append(TextUtil.numberToComponentOrObf(
            getCurrentProfileData().wormBestiaryMagicFind.getOr(-1f),
            2, false, RoundingMode.FLOOR)
        );
    }
    
    public MutableComponent getTotalMagicFindComponent(boolean addSymbol)
    {
        MutableComponent component = Component.empty().withStyle(ChatFormatting.AQUA);
        if (addSymbol) component.append(UnicodeSymbol.magicFind + " ");
        return component.append(TextUtil.numberToComponentOrObf(
            getTotalMagicFind(), 2, false, RoundingMode.FLOOR)
        );
    }
    
    public MutableComponent getPetLuckComponent(boolean addSymbol)
    {
        MutableComponent component = Component.empty().withStyle(ChatFormatting.LIGHT_PURPLE);
        if (addSymbol) component.append(UnicodeSymbol.petLuck + " ");
        return component.append(TextUtil.numberToComponentOrObf(
            getCurrentProfileData().petLuck.getOr(-1f),
            2, false, RoundingMode.FLOOR)
        );
    }
    
    public MutableComponent getEffectiveMagicFindComponent()
    {
        return Component.empty().withStyle(ChatFormatting.BLUE).append(
            TextUtil.numberToComponentOrObf(getEffectiveMagicFind(), 2, false, RoundingMode.FLOOR)
        );
    }
}
