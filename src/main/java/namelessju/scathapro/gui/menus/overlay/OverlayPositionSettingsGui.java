package namelessju.scathapro.gui.menus.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.elements.CycleButton;
import namelessju.scathapro.gui.elements.ScathaProSlider;
import namelessju.scathapro.managers.Config;
import namelessju.scathapro.overlay.elements.OverlayElement;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiSlider;

public class OverlayPositionSettingsGui extends OverlaySettingsGui implements GuiSlider.ISlider
{
    public OverlayPositionSettingsGui(ScathaPro scathaPro, GuiScreen parentGui)
    {
        super(scathaPro, parentGui);
    }
    
    @Override
    public String getTitle()
    {
        return "Overlay Position";
    }
    
    @Override
    public void initGui()
    {
        super.initGui();
        
        double overlayX = config.getDouble(Config.Key.overlayX);
        ScathaProSlider overlayXSlider = new ScathaProSlider(1, width / 2 - 155, height - 45 - 24 - 6, 150, 20, "X Position: ", "%", -1, 100, overlayX >= 0 ? overlayX * 100 : -1, false, true, this);
        if (overlayX < 0) setSliderTextDefault(overlayXSlider);
        elements.add(overlayXSlider);
        
        double overlayY = config.getDouble(Config.Key.overlayY);
        ScathaProSlider overlayYSlider = new ScathaProSlider(2, width / 2 + 5, height - 45 - 24 - 6, 150, 20, "Y Position: ", "%", -1, 100, overlayY >= 0 ? overlayY * 100 : -1, false, true, this);
        if (overlayY < 0) setSliderTextDefault(overlayYSlider);
        elements.add(overlayYSlider);
        
        elements.add(new ScathaProSlider(3, width / 2 - 155, height - 45 - 48 - 6, 310, 20, "Scale: ", "%", 25, 175, config.getDouble(Config.Key.overlayScale) * 100, false, true, this));
        
        CycleButton<OverlayElement.Alignment> alignmentButton = new CycleButton<OverlayElement.Alignment>(4, width / 2 - 155, height - 45 - 72 - 6, 310, 20, "Alignment", CycleButton.EnumOption.from(OverlayElement.Alignment.class, true), config.getEnum(Config.Key.overlayAlignment, OverlayElement.Alignment.class), button -> {
            OverlayElement.Alignment value = button.getSelectedValue();
            config.set(Config.Key.overlayAlignment, value != null ? value.name() : "");
            
            scathaPro.getOverlay().updateContentAlignment();
        });
        alignmentButton.setNullOptionName("Automatic");
        elements.add(alignmentButton);
        
        addDoneButton(width / 2 - 100, height - 45, 200, 20);
        
        overlay.updateOverlayFull();
    }
    
    @Override
    public void onChangeSliderValue(GuiSlider slider)
    {
        if (!slider.enabled) return;
        switch (slider.id)
        {
            case 1:
                double overlayX = (double) slider.getValueInt() / 100;
                config.set(Config.Key.overlayX, overlayX >= 0 ? overlayX : -1);

                overlay.updatePosition();

                if (overlayX < 0) setSliderTextDefault(slider);
                break;

            case 2:
                double overlayY = (double) slider.getValueInt() / 100;
                config.set(Config.Key.overlayY, overlayY >= 0 ? overlayY : -1);

                overlay.updatePosition();

                if (overlayY < 0) setSliderTextDefault(slider);
                break;

            case 3:
                config.set(Config.Key.overlayScale, (double) slider.getValueInt() / 100);

                overlay.updateScale();
                overlay.updatePosition();
                break;
        }
    }
    
}
