package namelessju.scathapro.entitydetection.entitydetector;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.entitydetection.detectedentity.DetectedEntity;
import namelessju.scathapro.entitydetection.detectedentity.DetectedJerry;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class JerryDetector extends EntityDetector
{
    // Note: this doesn't detect the Jerry itself, but the box that the hidden Jerry spawns from
    @Override
    public DetectedEntity detectEntity(@NonNull ScathaPro scathaPro, @NonNull ArmorStand entity, @Nullable String unformattedEntityName)
    {
        String expectedEntityName = "Mayor Jerry's";
        
        if (unformattedEntityName == null || !unformattedEntityName.contains(expectedEntityName)) return null;
        
        Optional<DetectedJerry.Type> detectedType = entity.getName().visit((style, text) -> {
            // Detect first style that matches
            for (DetectedJerry.Type type : DetectedJerry.Type.values())
            {
                if (style.equals(type.detectionNametagStyle.applyTo(style)))
                {
                    return Optional.of(type);
                }
            }
            return Optional.empty();
        }, Style.EMPTY);
        
        return new DetectedJerry(scathaPro, entity, detectedType.orElse(null));
    }
}
