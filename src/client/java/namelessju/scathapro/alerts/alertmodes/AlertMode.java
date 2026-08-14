package namelessju.scathapro.alerts.alertmodes;

import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.miscellaneous.data.OverlayIconEyePositions;
import namelessju.scathapro.sounds.SoundData;
import namelessju.scathapro.util.Util;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class AlertMode
{
    public final String id;
    public final String name;

    /** Used for april fools googly eyes */
    public final OverlayIconEyePositions eyePositions;

    protected AlertMode(String id, String name, OverlayIconEyePositions eyePositions)
    {
        this.id = id;
        this.name = name;

        this.eyePositions = eyePositions;
    }


    public abstract @NonNull String getIconPath();

    public @Nullable String getIconOverlayPath()
    {
        return null;
    }

    public int getIconColor()
    {
        return Util.Color.WHITE;
    }


    public abstract @Nullable SoundData getSoundData(@NonNull Alert alert);

    public float getSoundVolumeMultiplier(@NonNull Alert alert)
    {
        return 1f;
    }

    public abstract @Nullable Component getTitleOverride(@NonNull Alert alert);

    public abstract @Nullable Component getSubtitleOverride(@NonNull Alert alert);
}