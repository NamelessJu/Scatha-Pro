package com.namelessju.scathapro.alerts;

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
        return "overlay/mode_icons/" + id + ".png";
    }

    public ResourceLocation getSoundBaseResourceLocation()
    {
        return new ResourceLocation(ScathaPro.MODID, "alert_modes." + id);
    }
}
