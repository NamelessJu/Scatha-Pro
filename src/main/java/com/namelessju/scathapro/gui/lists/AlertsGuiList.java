package com.namelessju.scathapro.gui.lists;

import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.SubMenuButton;
import com.namelessju.scathapro.gui.menus.ScathaProGui;

import net.minecraft.util.EnumChatFormatting;

public class AlertsGuiList extends ScathaProGuiList
{
    public AlertsGuiList(ScathaProGui gui)
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
            BooleanSettingButton toggleAlertButton = new BooleanSettingButton(1, 0, 2, getListWidth(), 20, alert.alertName, alert.configKey);
            if (alert.description != null) toggleAlertButton.setTooltip(EnumChatFormatting.GRAY + alert.description);
            addButton(toggleAlertButton);
            
            if (alert.settingsGuiClass != null)
            {
                toggleAlertButton.setWidth(getListWidth() - 80);
                addButton(new SubMenuButton(1, getListWidth() - 75, 2, 75, 20, "Configure...", AlertsGuiList.this.gui, alert.settingsGuiClass));
            }
        }
    }
}
