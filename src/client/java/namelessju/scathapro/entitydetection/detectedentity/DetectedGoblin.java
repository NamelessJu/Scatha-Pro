package namelessju.scathapro.entitydetection.detectedentity;

import namelessju.scathapro.ScathaPro;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.Nullable;

public class DetectedGoblin extends DetectedEntity
{
    public enum Type
    {
        GOLD, DIAMOND;
    }
    
    public final @Nullable Type type;

    public DetectedGoblin(ScathaPro scathaPro, ArmorStand entity, @Nullable Type type)
    {
        super(scathaPro, entity);
        this.type = type;
    }

    @Override
    public long getMaxLifetime()
    {
        return 30000;
    }

    @Override
    public void onRegistration()
    {
        if (!scathaPro.config.alerts.goblinSpawnAlertEnabled.get()) return;
        scathaPro.alertManager.goblinSpawnAlert.play(switch (type) {
            case GOLD -> Component.literal("Golden").withStyle(ChatFormatting.GOLD);
            case DIAMOND -> Component.literal("Diamond").withStyle(ChatFormatting.AQUA);
            case null -> Component.literal("Unknown type").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
        });
    }
}
