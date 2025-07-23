package namelessju.scathapro.entitydetection.entitydetectors;

import namelessju.scathapro.entitydetection.detectedentities.DetectedEntity;
import namelessju.scathapro.entitydetection.detectedentities.DetectedJerry;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.EnumChatFormatting;

public class JerryDetector extends EntityDetector
{
    @Override
    public DetectedEntity detectEntity(EntityArmorStand entity, String unformattedEntityName)
    {
        if (unformattedEntityName.equals("Mayor Jerry's"))
        {
            String formattedName = entity.hasCustomName() ? entity.getCustomNameTag() : "";

            if (formattedName.startsWith(EnumChatFormatting.GREEN.toString()))
            {
                return new DetectedJerry(entity, DetectedJerry.Type.GREEN);
            }

            if (formattedName.startsWith(EnumChatFormatting.BLUE.toString()))
            {
                return new DetectedJerry(entity, DetectedJerry.Type.BLUE);
            }

            if (formattedName.startsWith(EnumChatFormatting.DARK_PURPLE.toString()))
            {
                return new DetectedJerry(entity, DetectedJerry.Type.PURPLE);
            }

            if (formattedName.startsWith(EnumChatFormatting.GOLD.toString()))
            {
                return new DetectedJerry(entity, DetectedJerry.Type.GOLDEN);
            }

            return new DetectedJerry(entity, null);
        }
        
        return null;
    }
}
