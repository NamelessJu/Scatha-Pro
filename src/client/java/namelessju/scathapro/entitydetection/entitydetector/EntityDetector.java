package namelessju.scathapro.entitydetection.entitydetector;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.entitydetection.detectedentity.DetectedEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class EntityDetector
{
    public abstract DetectedEntity detectEntity(@NonNull ScathaPro scathaPro, @NonNull ArmorStand entity, @Nullable String unformattedEntityName);
}
