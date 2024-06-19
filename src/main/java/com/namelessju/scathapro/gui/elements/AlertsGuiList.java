package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.alerts.Alert;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class AlertsGuiList extends ScathaProGuiList
{
    public AlertsGuiList(GuiScreen gui)
    {
        super(gui, 65, gui.height - 40, 27);
        
        Alert[] alerts = Alert.values();
        for (Alert alert : alerts)
        {
            if (alert.configKey == null) continue;
            this.listEntries.add(new AlertEntry(alert));
        }
    }
    
    
    private class AlertEntry extends ListEntry
    {
        public AlertEntry(Alert alert)
        {
            BooleanSettingButton button = new BooleanSettingButton(1, 0, 2, getListWidth(), 20, alert.alertName, alert.configKey);
            if (alert.description != null) button.setTooltip(EnumChatFormatting.GRAY + alert.description);
            addButton(button);
        }
    }
}
