package com.namelessju.scathapro.alerts.alertmodes;

import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.alerts.AlertTitle;
import com.namelessju.scathapro.miscellaneous.OverlayIconEyePositions;

import net.minecraft.util.ResourceLocation;

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
    
    
    public String getIconPath()
    {
        return "overlay/scatha_icons/default.png";
    }
    
    public String getIconOverlayPath()
    {
        return null;
    }
    
    public int getIconColor()
    {
        return 0xFFFFFF;
    }

    
    public ResourceLocation getSoundBaseResourceLocation()
    {
        return null;
    }
    
    public float getSoundVolume(Alert alert)
    {
        return 1f;
    }
    
    public AlertTitle getTitle(Alert alert)
    {
        return null;
    }
    
    
    public static OverlayIconEyePositions getDefaultIconEyePositions()
    {
        return new OverlayIconEyePositions(0.27f, 0.56f);
    }
}
