package namelessju.scathapro.gui.elements;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.Config;

public class ScappaModeButton extends ScathaProButton implements IClickActionButton
{
    public ScappaModeButton(int buttonId, int x, int y, int widthIn, int heightIn)
    {
        super(buttonId, x, y, widthIn, heightIn, "");
        
        updateText();
    }
    
    @Override
    public void click()
    {
        ScathaPro scathaPro = ScathaPro.getInstance();
        
        if (scathaPro.variables.scappaModeActiveTemp)
        {
            scathaPro.variables.scappaModeActiveTemp = false;
        }
        else
        {
            Config config = scathaPro.getConfig();
            config.set(Config.Key.scappaMode, !config.getBoolean(Config.Key.scappaMode));
        }
        
        ScathaPro.getInstance().getOverlay().updateScappaMode();
        
        updateText();
    }
    
    private void updateText()
    {
        this.displayString = "Scappa Mode: ";
        
        if (ScathaPro.getInstance().variables.scappaModeActiveTemp)
        {
            this.displayString += "Temporary";
            return;
        }
        
        boolean enabled = ScathaPro.getInstance().getConfig().getBoolean(Config.Key.scappaMode);
        this.displayString += enabled ? "ON" : "OFF";
    }
}
