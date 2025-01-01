package com.namelessju.scathapro.gui.lists;

import com.namelessju.scathapro.alerts.Alert;
import com.namelessju.scathapro.gui.elements.BooleanSettingButton;
import com.namelessju.scathapro.gui.elements.SubMenuButton;
import com.namelessju.scathapro.gui.menus.AlertIndividualConfigGui;
import com.namelessju.scathapro.gui.menus.ScathaProGui;

import net.minecraft.util.EnumChatFormatting;

public class AlertsGuiList extends ScathaProGuiList
{
    public AlertsGuiList(ScathaProGui gui)
    {
        super(gui, 63, gui.height - 40, 25);
        
        this.listEntries.add(new AlertsConfigButtonEntry());
        
        for (Alert alert : Alert.values())
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
            if (alert.description != null) toggleAlertButton.getTooltip().setText(EnumChatFormatting.GRAY + alert.description);
            addElement(toggleAlertButton);
        }
    }
    
    private class AlertsConfigButtonEntry extends ListEntry
    {
        public AlertsConfigButtonEntry()
        {
            addElement(new SubMenuButton(1, getListWidth() / 2 - 75, 2, 150, 20, "Configure Alerts...", AlertsGuiList.this.gui, AlertIndividualConfigGui.class));
        }
    }
}
