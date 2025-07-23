package namelessju.scathapro.gui.lists;

import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.gui.elements.CycleButton;
import namelessju.scathapro.gui.elements.IGuiElement;
import namelessju.scathapro.gui.elements.ScathaProLabel;
import namelessju.scathapro.gui.elements.ScathaProSlider;
import namelessju.scathapro.gui.menus.ScathaProGui;
import namelessju.scathapro.managers.Config;
import namelessju.scathapro.miscellaneous.enums.OldLobbyAlertTriggerMode;
import net.minecraftforge.fml.client.config.GuiSlider;

public class AlertConfigurationList extends ScathaProGuiList implements GuiSlider.ISlider
{
    private final Config config;
    
    private ScathaProSlider antiSleepIntervalMinSlider, antiSleepIntervalMaxSlider;
    
    public AlertConfigurationList(ScathaProGui gui)
    {
        super(gui, 25);
        
        config = gui.scathaPro.getConfig();
        
        addAlertHeading(Alert.bedrockWall);
        add(new ScathaProSlider(1, 0, 0, 0, 0, "Trigger Distance: ", " Blocks", 0, 50, config.getInt(Config.Key.bedrockWallAlertTriggerDistance), false, true, this));
        
        addAlertHeading(Alert.oldLobby);
        add(new ScathaProSlider(5, 0, 0, 0, 0, "Trigger Day: ", "", 1, 30, config.getInt(Config.Key.oldLobbyAlertTriggerDay), false, true, this));
        add(new CycleButton<OldLobbyAlertTriggerMode>(6, 0, 0, 0, 0, "Show On", CycleButton.EnumOption.from(OldLobbyAlertTriggerMode.class, false), config.getEnum(Config.Key.oldLobbyAlertTriggerMode, OldLobbyAlertTriggerMode.class), button -> {
            config.set(Config.Key.oldLobbyAlertTriggerMode, button.getSelectedValue().name());
        }));
        
        addAlertHeading(Alert.highHeat);
        add(new ScathaProSlider(2, 0, 0, 0, 0, "Trigger Heat Value: ", "", 90, 100, config.getInt(Config.Key.highHeatAlertTriggerValue), false, true, this));
        
        addAlertHeading(Alert.antiSleep);
        add(antiSleepIntervalMinSlider = new ScathaProSlider(3, 0, 0, 0, 0, "Interval Minimum: ", " Minute(s)", 1, 60, config.getInt(Config.Key.antiSleepAlertIntervalMin), false, true, this));
        add(antiSleepIntervalMaxSlider = new ScathaProSlider(4, 0, 0, 0, 0, "Interval Maximum: ", " Minute(s)", 1, 60, config.getInt(Config.Key.antiSleepAlertIntervalMax), false, true, this));
    }
    
    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (!slider.enabled) return;
        switch (slider.id)
        {
            case 1:
                config.set(Config.Key.bedrockWallAlertTriggerDistance, slider.getValueInt());
                break;
            
            case 2:
                config.set(Config.Key.highHeatAlertTriggerValue, slider.getValueInt());
                break;
                
            case 3:
                int newMinValue = slider.getValueInt();
                config.set(Config.Key.antiSleepAlertIntervalMin, newMinValue);
                
                if (antiSleepIntervalMaxSlider.getValue() < slider.getValue())
                {
                    antiSleepIntervalMaxSlider.sliderValue = slider.sliderValue;
                    antiSleepIntervalMaxSlider.updateSliderWithoutEvent();
                    config.set(Config.Key.antiSleepAlertIntervalMax, newMinValue);
                }
                break;
                
            case 4:
                int newMaxValue = slider.getValueInt();
                config.set(Config.Key.antiSleepAlertIntervalMax, newMaxValue);
                
                if (antiSleepIntervalMinSlider.getValue() > slider.getValue())
                {
                    antiSleepIntervalMinSlider.sliderValue = slider.sliderValue;
                    antiSleepIntervalMinSlider.updateSliderWithoutEvent();
                    config.set(Config.Key.antiSleepAlertIntervalMin, newMaxValue);
                }
                break;
                
            case 5:
                config.set(Config.Key.oldLobbyAlertTriggerDay, slider.getValueInt());
                break;
        }
    }
    
    private void add(IGuiElement element)
    {
        listEntries.add(new GuiElementEntry(element));
    }
    
    private void addAlertHeading(Alert alert)
    {
        add(new ScathaProLabel(0, 0, 2, 0, alert.alertName).setCentered());
    }
    
    private class GuiElementEntry extends ListEntry
    {
        public GuiElementEntry(IGuiElement element)
        {
            element.setElementX(0);
            if (element.getElementY() < 0) element.setElementY(0);
            element.setElementWidth(getListWidth());
            element.setElementHeight(20);
            addElement(element);
        }
    }
}
