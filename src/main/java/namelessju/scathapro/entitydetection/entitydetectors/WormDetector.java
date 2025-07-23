package namelessju.scathapro.entitydetection.entitydetectors;

import namelessju.scathapro.entitydetection.detectedentities.DetectedEntity;
import namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.entity.item.EntityArmorStand;

public class WormDetector extends EntityDetector
{
    @Override
    public DetectedEntity detectEntity(EntityArmorStand entity, String unformattedEntityName)
    {
        if (unformattedEntityName != null && unformattedEntityName.endsWith(String.valueOf(UnicodeSymbol.heavyBlackHeart)))
        {
            DetectedWorm worm = null;
            
            if (unformattedEntityName.startsWith("[Lv5] Worm "))
            {
                worm = new DetectedWorm(entity, false);
            }
            else if (unformattedEntityName.startsWith("[Lv10] Scatha "))
            {
                worm = new DetectedWorm(entity, true);
            }
            
            return worm;
        }
        
        return null;
    }
}
