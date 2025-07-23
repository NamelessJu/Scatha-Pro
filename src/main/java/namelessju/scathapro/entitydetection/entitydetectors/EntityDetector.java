package namelessju.scathapro.entitydetection.entitydetectors;

import namelessju.scathapro.entitydetection.detectedentities.DetectedEntity;
import net.minecraft.entity.item.EntityArmorStand;

public abstract class EntityDetector
{
    public abstract DetectedEntity detectEntity(EntityArmorStand entity, String unformattedEntityName);
}
