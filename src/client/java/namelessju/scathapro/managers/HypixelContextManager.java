package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.events.framework.DatalessEvent;
import namelessju.scathapro.miscellaneous.data.enums.HypixelEnvironment;
import namelessju.scathapro.miscellaneous.data.enums.SkyBlockArea;
import namelessju.scathapro.util.TextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class HypixelContextManager
{
    private static final HypixelEnvironment DEV_MODE_ENVIRONMENT = HypixelEnvironment.OTHER;
    private static final UUID DEV_MODE_PROFILE_ID = new UUID(0L, 0L);
    private static final SkyBlockArea DEV_MODE_SKYBLOCK_AREA = SkyBlockArea.CRYSTAL_HOLLOWS;

    private static final short PROFILE_ID_COMMAND_WAIT_TICKS = 9 * 20;
    private static final short NO_PROFILE_DETECTED_WAIT_TICKS = 15 * 20;


    public final DatalessEvent contextChangedEvent = new DatalessEvent();
    public final DatalessEvent profileDetectionFailedEvent = new DatalessEvent();

    private final ScathaPro scathaPro;

    private @Nullable HypixelEnvironment environment = null;
    private boolean isOnSkyBlock = false;
    private @Nullable SkyBlockArea skyBlockArea = null;
    private @Nullable UUID profileId;

    private short profileIdCommandTickTimer = 0;
    private byte profileIdSuggestMessagesToIgnore = 0;

    private short profileDetectionTimeoutTicksLeft = 0;
    private boolean profileDetectionFailed = false;

    public HypixelContextManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;

        ScathaProEvents.worldLeftEvent.addListener(_ -> {
            isOnSkyBlock = false;
            skyBlockArea = null;
            profileId = null;
            contextChangedEvent.trigger();
        });
        ScathaProEvents.serverLeftEvent.addListener(_ -> {
            environment = null;
            contextChangedEvent.trigger();
        });
    }

    public @Nullable HypixelEnvironment environment()
    {
        if (shouldUseDevModeProfile()) return DEV_MODE_ENVIRONMENT;
        return environment;
    }

    public @Nullable UUID profileId()
    {
        if (shouldUseDevModeProfile()) return DEV_MODE_PROFILE_ID;
        return profileId;
    }

    public boolean hasProfileDetectionFailed()
    {
        return profileDetectionFailed;
    }

    private boolean shouldUseDevModeProfile()
    {
        return profileId == null && environment == null && scathaPro.config.dev.devModeEnabled.get();
    }

    @SuppressWarnings("unused")
    public boolean isOnSkyBlock()
    {
        if (scathaPro.config.dev.devModeEnabled.get()) return true;

        return isOnSkyBlock;
    }

    /** Note: may be null even if the player is on SkyBlock, as not all areas get detected */
    public @Nullable SkyBlockArea skyBlockArea()
    {
        if (skyBlockArea == null && scathaPro.config.dev.devModeEnabled.get())
        {
            return DEV_MODE_SKYBLOCK_AREA;
        }

        return skyBlockArea;
    }

    public boolean isInCrystalHollows()
    {
        return skyBlockArea() == SkyBlockArea.CRYSTAL_HOLLOWS;
    }

    public void tick()
    {
        if (profileIdCommandTickTimer > 0)
        {
            profileIdCommandTickTimer --;
            if (profileIdCommandTickTimer == 0)
            {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null)
                {
                    player.connection.sendCommand("profileid");
                    profileIdSuggestMessagesToIgnore = 2;
                }
            }
        }

        if (profileDetectionTimeoutTicksLeft > 0)
        {
            profileDetectionTimeoutTicksLeft--;
            if (profileDetectionTimeoutTicksLeft == 0)
            {
                profileDetectionFailed = true;
                if (isInCrystalHollows())
                {
                    scathaPro.chatManager.sendChatErrorMessage("Failed to detect SkyBlock profile! Data from this lobby can not be saved.");
                }
                profileDetectionFailedEvent.trigger();
            }
        }
    }

    public boolean shouldCancelChatMessage(String message)
    {
        if (profileIdSuggestMessagesToIgnore > 0 && message.startsWith("CLICK THIS TO SUGGEST IT IN CHAT"))
        {
            profileIdSuggestMessagesToIgnore--;
            return true;
        }
        return false;
    }

    public boolean parseChatMessage(String message)
    {
        if (!message.startsWith("Profile ID: ")) return false;
        String profileIdString = message.substring(12).trim().split(" ", 2)[0];

        cancelProfileIdCommandTimer();

        UUID profileIdBefore = profileId;
        profileId = TextUtil.parseUUID(profileIdString);
        if (!Objects.equals(profileId, profileIdBefore))
        {
            ScathaPro.LOGGER.debug("Profile ID changed: {} -> {}", profileIdBefore, profileId);
            profileDetectionTimeoutTicksLeft = 0;
            contextChangedEvent.trigger();
        }

        return true;
    }

    public void setEnvironment(@Nullable HypixelEnvironment environment)
    {
        HypixelEnvironment previousEnvironment = this.environment;
        this.environment = environment;
        clearProfile();
        if (!Objects.equals(previousEnvironment, this.environment))
        {
            contextChangedEvent.trigger();
        }
    }

    public void setLocation(boolean isOnSkyBlock, @Nullable SkyBlockArea area)
    {
        this.isOnSkyBlock = isOnSkyBlock;
        this.skyBlockArea = area;
        clearProfile();

        if (isOnSkyBlock)
        {
            profileIdCommandTickTimer = PROFILE_ID_COMMAND_WAIT_TICKS;
            profileDetectionTimeoutTicksLeft = NO_PROFILE_DETECTED_WAIT_TICKS;
        }

        ScathaPro.LOGGER.debug("Hypixel location changed - is SkyBlock: {}, SkyBlock area: {}", isOnSkyBlock, area);
        contextChangedEvent.trigger();
    }

    public void cancelProfileIdCommandTimer()
    {
        profileIdCommandTickTimer = 0;
    }

    private void clearProfile()
    {
        profileId = null;
        profileDetectionFailed = false;
        profileIdCommandTickTimer = 0;
        profileDetectionTimeoutTicksLeft = 0;
    }
}