package com.namelessju.scathapro.alerts.alertmodes;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.miscellaneous.OverlayIconEyePositions;

import net.minecraft.util.ResourceLocation;

public class PresetAlertMode extends AlertMode
{
    public PresetAlertMode(String id, String name, OverlayIconEyePositions eyePositions)
    {
        super(id, name, eyePositions);
    }

    @Override
    public String getIconPath()
    {
        return "overlay/scatha_icons/mode_" + id + ".png";
    }

    @Override
    public ResourceLocation getSoundBaseResourceLocation()
    {
        return new ResourceLocation(ScathaPro.MODID, "alert_modes." + id);
    }
}
