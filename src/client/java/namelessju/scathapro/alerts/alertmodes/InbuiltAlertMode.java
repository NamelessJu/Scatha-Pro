package namelessju.scathapro.alerts.alertmodes;

import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.miscellaneous.data.OverlayIconEyePositions;
import namelessju.scathapro.sounds.SoundData;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class InbuiltAlertMode extends AlertMode
{
    public InbuiltAlertMode(String id, String name, OverlayIconEyePositions eyePositions)
    {
        super(id, name, eyePositions);
    }

    @Override
    public @NonNull String getIconPath()
    {
        return "overlay/scatha_icons/mode_" + id + ".png";
    }

    @Override
    public @Nullable SoundData getSoundData(@NonNull Alert alert)
    {
        if (alert.alertId.equals("anti_sleep")) return null;

        return SoundData.scathaPro("alert_modes." + id + "." + alert.alertId, 1f, 1f);
    }

    @Override
    public @Nullable Component getTitleOverride(@NonNull Alert alert)
    {
        return null;
    }

    @Override
    public @Nullable Component getSubtitleOverride(@NonNull Alert alert)
    {
        return null;
    }
}