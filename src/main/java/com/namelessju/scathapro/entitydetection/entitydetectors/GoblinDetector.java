package com.namelessju.scathapro.entitydetection.entitydetectors;

import com.namelessju.scathapro.entitydetection.detectedentities.DetectedEntity;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedGoblin;
import com.namelessju.scathapro.util.UnicodeSymbol;

import net.minecraft.entity.item.EntityArmorStand;

public class GoblinDetector extends EntityDetector
{
    @Override
    public DetectedEntity detectEntity(EntityArmorStand entity, String unformattedEntityName)
    {
        if (unformattedEntityName != null && unformattedEntityName.contains(String.valueOf(UnicodeSymbol.heavyBlackHeart)))
        {
            if (unformattedEntityName.contains("[Lv50] Golden Goblin "))
            {
                return new DetectedGoblin(entity, DetectedGoblin.Type.GOLD);
            }
            
            if (unformattedEntityName.contains("[Lv500] Diamond Goblin "))
            {
                return new DetectedGoblin(entity, DetectedGoblin.Type.DIAMOND);
            }
        }
        
        return null;
    }
}
