package namelessju.scathapro.alerts;

import namelessju.scathapro.ScathaPro;
import net.minecraft.util.EnumChatFormatting;

public class AlertTitle
{
    public String title;
    public boolean hasVariableSubtitle;
    public String subtitle;
    public final String titleFormatting;
    public final String subtitleFormatting;
    public final int fadeInTicks;
    public final int stayTicks;
    public final int fadeOutTicks;

    private AlertTitle(String title, boolean hasVariableSubtitle, String subtitle, String titleFormatting, String subtitleFormatting, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        this.title = title;
        this.hasVariableSubtitle = hasVariableSubtitle;
        this.subtitle = subtitle;
        this.titleFormatting = titleFormatting;
        this.subtitleFormatting = subtitleFormatting;
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
    }
    
    public static AlertTitle createTextOnly(String title, String subtitle)
    {
        return new AlertTitle(title, false, subtitle, null, null, 0, 0, 0);
    }
    
    public static AlertTitle create(String title, String subtitle, String titleFormatting, String subtitleFormatting, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        return new AlertTitle(title, false, subtitle, titleFormatting, subtitleFormatting, fadeInTicks, stayTicks, fadeOutTicks);
    }
    
    public static AlertTitle createWithVariableSubtitle(String title, String titleFormatting, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        return new AlertTitle(title, true, null, titleFormatting, null, fadeInTicks, stayTicks, fadeOutTicks);
    }
    
    public void display()
    {
        if (fadeInTicks <= 0 && stayTicks <= 0 && fadeOutTicks <= 0) return;
        
        String displayTitle = "";
        if (this.title != null)
        {
            String titleDefaultFormatting = EnumChatFormatting.RESET + stringOrEmpty(this.titleFormatting);
            displayTitle = titleDefaultFormatting + this.title.replace(EnumChatFormatting.RESET.toString(), titleDefaultFormatting);
        }
        
        String displaySubtitle = "";
        if (this.subtitle != null)
        {
            String subtitleDefaultFormatting = EnumChatFormatting.RESET + stringOrEmpty(this.subtitleFormatting);
            displaySubtitle = subtitleDefaultFormatting + this.subtitle.replace(EnumChatFormatting.RESET.toString(), subtitleDefaultFormatting);
        }
        
        ScathaPro.getInstance().getAlertTitleOverlay().displayTitle(displayTitle, displaySubtitle, fadeInTicks, stayTicks, fadeOutTicks);
    }
    
    public AlertTitle replaceWith(AlertTitle other, String variableSubtitle)
    {
        String title = this.title;
        if (other != null && other.title != null && !other.title.trim().isEmpty())
        {
            title = other.title;
        }
        
        String subtitle = this.subtitle;
        if (!hasVariableSubtitle && other != null && other.subtitle != null && !other.subtitle.trim().isEmpty())
        {
            subtitle = other.subtitle;
        }
        else if (hasVariableSubtitle)
        {
            if (variableSubtitle != null) subtitle = variableSubtitle;
            else subtitle = null;
        }
        
        return new AlertTitle(title, hasVariableSubtitle, subtitle, titleFormatting, subtitleFormatting, fadeInTicks, stayTicks, fadeOutTicks);
    }
    
    
    private String stringOrEmpty(String string)
    {
        return string != null ? string : "";
    }
}
