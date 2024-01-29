package com.namelessju.scathapro.entitydetection.entitydetectors;

import com.namelessju.scathapro.entitydetection.detectedentities.DetectedEntity;
import com.namelessju.scathapro.entitydetection.detectedentities.DetectedGoblin;
import com.namelessju.scathapro.util.Util;

import net.minecraft.entity.item.EntityArmorStand;

public class GoblinDetector extends EntityDetector
{
    @Override
    public DetectedEntity detectEntity(EntityArmorStand entity, String unformattedEntityName)
    {
        if (unformattedEntityName != null && unformattedEntityName.contains(Util.getUnicodeString("2764")))
        {
            DetectedGoblin goblin = null;
            
            if (unformattedEntityName.contains("[Lv50] Golden Goblin "))
            {
                goblin = new DetectedGoblin(entity, DetectedGoblin.Type.GOLD);
            }
            else if (unformattedEntityName.contains("[Lv500] Diamond Goblin "))
            {
                goblin = new DetectedGoblin(entity, DetectedGoblin.Type.DIAMOND);
            }
            
            return goblin;
        }
        
        return null;
    }
}
