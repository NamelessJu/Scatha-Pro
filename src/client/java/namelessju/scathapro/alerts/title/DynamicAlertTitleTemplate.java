package namelessju.scathapro.alerts.title;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

public class DynamicAlertTitleTemplate extends AlertTitleTemplate
{
    public DynamicAlertTitleTemplate(@Nullable String titleText, @Nullable Style titleStyle, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        super(titleText, titleStyle, fadeInTicks, stayTicks, fadeOutTicks);
    }
    
    @Override
    protected @Nullable Component getDisplayableSubtitle(@Nullable Component subtitleOverride, @Nullable Component variableSubtitle)
    {
        return variableSubtitle;
    }
}
