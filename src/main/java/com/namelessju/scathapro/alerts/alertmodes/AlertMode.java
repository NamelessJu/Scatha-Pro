package com.namelessju.scathapro.alerts.alertmodes;

import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.alerts.AlertTitle;

import net.minecraft.util.ResourceLocation;

public class AlertMode
{
    public static final AlertMode DEFAULT_MODE = new AlertMode("normal", "Vanilla");
    
    
    public final String id;
    public final String name;
    
    protected AlertMode(String id, String name)
    {
        this.id = id;
        this.name = name;
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
    
}
