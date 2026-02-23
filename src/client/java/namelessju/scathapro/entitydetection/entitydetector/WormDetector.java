package namelessju.scathapro.entitydetection.entitydetector;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.entitydetection.detectedentity.DetectedEntity;
import namelessju.scathapro.entitydetection.detectedentity.DetectedWorm;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class WormDetector extends EntityDetector
{
    @Override
    public DetectedEntity detectEntity(@NonNull ScathaPro scathaPro, @NonNull ArmorStand entity, @Nullable String unformattedEntityName)
    {
        if (unformattedEntityName == null || !unformattedEntityName.contains(String.valueOf(UnicodeSymbol.heavyBlackHeart)))
        {
            return null;
        }
        
        if (unformattedEntityName.contains("[Lv5] Worm "))
        {
            return new DetectedWorm(scathaPro, entity, false);
        }
        if (unformattedEntityName.contains("[Lv10] Scatha "))
        {
            return new DetectedWorm(scathaPro, entity, true);
        }
        
        return null;
    }
}
