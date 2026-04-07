package namelessju.scathapro.files.customalertmode;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.framework.ScathaProFile;
import namelessju.scathapro.util.JsonUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CustomAlertModePropertiesUpdater extends ScathaProFile
{
    private final @NonNull String subModeId;
    private @Nullable JsonObject root = null;
    
    public CustomAlertModePropertiesUpdater(ScathaPro scathaPro, @NonNull String subModeId)
    {
        super(scathaPro, scathaPro.customAlertModeManager.getPropertiesFile(subModeId));
        this.subModeId = subModeId;
    }
    
    @Override
    protected void deserialize(@Nullable String content)
    {
        if (content == null) return;
        JsonElement parsedJson = JsonUtil.parseJson(content);
        if (!(parsedJson instanceof JsonObject jsonObject)) return;
        root = jsonObject;
        boolean updated = false;
        
        JsonObject titlesObject = JsonUtil.getJsonObject(root, "titles");
        if (titlesObject != null)
        {
            for (String alert : titlesObject.keySet().toArray(String[]::new))
            {
                if (!scathaPro.customAlertModeManager.getAlertAudioFile(subModeId, alert).exists()) continue;
                
                JsonObject alertEntry = JsonUtil.getJsonObject(titlesObject, alert);
                if (alertEntry == null) continue;
                
                JsonObject soundSourceProperties = JsonUtil.getJsonObject(alertEntry, "sound.source");
                if (soundSourceProperties == null)
                {
                    JsonObject value = new JsonObject();
                    value.add("alertMode", new JsonPrimitive(scathaPro.alertModeManager.customMode.id));
                    JsonUtil.set(alertEntry, "sound.source", value);
                }
            }
            
            JsonUtil.set(root, "alerts", titlesObject);
            JsonUtil.remove(root, "titles");
            updated = true;
        }
        
        JsonElement soundVolumesElement = root.get("soundVolumes");
        if (soundVolumesElement instanceof JsonObject soundVolumesObject)
        {
            for (String alert : soundVolumesObject.keySet())
            {
                JsonUtil.set(root, "alerts." + alert + ".sound.volume", soundVolumesObject.get(alert));
            }
            
            root.remove("soundVolumes");
            updated = true;
        }
        
        if (updated)
        {
            save();
            ScathaPro.LOGGER.info("Updated custom alert mode properties file for {}", subModeId);
        }
        root = null;
    }
    
    @Override
    protected @NonNull String serialize()
    {
        if (root == null) throw new IllegalStateException("File cannot be saved manually");
        return JsonUtil.toString(root, true);
    }
}
