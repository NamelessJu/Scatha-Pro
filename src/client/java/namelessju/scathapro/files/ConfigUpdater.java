package namelessju.scathapro.files;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.BackupManager;
import namelessju.scathapro.miscellaneous.data.enums.ChatPrefixType;
import namelessju.scathapro.util.JsonUtil;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

public class ConfigUpdater implements BiConsumer<@Nullable JsonElement, Config>
{
    public static final int TARGET_VERSION = 5;

    private final ScathaPro scathaPro;

    public ConfigUpdater(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }

    @Override
    public void accept(@Nullable JsonElement jsonElement, Config config)
    {
        if (!(jsonElement instanceof JsonObject jsonObject)) return;

        int currentVersion = config.version.getOr(-1);
        if (currentVersion >= TARGET_VERSION) return;

        if (currentVersion < 2)
        {
            Boolean shortChatPrefix = JsonUtil.getBoolean(jsonObject, "miscellaneous.shortChatPrefix");
            if (shortChatPrefix != null) config.miscellaneous.chatPrefixType.set(
                shortChatPrefix ? ChatPrefixType.ACRONYM_BRACKETS : ChatPrefixType.FULL_NAME_BRACKETS
            );
        }
        if (currentVersion < 3)
        {
            Boolean overlayBackgroundEnabled = JsonUtil.getBoolean(jsonObject, "overlay.backgroundEnabled");
            if (overlayBackgroundEnabled != null) config.overlay.backgroundOpacity.set(overlayBackgroundEnabled ? null : 0f);
        }
        if (currentVersion < 4)
        {
            Integer gamblingReelDurationOld = JsonUtil.getInt(jsonObject, "miscellaneous.scathaDropsSlotMachine.animationDurationTicks");
            if (gamblingReelDurationOld != null)
            {
                config.miscellaneous.dropsGamblingReelAnimationTicksMin.set(gamblingReelDurationOld);
                config.miscellaneous.dropsGamblingReelAnimationTicksMax.set(gamblingReelDurationOld);
            }
        }


        Boolean automaticPetDropScreenshotEnabled = JsonUtil.getBoolean(jsonObject, "miscellaneous.automaticPetDropScreenshot");
        if (automaticPetDropScreenshotEnabled != null) config.petDrop.automaticScreenshotEnabled.set(automaticPetDropScreenshotEnabled);

        if (scathaPro.backupManager.getBackupFiles(BackupManager.BackupType.FULL, null).length > config.miscellaneous.fullBackupsLimit.get())
        {
            config.miscellaneous.fullBackupsLimit.set(0);
        }


        // NO SAVING, gets called before backups are made
        // (doesn't need saving anyways because config is always saved after loading)
    }
}