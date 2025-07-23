package namelessju.scathapro.gui.menus;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.elements.BooleanSettingButton;
import namelessju.scathapro.gui.elements.ScathaProSlider;
import namelessju.scathapro.managers.Config;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.client.config.GuiSlider;

public class SoundSettingsGui extends ConfigGui implements GuiSlider.ISlider
{
    private BooleanSettingButton muteCHSoundsButton, keepDragonLairSoundsButton;
    
    public SoundSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Sound Settings";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        addGridButton(new ScathaProSlider(1, 0, 0, 0, 0, ScathaPro.DYNAMIC_MODNAME + " Sounds Volume: ", "%", 0, 100, scathaPro.getConfig().getDouble(Config.Key.soundsVolume) * 100, false, true, this), GridElementMode.FULL_WIDTH);
        addGridGap();
        addGridButton(muteCHSoundsButton = new BooleanSettingButton(2, 0, 0, 0, 0, "Mute Crystal Hollows Sounds", Config.Key.muteCrystalHollowsSounds), GridElementMode.FULL_WIDTH);
        addGridButton(keepDragonLairSoundsButton = new BooleanSettingButton(3, 0, 0, 0, 0, "Keep Golden Dragon's Lair Sounds", Config.Key.keepDragonLairSounds), GridElementMode.FULL_WIDTH);
        updateMuteCHSoundsButtons();
        
        addDoneButton();
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        switch (button.id)
        {
            case 2:
                updateMuteCHSoundsButtons();
                break;
        }
    }
    
    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (slider.enabled)
        {
            switch (slider.id)
            {
                case 1:
                    double volume = (double) slider.getValueInt() / 100;
                    
                    Config config = scathaPro.getConfig();
                    config.set(Config.Key.soundsVolume, volume);
                    break;
            }
        }
    }
    
    private void updateMuteCHSoundsButtons()
    {
        if (muteCHSoundsButton.isSettingEnabled())
        {
            keepDragonLairSoundsButton.enabled = true;
            keepDragonLairSoundsButton.getTooltip().setText(null);
        }
        else
        {
            keepDragonLairSoundsButton.enabled = false;
            keepDragonLairSoundsButton.getTooltip().setText(EnumChatFormatting.YELLOW + "Applies only when Crystal\nHollows sounds are muted");
        }
    }
}
