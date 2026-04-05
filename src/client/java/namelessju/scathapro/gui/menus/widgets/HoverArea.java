package namelessju.scathapro.gui.menus.widgets;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class HoverArea extends Button
{
    public HoverArea(int x, int y, int width, int height, Component component)
    {
        super(x, y, width, height, component, button -> {}, Button.DEFAULT_NARRATION);
    }
    
    @Override
    protected void renderContents(@NonNull GuiGraphics guiGraphics, int i, int j, float f)
    {
        this.renderDefaultSprite(guiGraphics);
        this.renderDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphics.HoveredTextEffects.NONE));
    }
    
    @Override
    protected void handleCursor(@NonNull GuiGraphics guiGraphics)
    {
        // no cursor changes
    }
    
    @Override
    public void playDownSound(@NonNull SoundManager soundManager)
    {
        // no sound
    }
}
