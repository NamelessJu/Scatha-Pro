package namelessju.scathapro.managers;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.alerts.alertmodes.InbuiltAlertMode;
import namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertMode;
import namelessju.scathapro.miscellaneous.data.OverlayIconEyePositions;
import namelessju.scathapro.files.Config;

import java.util.LinkedHashMap;

public class AlertModeManager
{
    private final Config config;
    
    private final LinkedHashMap<String, AlertMode> modes = new LinkedHashMap<>();
    
    public AlertModeManager(ScathaPro scathaPro)
    {
        this.config = scathaPro.config;
        
        registerMode(AlertMode.DEFAULT_MODE);
        registerMode(new InbuiltAlertMode("meme", "Meme", new OverlayIconEyePositions(0.5f, 0.42f, 0.84f, 0.32f)));
        registerMode(new InbuiltAlertMode("anime", "Anime", new OverlayIconEyePositions(0.5f, 0.48f, 0.85f, 0.34f)));
        registerMode(new CustomAlertMode(scathaPro));
    }
    
    public void registerMode(AlertMode mode)
    {
        modes.put(mode.id, mode);
    }
    
    public AlertMode[] getAllModes()
    {
        return modes.values().toArray(AlertMode[]::new);
    }
    
    public AlertMode getModeByID(String id)
    {
        return modes.get(id);
    }
    
    // TODO: make an update method instead of doing this logic multiple times per tick
    public AlertMode getCurrentMode()
    {
        String currentModeId = config.alerts.mode.get();
        if (currentModeId == null || currentModeId.isEmpty())
        {
            return AlertMode.DEFAULT_MODE;
        }
        AlertMode mode = getModeByID(currentModeId);
        return mode != null ? mode : AlertMode.DEFAULT_MODE;
    }
}
