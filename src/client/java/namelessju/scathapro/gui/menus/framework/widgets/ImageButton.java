package namelessju.scathapro.gui.menus.framework.widgets;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;

public class ImageButton extends Button
{
    private static final float PADDING = 3.75f;
    
    private final ResourceLocation imageIdentifier;
    private final int textureWidth;
    private final int textureHeight;
    
    public ImageButton(int x, int y, int width, int height, String texturePath, int textureWidth, int textureHeight, Button.OnPress onPress)
    {
        super(x, y, width, height, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        imageIdentifier = ScathaPro.getIdentifier("textures/" + texturePath);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f)
    {
        super.renderWidget(guiGraphics, i, j, f);
        
        guiGraphics.nextStratum();
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(
            getX() + getWidth() * 0.5f,
            getY() + getHeight() * 0.5f
        );
        guiGraphics.pose().scale(Math.min(
            (getWidth() - PADDING * 2) / textureWidth,
            (getHeight() - PADDING * 2) / textureHeight
        ));
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, imageIdentifier,
            -textureWidth/2, -textureHeight/2, 0f, 0f,
            textureWidth, textureHeight,
            textureWidth, textureHeight
        );
        guiGraphics.pose().popMatrix();
    }
    
    @Override
    public void renderString(GuiGraphics guiGraphics, Font font, int i)
    {
        // No string for image button
    }
}
