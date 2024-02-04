package com.namelessju.scathapro.entitydetection.detectedentities;

import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.managers.Config;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.EnumChatFormatting;

public class DetectedGoblin extends DetectedEntity
{
    public static enum Type
    {
        GOLD, DIAMOND;
    }
    
    public final Type type;

    public DetectedGoblin(EntityArmorStand entity, Type type)
    {
        super(entity);
        this.type = type;
    }

    @Override
    protected void onRegistration()
    {
        if (!ScathaPro.getInstance().config.getBoolean(Config.Key.goblinSpawnAlert)) return;
        
        String typeText = EnumChatFormatting.GRAY.toString() + EnumChatFormatting.ITALIC + "Unknown type";
        if (type != null)
        {
            switch (type)
            {
                case GOLD:
                    typeText = EnumChatFormatting.GOLD + "Golden";
                    break;
                case DIAMOND:
                    typeText = EnumChatFormatting.AQUA + "Diamond";
                    break;
            }
        }
        
        Alert.goblinSpawn.play(typeText);
    }
}
