package namelessju.scathapro.entitydetection.entitydetector;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.entitydetection.detectedentity.DetectedEntity;
import namelessju.scathapro.entitydetection.detectedentity.DetectedGoblin;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class GoblinDetector extends EntityDetector
{
    @Override
    public DetectedEntity detectEntity(@NonNull ScathaPro scathaPro, @NonNull ArmorStand entity, @Nullable String unformattedEntityName)
    {
        if (unformattedEntityName != null && unformattedEntityName.contains(String.valueOf(UnicodeSymbol.heavyBlackHeart)))
        {
            if (unformattedEntityName.contains("[Lv50]") && unformattedEntityName.contains("Golden Goblin"))
            {
                return new DetectedGoblin(scathaPro, entity, DetectedGoblin.Type.GOLD);
            }
            if (unformattedEntityName.contains("[Lv500]") && unformattedEntityName.contains("Diamond Goblin "))
            {
                return new DetectedGoblin(scathaPro, entity, DetectedGoblin.Type.DIAMOND);
            }
        }
        
        return null;
    }
}
