package namelessju.scathapro.files.customalertmode;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.alerts.alertmodes.AlertMode;
import namelessju.scathapro.alerts.alertmodes.AlertModeManager;
import namelessju.scathapro.files.ScathaProSerializers;
import namelessju.scathapro.files.framework.JsonFile;
import namelessju.scathapro.files.framework.ObjectRootJsonFile;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CustomAlertModeProperties extends ObjectRootJsonFile
{
    public CustomAlertModeProperties(ScathaPro scathaPro, @NonNull String subModeId)
    {
        super(scathaPro, scathaPro.customAlertModeManager.getPropertiesFile(subModeId), true);
        this.savesDefaultValues = true;
    }
    
    public final AlertMapValue alertProperties = root.addValue("alerts", new AlertMapValue());
    
    
    public static final class AlertPropertiesValue extends ObjectValue
    {
        public final PrimitiveValueNullable<String> title = addPrimitiveNullable("title", STRING_SERIALIZER);
        public final PrimitiveValueNullable<String> subtitle = addPrimitiveNullable("subtitle", STRING_SERIALIZER);
        /** The alert mode that should provide the sound (custom mode = custom sound, otherwise plays the according inbuilt sound) */
        public final PrimitiveValueWithDefault<AlertMode> soundSourceAlertMode = addPrimitiveWithDefault("sound.source.alertMode", ScathaProSerializers.ALERT_MODE_SERIALIZER, AlertModeManager.DEFAULT_MODE);
        /** Determines for which alert the sound is inherited from the source mode */
        public final PrimitiveValueNullable<Alert> soundSourceAlert = addPrimitiveNullable("sound.source.alert", ScathaProSerializers.ALERT_SERIALIZER);
        public final PrimitiveValueWithDefault<Float> soundVolume = addPrimitiveWithDefault("sound.volume", FLOAT_SERIALIZER, 1f);
    }
    
    public final class AlertMapValue implements JsonValue
    {
        private final Map<Alert, AlertPropertiesValue> map = new HashMap<>();
        
        public @Nullable AlertPropertiesValue getPropertiesFor(Alert alert)
        {
            return map.get(alert);
        }
        
        public @NonNull AlertPropertiesValue initAndGetPropertiesFor(Alert alert)
        {
            AlertPropertiesValue value = getPropertiesFor(alert);
            if (value == null) map.put(alert, value = new AlertPropertiesValue());
            return value;
        }
        
        @Override
        public void reset()
        {
            map.clear();
        }
        
        @Override
        public boolean hasValue()
        {
            return !map.isEmpty();
        }
        
        @Override
        public void loadFromJson(@Nullable JsonElement jsonElement)
        {
            reset();
            if (!(jsonElement instanceof JsonObject jsonObject)) return;
            
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet())
            {
                Alert alert = scathaPro.alertManager.getAlertById(entry.getKey());
                if (alert == null)
                {
                    ScathaPro.LOGGER.error("Encountered alert with unknown ID {} in custom alert mode properties file \"{}\"", entry.getKey(), getFile());
                    continue;
                }
                
                AlertPropertiesValue propertiesValue = new AlertPropertiesValue();
                propertiesValue.loadFromJson(entry.getValue());
                
                map.put(alert, propertiesValue);
            }
        }
        
        @Override
        public @NonNull JsonElement getAsJson(@NonNull JsonFile<?> jsonFile)
        {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<Alert, AlertPropertiesValue> entry : map.entrySet())
            {
                jsonObject.add(entry.getKey().alertId, entry.getValue().getAsJson(jsonFile));
            }
            return jsonObject;
        }
    }
}
