package namelessju.scathapro.gui.menus.framework.widgets;

import namelessju.scathapro.miscellaneous.data.Texture;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import org.jspecify.annotations.NonNull;

public class ImageButton extends Button
{
    private static final float PADDING = 3.75f;

    private final Texture texture;

    public ImageButton(int x, int y, int width, int height, Texture texture, Button.OnPress onPress)
    {
        super(x, y, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.texture = texture;
    }

    @Override
    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphics, int i, int j, float f)
    {
        this.extractDefaultSprite(guiGraphics);

        guiGraphics.nextStratum();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(
            getX() + getWidth() * 0.5f,
            getY() + getHeight() * 0.5f
        );
        guiGraphics.pose().scale(Math.min(
            ((float) getWidth() - PADDING * 2f) / texture.width(),
            ((float) getHeight() - PADDING * 2f) / texture.height()
        ));
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture.identifier(),
            -texture.width()/2, -texture.height()/2, 0f, 0f,
            texture.width(), texture.height(),
            texture.width(), texture.height()
        );
        guiGraphics.pose().popMatrix();
    }
}