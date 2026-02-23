package namelessju.scathapro.alerts.title;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

public class FullAlertTitleTemplate extends AlertTitleTemplate
{
    public final @Nullable String subtitleText;
    public final @Nullable Style subtitleStyle;
    
    public FullAlertTitleTemplate(@Nullable String titleText, @Nullable String subtitleText, @Nullable Style titleStyle, @Nullable Style subtitleStyle, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        super(titleText, titleStyle, fadeInTicks, stayTicks, fadeOutTicks);
        this.subtitleText = subtitleText;
        this.subtitleStyle = subtitleStyle;
    }
    
    @Override
    protected @Nullable Component getDisplayableSubtitle(@Nullable Component subtitleOverride, @Nullable Component variableSubtitle)
    {
        return getComponent(subtitleText, subtitleOverride, subtitleStyle);
    }
}
