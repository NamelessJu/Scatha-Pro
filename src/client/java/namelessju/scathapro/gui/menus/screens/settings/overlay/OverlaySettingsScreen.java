package namelessju.scathapro.gui.menus.screens.settings.overlay;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public abstract class OverlaySettingsScreen extends ConfigScreen
{
    public OverlaySettingsScreen(ScathaPro scathaPro, String title, Screen parentScreen)
    {
        super(scathaPro, title, parentScreen);
    }
    
    @Override
    public void renderBackground(@NonNull GuiGraphics guiGraphics, int i, int j, float f)
    {
        super.renderBackground(guiGraphics, i, j, f);
        
        scathaPro.mainOverlay.render(guiGraphics, minecraft.getDeltaTracker());
    }
    
    @Override
    public void tick()
    {
        super.tick();
        
        scathaPro.mainOverlay.requestUpdateTick();
    }
}
