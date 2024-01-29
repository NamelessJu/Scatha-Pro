package com.namelessju.scathapro.entitydetection.entitydetectors;

import com.namelessju.scathapro.entitydetection.detectedentities.DetectedEntity;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedWorm;
import com.namelessju.scathapro.util.Util;

import net.minecraft.entity.item.EntityArmorStand;

public class WormDetector extends EntityDetector
{
    @Override
    public DetectedEntity detectEntity(EntityArmorStand entity, String unformattedEntityName)
    {
        if (unformattedEntityName != null && unformattedEntityName.contains(Util.getUnicodeString("2764")))
        {
            DetectedWorm worm = null;
            
            if (unformattedEntityName.contains("[Lv5] Worm "))
            {
                worm = new DetectedWorm(entity, false);
            }
            else if (unformattedEntityName.contains("[Lv10] Scatha "))
            {
                worm = new DetectedWorm(entity, true);
            }
            
            return worm;
        }
        
        return null;
    }
}
