package namelessju.scathapro.alerts.title;

import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class DisplayableAlertTitle extends AlertTitle
{
    public final @Nullable Component title;
    public final @Nullable Component subtitle;
    
    public DisplayableAlertTitle(@Nullable Component title, @Nullable Component subtitle,
                                 int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        super(fadeInTicks, stayTicks, fadeOutTicks);
        this.title = title;
        this.subtitle = subtitle;
    }
}
