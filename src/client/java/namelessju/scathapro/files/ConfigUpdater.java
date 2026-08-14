package namelessju.scathapro.files;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import namelessju.scathapro.miscellaneous.data.enums.ChatPrefixType;
import namelessju.scathapro.util.JsonUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class ConfigUpdater implements Consumer<@Nullable JsonElement>
{
    public static final int TARGET_VERSION = 2;

    private final @NonNull Config config;

    public ConfigUpdater(@NonNull Config config)
    {
        this.config = config;
    }

    @Override
    public void accept(@Nullable JsonElement jsonElement)
    {
        if (!(jsonElement instanceof JsonObject jsonObject)) return;

        int currentVersion = config.version.getOr(-1);
        if (currentVersion >= TARGET_VERSION) return;

        // if (currentVersion < 2) - not necessary for update step to TARGET_VERSION because of first if
        updateToV2(jsonObject, config);

        // doesn't need saving because config is always saved after loading
    }

    private void updateToV2(@NonNull JsonObject jsonObject, @NonNull Config config)
    {
        Boolean shortChatPrefix = JsonUtil.getBoolean(jsonObject, "miscellaneous.shortChatPrefix");
        if (shortChatPrefix != null) config.miscellaneous.chatPrefixType.set(
            shortChatPrefix ? ChatPrefixType.ACRONYM_BRACKETS : ChatPrefixType.FULL_NAME_BRACKETS
        );
    }
}