package namelessju.scathapro.managers.detectors.entities.detected;

import namelessju.scathapro.ScathaPro;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.decoration.ArmorStand;

public class DetectedJerry extends DetectedEntity
{
    public enum Type
    {
        GREEN(Style.EMPTY.withColor(TextColor.GREEN)),
        BLUE(Style.EMPTY.withColor(TextColor.BLUE)),
        PURPLE(Style.EMPTY.withColor(TextColor.DARK_PURPLE)),
        GOLDEN(Style.EMPTY.withColor(TextColor.GOLD));

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
        scathaPro.alertManager.jerrySpawnAlert.play(scathaPro, switch (type) {
            case GREEN -> Component.literal("Green").withColor(TextColor.GREEN);
            case BLUE -> Component.literal("Blue").withColor(TextColor.BLUE);
            case PURPLE -> Component.literal("Purple").withColor(TextColor.DARK_PURPLE);
            case GOLDEN -> Component.literal("Golden").withColor(TextColor.GOLD);
            case null -> Component.literal("Unknown type")
                            .setStyle(Style.EMPTY.withColor(TextColor.GRAY).withItalic(true));
        });
    }
}