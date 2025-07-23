package namelessju.scathapro.alerts.alertmodes;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.elements.CycleButton.IOption;

public class AlertModeButtonOption implements IOption<AlertMode>
{
    public static AlertModeButtonOption[] getAllOptions()
    {
        AlertMode[] modes = ScathaPro.getInstance().getAlertModeManager().getAllModes();
        AlertModeButtonOption[] options = new AlertModeButtonOption[modes.length];
        for (int i = 0; i < options.length; i ++)
        {
            options[i] = new AlertModeButtonOption(modes[i]);
        }
        return options;
    }
    
    public final AlertMode mode;
    
    public AlertModeButtonOption(AlertMode mode)
    {
        this.mode = mode;
    }
    
    @Override
    public String getOptionName()
    {
        return mode.name;
    }
    
    @Override
    public AlertMode getOptionValue()
    {
        return mode;
    }
}