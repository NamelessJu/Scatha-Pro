package namelessju.scathapro.alerts.alertmodes;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.OverlayIconEyePositions;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;

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
    public ResourceLocation getSoundBaseIdentifier()
    {
        return ScathaPro.getIdentifier("alert_modes." + id);
    }
}
