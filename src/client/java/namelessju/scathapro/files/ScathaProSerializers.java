package namelessju.scathapro.files;

import com.google.gson.JsonPrimitive;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.files.framework.JsonFile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ScathaProSerializers
{
    private ScathaProSerializers() {}

    public final static JsonFile.JsonValue.Serializer<Alert, JsonPrimitive> ALERT_SERIALIZER = new JsonFile.JsonValue.Serializer<>()
    {
        @Override
        public @Nullable Alert jsonToValue(@NonNull JsonPrimitive jsonPrimitive)
        {
            if (!jsonPrimitive.isString()) return null;
            return ScathaPro.getInstance().alertManager.getAlertById(jsonPrimitive.getAsString());
        }

        @Override
        public @NonNull JsonPrimitive valueToJson(@NonNull Alert alert)
        {
            return new JsonPrimitive(alert.alertId);
        }
    };

    public final static JsonFile.JsonValue.Serializer<AlertMode, JsonPrimitive> ALERT_MODE_SERIALIZER = new JsonFile.JsonValue.Serializer<>()
    {
        @Override
        public @Nullable AlertMode jsonToValue(@NonNull JsonPrimitive jsonPrimitive)
        {
            if (!jsonPrimitive.isString()) return null;
            return ScathaPro.getInstance().alertModeManager.getModeByID(jsonPrimitive.getAsString());
        }

        @Override
        public @NonNull JsonPrimitive valueToJson(@NonNull AlertMode alertMode)
        {
            return new JsonPrimitive(alertMode.id);
        }
    };
}