package namelessju.scathapro.alerts.alertmodes.customalertmode;

import com.google.gson.JsonElement;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.alerts.AlertTitle;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.util.JsonUtil;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

public class CustomAlertMode extends AlertMode
{
    public CustomAlertMode(String id, String name)
    {
        super(id, name, getDefaultIconEyePositions());
    }
    
    @Override
    public String getIconPath()
    {
        return "overlay/scatha_icons/mode_custom.png";
    }
    
    @Override
    public String getIconOverlayPath()
    {
        return "overlay/scatha_icons/mode_custom_overlay.png";
    }
    
    @Override
    public int getIconColor()
    {
        final int hue = (int) ((TimeUtil.now() / 8) % 360);
        float h = hue / 60f;
        float x = 1 - Math.abs(h % 2 - 1);
        
        float r = 0;
        float g = 0;
        float b = 0;
        
        switch ((int) h)
        {
            case 0:
                r = 1f;
                g = x;
                break;
            case 1:
                r = x;
                g = 1f;
                break;
            case 2:
                g = 1f;
                b = x;
                break;
            case 3:
                g = x;
                b = 1f;
                break;
            case 4:
                r = x;
                b = 1f;
                break;
            case 5:
                r = 1f;
                b = x;
                break;
        }
        
        int color = (int) (r * 255f);
        color = (color << 8) + (int) (g * 255f);
        color = (color << 8) + (int) (b * 255f);
        return color;
    }
    
    @Override
    public ResourceLocation getSoundBaseResourceLocation()
    {
        return new ResourceLocation(CustomAlertModeManager.resourceDomain, "");
    }
    
    @Override
    public float getSoundVolume(Alert alert)
    {
        Double volume = JsonUtil.getDouble(ScathaPro.getInstance().getCustomAlertModeManager().getCurrentSubmodePropertyJsonElement(null), "soundVolumes/" + alert.alertId);
        return volume != null ? volume.floatValue() : 1f;
    }
    
    @Override
    public AlertTitle getTitle(Alert alert)
    {
        JsonElement titlesJson = ScathaPro.getInstance().getCustomAlertModeManager().getCurrentSubmodePropertyJsonElement("titles");
        if (titlesJson == null) return null;
        
        String title = JsonUtil.getString(titlesJson, alert.alertId + "/title");
        if (title != null)
        {
            title = StringUtils.stripControlCodes(title).replaceAll("&(?=" + TextUtil.formattingCodesRegex + ")", TextUtil.formattingStartCharacter);
        }
        
        String subtitle = JsonUtil.getString(titlesJson, alert.alertId + "/subtitle");
        if (subtitle != null)
        {
            subtitle = StringUtils.stripControlCodes(subtitle).replaceAll("&(?=" + TextUtil.formattingCodesRegex + ")", TextUtil.formattingStartCharacter);
        }
        
        return AlertTitle.createTextOnly(title, subtitle);
    }
}
