package namelessju.scathapro.alerts.alertmodes;

import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.miscellaneous.data.OverlayIconEyePositions;
import namelessju.scathapro.miscellaneous.data.Texture;
import namelessju.scathapro.sounds.SoundData;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class InbuiltAlertMode extends AlertMode
{
    private final Texture texture;

    public InbuiltAlertMode(String id, String name, OverlayIconEyePositions eyePositions)
    {
        super(id, name, eyePositions);
        this.texture = Texture.scathaPro("overlay/scatha_icons/mode_" + id + ".png", 64, 64);
    }

    @Override
    public @NonNull Texture getIconTexture()
    {
        return texture;
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