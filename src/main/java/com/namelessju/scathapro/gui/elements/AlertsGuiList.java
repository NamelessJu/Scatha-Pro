package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.alerts.Alert;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;

public class AlertsGuiList extends ScathaProGuiList
{
    public AlertsGuiList(GuiScreen gui)
    {
        super(gui, 65, gui.height - 40, 40);
        
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
            addButton(new BooleanSettingButton(1, 0, 5, getListWidth(), 20, alert.alertName, alert.configKey));
            addLabel(EnumChatFormatting.GRAY + alert.description, 2, 28, getListWidth() - 4, 10).setCentered();
        }
    }
}
