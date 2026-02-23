package namelessju.scathapro.alerts.alertmodes;

import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.miscellaneous.data.OverlayIconEyePositions;
import namelessju.scathapro.util.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class AlertMode
{
    public static final AlertMode DEFAULT_MODE = new AlertMode("normal", "Vanilla", new OverlayIconEyePositions(0.3f, 0.6f));
    
    
    public final String id;
    public final String name;
    
    /**
     * Used for april fools googly eyes
     */
    public final OverlayIconEyePositions eyePositions;
    
    protected AlertMode(String id, String name, OverlayIconEyePositions eyePositions)
    {
        this.id = id;
        this.name = name;
        
        this.eyePositions = eyePositions;
    }
    
    
    public @NonNull String getIconPath()
    {
        return "overlay/scatha_icons/default.png";
    }
    
    public @Nullable String getIconOverlayPath()
    {
        return null;
    }
    
    public int getIconColor()
    {
        return Util.Color.WHITE;
    }

    
    public @Nullable Identifier getSoundBaseIdentifier()
    {
        return null;
    }
    
    public float getAlertSoundVolume(Alert alert)
    {
        return 1f;
    }
    
    public @Nullable Component getTitleOverride(Alert alert)
    {
        return null;
    }
    
    public @Nullable Component getSubtitleOverride(Alert alert)
    {
        return null;
    }
    
    
    // TODO: should this really be here?
    public static OverlayIconEyePositions getDefaultIconEyePositions()
    {
        return new OverlayIconEyePositions(0.27f, 0.56f);
    }
}
