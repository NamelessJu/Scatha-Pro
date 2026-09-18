package namelessju.scathapro.miscellaneous.data;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.Nullable;

public record UnconnectedProfileDataImport(PersistentData.PlayerData playerData,
                                           PersistentData.ProfileData unconnectedProfileData,
                                           PersistentData.ProfileData activeProfileData)
{
    public static @Nullable UnconnectedProfileDataImport tryMake(ScathaPro scathaPro)
    {
        PersistentData.ProfileData currentProfileData = scathaPro.persistentDataProfileManager.currentProfileData();
        if (currentProfileData.isDummy()) return null;

        PersistentData.PlayerData currentPlayerData = scathaPro.persistentDataProfileManager.currentPlayerData();
        for (PersistentData.ProfileData profileData : currentPlayerData.profiles)
        {
            if (profileData.profileID.get() == null && profileData.hypixelEnvironment.get() == null)
                return new UnconnectedProfileDataImport(currentPlayerData, profileData, currentProfileData);
        }
        return null;
    }

    public Component getDataPreview()
    {
        PersistentData.ProfileData data = unconnectedProfileData;
        MutableComponent component = Component.empty()
            .append("Scatha kills: " + data.scathaKills.get() + "\n")
            .append("Stoneworm kills: " + data.stonewormKills.get() + "\n")
            .append(
                Component.literal("Pet Drops: ")
                    .append(UnicodeSymbol.scathaPetRare + " ")
                    .append(Component.literal(data.rarePetDrops.get() + " ")
                        .withColor(TextColor.BLUE))
                    .append(UnicodeSymbol.scathaPetEpic + " ")
                    .append(Component.literal(data.epicPetDrops.get() + " ")
                        .withColor(TextColor.DARK_PURPLE))
                    .append(UnicodeSymbol.scathaPetLegendary + " ")
                    .append(Component.literal(String.valueOf(data.legendaryPetDrops.get()))
                        .withColor(TextColor.GOLD))
            );
        if (data.scappaModeUnlocked.get()) component.append("\nScappa mode unlocked");
        return component;
    }

    public void handleImport(ScathaPro scathaPro)
    {
        playerData.profiles.remove(activeProfileData);
        unconnectedProfileData.profileID.set(activeProfileData.profileID.get());
        unconnectedProfileData.hypixelEnvironment.set(activeProfileData.hypixelEnvironment.get());
        scathaPro.persistentData.save();
        scathaPro.persistentDataProfileManager.forceUpdate();

        scathaPro.chatManager.sendChatMessage(
            Component.literal("Profile data updated!").withColor(TextColor.GREEN)
        );
    }

    public void handleDialogShown(ScathaPro scathaPro)
    {
        if (activeProfileData.unconnectedDataImportShown.get()) return;
        activeProfileData.unconnectedDataImportShown.set(true);
        scathaPro.persistentData.save();

        scathaPro.chatManager.sendChatMessage(
            Component.literal(
                "Profile data import rejected. You will not be notified about this again on the current profile, "
                + "but you can still choose to import the old data using \"/sp updateProfileData\"!"
            ).withColor(TextColor.GRAY)
        );
    }
}