package com.namelessju.scathapro.alerts;

import com.namelessju.scathapro.util.MessageUtil;

public class AlertTitle
{
    public static enum SubtitleType
    {
        NORMAL,
        VARIABLE
    }
    
    public String title;
    public final SubtitleType subtitleType;
    public String subtitle;
    private final String titleFormatting;
    private final String subtitleFormatting;
    private final int fadeInTicks;
    private final int stayTicks;
    private final int fadeOutTicks;

    public AlertTitle(String title, SubtitleType subtitleType, String subtitle, String titleFormatting, String subtitleFormatting, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        this.title = title;
        this.subtitleType = subtitleType;
        this.subtitle = subtitle;
        this.titleFormatting = titleFormatting;
        this.subtitleFormatting = subtitleFormatting;
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
    }
    
    public AlertTitle(String title, String subtitle)
    {
        this(title, SubtitleType.NORMAL, subtitle, null, null, 0, 0, 0);
    }
    
    public AlertTitle(String title, String subtitle, String titleFormatting, String subtitleFormatting, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        this(title, SubtitleType.NORMAL, subtitle, titleFormatting, subtitleFormatting, fadeInTicks, stayTicks, fadeOutTicks);
    }
    
    public AlertTitle(String title, SubtitleType subtitleType, String titleFormatting, String subtitleFormatting, int fadeInTicks, int stayTicks, int fadeOutTicks)
    {
        this(title, subtitleType, null, titleFormatting, subtitleFormatting, fadeInTicks, stayTicks, fadeOutTicks);
    }
    
    public void display()
    {
        if (fadeInTicks <= 0 && stayTicks <= 0 && fadeOutTicks <= 0) return;
        MessageUtil.displayTitle(replaceNullString(titleFormatting) + replaceNullString(title), replaceNullString(subtitleFormatting) + replaceNullString(subtitle), fadeInTicks, stayTicks, fadeOutTicks);
    }
    
    public AlertTitle replaceWith(AlertTitle other, String details)
    {
        String title = this.title;
        if (title != null && other != null && other.title != null && !other.title.trim().isEmpty()) {
            title = other.title;
        }
        
        String subtitle = this.subtitle;
        if (subtitleType == SubtitleType.NORMAL && subtitle != null && other != null && other.subtitle != null && !other.subtitle.trim().isEmpty()) {
            subtitle = other.subtitle;
        }
        else if (subtitleType == SubtitleType.VARIABLE) {
            if (details != null) subtitle = details;
            else subtitle = null;
        }
        
        return new AlertTitle(title, subtitleType, subtitle, titleFormatting, subtitleFormatting, fadeInTicks, stayTicks, fadeOutTicks);
    }
    
    
    private String replaceNullString(String str)
    {
        return str == null ? "" : str;
    }
}
