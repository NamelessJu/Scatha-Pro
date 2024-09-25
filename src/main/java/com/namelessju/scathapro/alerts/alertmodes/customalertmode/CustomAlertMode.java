package com.namelessju.scathapro.alerts.alertmodes.customalertmode;

import com.google.gson.JsonElement;
import com.namelessju.scathapro.ScathaPro;
import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.alerts.AlertTitle;
import com.namelessju.scathapro.alerts.alertmodes.AlertMode;
import com.namelessju.scathapro.util.JsonUtil;
import com.namelessju.scathapro.util.TextUtil;
import com.namelessju.scathapro.util.TimeUtil;

import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

public class CustomAlertMode extends AlertMode
{
    public CustomAlertMode(String id, String name)
    {
        super(id, name);
    }
    
    @Override
    public String getIconPath()
    {
        return "overlay/scatha_icons/mode_custom.png";
    }
    
    @Override
    public String getIconOverlayPath()
    {
        return "overlay/scatha_icons/mode_custom_overlay.png";
    }
    
    @Override
    public int getIconColor()
    {
        final int hue = (int) ((TimeUtil.now() / 8) % 360);
        float h = hue / 60f;
        float x = 1 - Math.abs(h % 2 - 1);
        
        float r = 0;
        float g = 0;
        float b = 0;
        
        switch ((int) h)
        {
            case 0:
                r = 1f;
                g = x;
                break;
            case 1:
                r = x;
                g = 1f;
                break;
            case 2:
                g = 1f;
                b = x;
                break;
            case 3:
                g = x;
                b = 1f;
                break;
            case 4:
                r = x;
                b = 1f;
                break;
            case 5:
                r = 1f;
                b = x;
                break;
        }
        
        int color = (int) (r * 255f);
        color = (color << 8) + (int) (g * 255f);
        color = (color << 8) + (int) (b * 255f);
        return color;
    }
    
    @Override
    public ResourceLocation getSoundBaseResourceLocation()
    {
        return new ResourceLocation(CustomAlertModeManager.resourceDomain, "");
    }

    @Override
    public AlertTitle getTitle(Alert alert)
    {
        JsonElement titlesJson = ScathaPro.getInstance().getCustomAlertModeManager().getCurrentSubmodePropertyJsonElement("titles");
        if (titlesJson == null) return null;
        String title = JsonUtil.getString(titlesJson, alert.alertId + "/title");
        String subtitle = JsonUtil.getString(titlesJson, alert.alertId + "/subtitle");
        
        if (title != null)
        {
            title = StringUtils.stripControlCodes(title).replaceAll("&(?=" + TextUtil.formattingCodesRegex + ")", TextUtil.formattingStartCharacter);
            String defaultTitleFormatting = alert.getDefaultTitle().titleFormatting;
            title = title.replace(EnumChatFormatting.RESET.toString(), EnumChatFormatting.RESET + (defaultTitleFormatting != null ? defaultTitleFormatting : ""));
        }
        if (subtitle != null)
        {
            subtitle = StringUtils.stripControlCodes(subtitle).replaceAll("&(?=" + TextUtil.formattingCodesRegex + ")", TextUtil.formattingStartCharacter);
            String defaultSubtitleFormatting = alert.getDefaultTitle().subtitleFormatting;
            subtitle = subtitle.replace(EnumChatFormatting.RESET.toString(), EnumChatFormatting.RESET + (defaultSubtitleFormatting != null ? defaultSubtitleFormatting : ""));
        }
        
        return AlertTitle.createTextOnly(title, subtitle);
    }
}
