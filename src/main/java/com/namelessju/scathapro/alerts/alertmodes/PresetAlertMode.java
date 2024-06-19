package com.namelessju.scathapro.alerts.alertmodes;

import com.namelessju.scathapro.ScathaPro;

import net.minecraft.util.ResourceLocation;

public class PresetAlertMode extends AlertMode
{
    public PresetAlertMode(String id, String name)
    {
        super(id, name);
    }
    
    public String getIconPath()
    {
        return "overlay/scatha_icons/mode_" + id + ".png";
    }

    public ResourceLocation getSoundBaseResourceLocation()
    {
        return new ResourceLocation(ScathaPro.MODID, "alert_modes." + id);
    }
}
