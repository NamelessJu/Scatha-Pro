package namelessju.scathapro.managers.detectors.entities;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.detectors.entities.detected.DetectedEntity;
import namelessju.scathapro.managers.detectors.entities.detected.DetectedWorm;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class WormDetector extends EntityDetector
{
    @Override
    public DetectedEntity detectEntity(@NonNull ScathaPro scathaPro, @NonNull ArmorStand entity, @Nullable String unformattedEntityName)
    {
        if (unformattedEntityName == null || !unformattedEntityName.contains(String.valueOf(UnicodeSymbol.hypixelHeart)))
        {
            return null;
        }

        if (unformattedEntityName.contains("[Lv5]") && unformattedEntityName.contains(" Stoneworm "))
        {
            return new DetectedWorm(scathaPro, entity, false);
        }
        if (unformattedEntityName.contains("[Lv10]") && unformattedEntityName.contains(" Scatha "))
        {
            return new DetectedWorm(scathaPro, entity, true);
        }

        return null;
    }
}