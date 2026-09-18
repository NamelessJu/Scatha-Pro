package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.events.ScathaProEvents;
import namelessju.scathapro.events.framework.DataEvent;
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


    public final DataEvent<ScathaPro> contextChangedEvent = new DataEvent<>();

    private final ScathaPro scathaPro;

    private @Nullable HypixelEnvironment environment = null;
    private boolean isOnSkyBlock = false;
    private @Nullable SkyBlockArea skyBlockArea = null;
    private @Nullable UUID profileId;

    private short profileIdCommandTickTimer = 0;
    private byte profileIdSuggestMessagesToIgnore = 0;

    public HypixelContextManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;

        ScathaProEvents.worldLeftEvent.addListener(_ -> {
            isOnSkyBlock = false;
            skyBlockArea = null;
            profileId = null;
            contextChangedEvent.trigger(scathaPro);
        });
        ScathaProEvents.serverLeftEvent.addListener(_ -> {
            environment = null;
            contextChangedEvent.trigger(scathaPro);
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

    private boolean shouldUseDevModeProfile()
    {
        return profileId == null && environment == null && scathaPro.config.dev.devModeEnabled.get();
    }

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
            contextChangedEvent.trigger(scathaPro);
        }

        return true;
    }

    public void setEnvironment(@Nullable HypixelEnvironment environment)
    {
        HypixelEnvironment previousEnvironment = this.environment;
        this.environment = environment;
        if (!Objects.equals(previousEnvironment, this.environment))
        {
            contextChangedEvent.trigger(scathaPro);
        }
    }

    public void setLocation(boolean isOnSkyBlock, @Nullable SkyBlockArea area)
    {
        this.isOnSkyBlock = isOnSkyBlock;
        this.skyBlockArea = area;

        if (isOnSkyBlock)
        {
            profileIdCommandTickTimer = 20 * 9;
        }

        ScathaPro.LOGGER.debug("Hypixel location changed - is SkyBlock: {}, SkyBlock area: {}", isOnSkyBlock, area);
        contextChangedEvent.trigger(scathaPro);
    }

    public void cancelProfileIdCommandTimer()
    {
        profileIdCommandTickTimer = 0;
    }
}