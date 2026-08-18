package namelessju.scathapro.gui.menus.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphics, int i, int j, float f)
    {
        this.extractDefaultSprite(guiGraphics);
        this.extractDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }

    @Override
    protected void handleCursor(@NonNull GuiGraphicsExtractor guiGraphics)
    {
        // no cursor changes
    }

    @Override
    public void playDownSound(@NonNull SoundManager soundManager)
    {
        // no sound
    }
}