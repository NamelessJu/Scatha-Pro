package namelessju.scathapro.alerts.alertmodes;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.alertmodes.customalertmode.CustomAlertMode;
import namelessju.scathapro.miscellaneous.data.OverlayIconEyePositions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;

public class AlertModeManager
{
    public static final AlertMode DEFAULT_MODE = new VanillaAlertMode();
    
    private final LinkedHashMap<String, AlertMode> modes = new LinkedHashMap<>();
    
    public final CustomAlertMode customMode;
    
    public AlertModeManager(ScathaPro scathaPro)
    {
        registerMode(DEFAULT_MODE);
        registerMode(new InbuiltAlertMode("meme", "Meme", new OverlayIconEyePositions(0.5f, 0.42f, 0.84f, 0.32f)));
        registerMode(new InbuiltAlertMode("anime", "Anime", new OverlayIconEyePositions(0.5f, 0.48f, 0.85f, 0.34f)));
        registerMode(customMode = new CustomAlertMode(scathaPro));
    }
    
    public void registerMode(@NonNull AlertMode mode)
    {
        modes.put(mode.id, mode);
    }
    
    public AlertMode[] getAllModes()
    {
        return modes.values().toArray(AlertMode[]::new);
    }
    
    public @Nullable AlertMode getModeByID(String id)
    {
        return modes.get(id);
    }
}
