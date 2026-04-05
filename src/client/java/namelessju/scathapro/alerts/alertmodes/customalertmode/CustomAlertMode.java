package namelessju.scathapro.alerts.alertmodes.customalertmode;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.alerts.alertmodes.AlertModeManager;
import namelessju.scathapro.files.customalertmode.CustomAlertModeProperties;
import namelessju.scathapro.sounds.SoundData;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CustomAlertMode extends AlertMode
{
    private final ScathaPro scathaPro;
    
    public CustomAlertMode(ScathaPro scathaPro)
    {
        super("custom", "Custom", AlertModeManager.DEFAULT_MODE.eyePositions);
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
        final int hue = (int) ((TimeUtil.getEpochMilliseconds() / 8) % 360);
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
    public @Nullable SoundData getSoundData(@NonNull Alert alert)
    {
        CustomAlertModeProperties.AlertPropertiesValue alertProperties = getAlertProperties(alert);
        AlertMode sourceMode;
        Alert sourceAlert;
        if (alertProperties != null)
        {
            sourceMode = alertProperties.soundSourceAlertMode.get();
            sourceAlert = alertProperties.soundSourceAlert.getOr(alert);
        }
        else
        {
            sourceMode = AlertModeManager.DEFAULT_MODE;
            sourceAlert = alert;
        }
        
        if (sourceMode != this)
        {
            return sourceMode.getSoundData(sourceAlert);
        }
        
        SoundData soundData = new SoundData(
            Identifier.fromNamespaceAndPath(CustomAlertModePackResources.NAMESPACE, alert.alertId),
            1f, 1f
        );
        if (!scathaPro.soundManager.soundExists(soundData.identifier()))
        {
            ScathaPro.LOGGER.warn(
                "Couldn't play custom alert sound \"{}\" for {}: Sound not found - playing default sound instead",
                soundData.identifier(), alert.alertId
            );
            return null;
        }
        return soundData;
    }
    
    @Override
    public float getSoundVolumeMultiplier(@NonNull Alert alert)
    {
        CustomAlertModeProperties.AlertPropertiesValue alertProperties = getAlertProperties(alert);
        return alertProperties != null ? alertProperties.soundVolume.get() : 1f;
    }
    
    @Override
    public @Nullable Component getTitleOverride(@NonNull Alert alert)
    {
        CustomAlertModeProperties.AlertPropertiesValue alertProperties = getAlertProperties(alert);
        if (alertProperties == null) return null;
        
        String title = alertProperties.title.get();
        if (title != null) return CustomAlertModeManager.formattedStringToComponent(title);
        return null;
    }
    
    @Override
    public @Nullable Component getSubtitleOverride(@NonNull Alert alert)
    {
        CustomAlertModeProperties.AlertPropertiesValue alertProperties = getAlertProperties(alert);
        if (alertProperties == null) return null;
        
        String subtitle = alertProperties.subtitle.get();
        if (subtitle != null) return CustomAlertModeManager.formattedStringToComponent(subtitle);
        return null;
    }
    
    private CustomAlertModeProperties.@Nullable AlertPropertiesValue getAlertProperties(Alert alert)
    {
        CustomAlertModeProperties properties = scathaPro.customAlertModeManager.getCurrentSubModeProperties();
        if (properties == null) return null;
        return properties.alertProperties.getPropertiesFor(alert);
    }
}
