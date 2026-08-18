package namelessju.scathapro.files.customalertmode;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.files.framework.ObjectRootJsonFile;
import org.jspecify.annotations.NonNull;

public class CustomAlertModeMeta extends ObjectRootJsonFile
{
    public CustomAlertModeMeta(ScathaPro scathaPro, @NonNull String subModeId)
    {
        super(scathaPro, scathaPro.customAlertModeManager.getMetaFile(subModeId), true);
        this.savesDefaultValues = true;
    }

    public final PrimitiveValueNullable<String> modeName = root.addPrimitiveNullable("modeName", STRING_SERIALIZER);
    public final PrimitiveValueNullable<Long> lastUsedAtTimestamp = root.addPrimitiveNullable("lastUsedAtTimestamp", LONG_SERIALIZER);
}