package namelessju.scathapro.files.customalertmode;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.framework.ScathaProFile;
import namelessju.scathapro.util.JsonUtil;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CustomAlertModeMetaUpdater extends ScathaProFile
{
    private final @NonNull String subModeId;
    private @Nullable JsonObject root = null;
    
    public CustomAlertModeMetaUpdater(ScathaPro scathaPro, @NonNull String subModeId)
    {
        super(scathaPro, scathaPro.customAlertModeManager.getMetaFile(subModeId));
        this.subModeId = subModeId;
    }
    
    @SuppressWarnings("RedundantIfStatement")
    @Override
    protected void deserialize(@Nullable String content)
    {
        if (content == null) return;
        JsonElement parsedJson = JsonUtil.parseJson(content);
        if (!(parsedJson instanceof JsonObject jsonObject)) return;
        root = jsonObject;
        boolean updated = false;
        
        if (JsonUtil.move(root, "name", "modeName")) updated = true;
        if (JsonUtil.move(root, "lastUsed", "lastUsedAtTimestamp")) updated = true;
        
        if (updated)
        {
            save();
            ScathaPro.LOGGER.info("Updated custom alert mode meta file for {}", subModeId);
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
