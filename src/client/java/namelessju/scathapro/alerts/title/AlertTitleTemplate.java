package namelessju.scathapro.alerts.title;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class AlertTitleTemplate extends AlertTitle
{
    public final @Nullable String titleText;
    public final @Nullable Style titleStyle;
    
    public AlertTitleTemplate(@Nullable String titleText, @Nullable Style titleStyle,
                              int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        super(fadeInTicks, stayTicks, fadeOutTicks);
        this.titleText = titleText;
        this.titleStyle = titleStyle;
    }
    
    public @NonNull DisplayableAlertTitle getDisplayable(@Nullable Component titleOverride, @Nullable Component subtitleOverride, @Nullable Component variableSubtitle)
    {
        return new DisplayableAlertTitle(
            getComponent(titleText, titleOverride, titleStyle),
            getDisplayableSubtitle(subtitleOverride, variableSubtitle),
            fadeInTicks, stayTicks, fadeOutTicks
        );
    }
    
    protected final @Nullable Component getComponent(@Nullable String defaultText, @Nullable Component override, @Nullable Style style)
    {
        MutableComponent component = null;
        if (override != null) component = Component.empty().append(override); // empty root so override can inherit the style
        else if (defaultText != null) component = Component.literal(defaultText);
        if (component != null && style != null) component.setStyle(style);
        return component;
    }
    
    protected abstract @Nullable Component getDisplayableSubtitle(@Nullable Component subtitleOverride, @Nullable Component variableSubtitle);
}
