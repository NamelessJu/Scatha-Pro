package namelessju.scathapro.miscellaneous.data;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record Texture(Identifier identifier, int width, int height)
{
    public Texture
    {
        if (width < 1 || height < 1) throw new IllegalArgumentException("Texture width or height cannot be less than 1");
    }

    public void extract(GuiGraphicsExtractor guiGraphicsExtractor, int x, int y, int renderWidth, int renderHeight, int color)
    {
        extract(guiGraphicsExtractor, x, y, renderWidth, renderHeight, 0f, 0f, this.width, this.height, color);
    }

    public void extract(GuiGraphicsExtractor guiGraphicsExtractor,
                        int x, int y, int renderWidth, int renderHeight,
                        float u, float v, int uvWidth, int uvHeight,
                        int color)
    {
        guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, this.identifier,
            x, y, u, v,
            renderWidth, renderHeight, uvWidth, uvHeight, this.width, this.height,
            color
        );
    }

    public void extractAnimated(GuiGraphicsExtractor guiGraphicsExtractor,
                        int x, int y, int renderWidth, int renderHeight,
                        float v, int frameCount,
                        int color)
    {
        guiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, this.identifier,
            x, y, 0f, v,
            renderWidth, renderHeight, this.width, this.height, this.width, this.height * frameCount,
            color
        );
    }

    public static Texture scathaPro(String subPath, int width, int height)
    {
        return new Texture(ScathaPro.getIdentifier("textures/" + subPath), width, height);
    }
}