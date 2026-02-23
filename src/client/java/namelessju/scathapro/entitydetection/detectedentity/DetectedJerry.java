package namelessju.scathapro.entitydetection.detectedentity;

import namelessju.scathapro.ScathaPro;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.decoration.ArmorStand;

public class DetectedJerry extends DetectedEntity
{
    public enum Type
    {
        GREEN(Style.EMPTY.withColor(ChatFormatting.GREEN)),
        BLUE(Style.EMPTY.withColor(ChatFormatting.BLUE)),
        PURPLE(Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE)),
        GOLDEN(Style.EMPTY.withColor(ChatFormatting.GOLD));
        
        public final Style detectionNametagStyle;
        
        Type(Style detectionNametagStyle)
        {
            this.detectionNametagStyle = detectionNametagStyle;
        }
    }
    
    public final Type type;
    
    public DetectedJerry(ScathaPro scathaPro, ArmorStand entity, Type type)
    {
        super(scathaPro, entity);
        this.type = type;
    }

    @Override
    public long getMaxLifetime()
    {
        return 3500;
    }
    
    @Override
    public void onRegistration()
    {
        if (!scathaPro.config.alerts.goblinSpawnAlertEnabled.get()) return;
        scathaPro.alertManager.jerrySpawnAlert.play(switch (type) {
            case GREEN -> Component.literal("Green").withStyle(ChatFormatting.GREEN);
            case BLUE -> Component.literal("Blue").withStyle(ChatFormatting.BLUE);
            case PURPLE -> Component.literal("Purple").withStyle(ChatFormatting.DARK_PURPLE);
            case GOLDEN -> Component.literal("Golden").withStyle(ChatFormatting.GOLD);
            case null -> Component.literal("Unknown type").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        });
    }
}
