package com.namelessju.scathapro.gui.elements;

import com.namelessju.scathapro.alerts.Alert;

import net.minecraft.client.gui.GuiScreen;

public class AlertsGuiList extends ScathaProGuiList
{
    public AlertsGuiList(GuiScreen gui)
    {
        super(gui, 65, gui.height - 40, 25);
        
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
            addButton(new BooleanSettingButton(1, getListWidth() / 2 - 100, 2, 200, 20, alert.alertName, alert.configKey));
        }
    }
}
