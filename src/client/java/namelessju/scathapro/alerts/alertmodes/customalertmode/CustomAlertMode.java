package namelessju.scathapro.alerts.alertmodes.customalertmode;

import com.google.gson.JsonElement;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.managers.CustomAlertModeManager;
import namelessju.scathapro.util.JsonUtil;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;

public class CustomAlertMode extends AlertMode
{
    private final ScathaPro scathaPro;
    
    public CustomAlertMode(ScathaPro scathaPro)
    {
        super("custom", "Custom", getDefaultIconEyePositions());
        this.scathaPro = scathaPro;
    }
    
    @Override
    public @NonNull String getIconPath()
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
        return ARGB.opaque(color);
    }
    
    @Override
    public Identifier getSoundBaseIdentifier()
    {
        return Identifier.fromNamespaceAndPath(CustomAlertModePackResources.NAMESPACE, "");
    }
    
    @Override
    public float getAlertSoundVolume(Alert alert)
    {
        Double volume = JsonUtil.getDouble(scathaPro.customAlertModeManager.getCurrentSubmodePropertyJsonElement(null), "soundVolumes." + alert.alertId);
        return volume != null ? volume.floatValue() : 1f;
    }
    
    @Override
    public Component getTitleOverride(Alert alert)
    {
        JsonElement titlesJson = scathaPro.customAlertModeManager.getCurrentSubmodePropertyJsonElement("titles");
        if (titlesJson == null) return null;
        
        String title = JsonUtil.getString(titlesJson, alert.alertId + ".title");
        if (title != null) return CustomAlertModeManager.convertFormattingCodes(title);
        return null;
    }
    
    @Override
    public Component getSubtitleOverride(Alert alert)
    {
        JsonElement titlesJson = scathaPro.customAlertModeManager.getCurrentSubmodePropertyJsonElement("titles");
        if (titlesJson == null) return null;
        
        String subtitle = JsonUtil.getString(titlesJson, alert.alertId + ".subtitle");
        if (subtitle != null) return CustomAlertModeManager.convertFormattingCodes(subtitle);
        return null;
    }
}
