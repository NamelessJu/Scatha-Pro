package namelessju.scathapro.entitydetection.detectedentities;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.managers.Config;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.EnumChatFormatting;

// Note: this doesn't detect the Jerry itself, but the box that the hidden Jerry spawns from
public class DetectedJerry extends DetectedEntity
{
    public static enum Type
    {
        GREEN, BLUE, PURPLE, GOLDEN;
    }
    
    public final Type type;
    
    public DetectedJerry(EntityArmorStand entity, Type type)
    {
        super(entity);
        this.type = type;
    }

    @Override
    public long getMaxLifetime()
    {
        return 3500;
    }
    
    @Override
    protected void onRegistration()
    {
        if (!ScathaPro.getInstance().getConfig().getBoolean(Config.Key.goblinSpawnAlert)) return;
        
        String typeText = EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + "Unknown type";
        if (type != null)
        {
            switch (type)
            {
                case GREEN:
                    typeText = EnumChatFormatting.GREEN + "Green";
                    break;
                case BLUE:
                    typeText = EnumChatFormatting.BLUE + "Blue";
                    break;
                case PURPLE:
                    typeText = EnumChatFormatting.DARK_PURPLE + "Purple";
                    break;
                case GOLDEN:
                    typeText = EnumChatFormatting.GOLD + "Golden";
                    break;
            }
        }
        
        Alert.jerrySpawn.play(typeText);
    }
}
