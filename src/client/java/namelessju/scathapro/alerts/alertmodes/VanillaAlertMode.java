package namelessju.scathapro.alerts.alertmodes;

import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.miscellaneous.data.OverlayIconEyePositions;
import namelessju.scathapro.sounds.SoundData;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class VanillaAlertMode extends AlertMode
{
    protected VanillaAlertMode()
    {
        super("normal", "Vanilla", new OverlayIconEyePositions(0.3f, 0.6f));
    }
    
    @Override
    public @NonNull String getIconPath()
    {
        return "overlay/scatha_icons/default.png";
    }
    
    @Override
    public @Nullable SoundData getSoundData(@NonNull Alert alert)
    {
        return null;
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
