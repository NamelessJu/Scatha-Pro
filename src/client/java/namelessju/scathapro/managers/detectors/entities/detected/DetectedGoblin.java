package namelessju.scathapro.managers.detectors.entities.detected;

import namelessju.scathapro.ScathaPro;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.jspecify.annotations.Nullable;

public class DetectedGoblin extends DetectedEntity
{
    public enum Type
    {
        GOLD, DIAMOND
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
        scathaPro.alertManager.goblinSpawnAlert.play(scathaPro, switch (type) {
            case GOLD -> Component.literal("Golden").withColor(TextColor.GOLD);
            case DIAMOND -> Component.literal("Diamond").withColor(TextColor.AQUA);
            case null -> Component.literal("Unknown type").withStyle(Style.EMPTY.withColor(TextColor.GRAY).withItalic(true));
        });
    }
}