package namelessju.scathapro.gui.overlay.elements;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.UnknownNullability;

public class TunnelVisionEffectProgressBar extends OverlayElement
{
    private static final Identifier textureIdentifier = ScathaPro.getIdentifier("textures/overlay/tunnel_vision_progress_bar.png");
    
    private final int width;
    private final int height;
    
    private float progress = 0f;
    
    public TunnelVisionEffectProgressBar(int x, int y, int width, int height, float scale)
    {
        super(x, y, scale);
        this.width = width;
        this.height = height;
    }
    
    @Override
    protected void extractContent(@UnknownNullability GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker)
    {
        int yOffset = (int) ((TimeUtil.getEpochMilliseconds() / 100L) % 32);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, textureIdentifier,
            0, 0, 0f, yOffset,
            Math.round(width * progress), height, 32, 32
        );
    }
    
    @Override
    public int getWidth()
    {
        return width;
    }
    
    @Override
    public int getHeight()
    {
        return height;
    }
    
    public void setProgress(float progress)
    {
        this.progress = Mth.clamp(progress, 0f, 1f);
    }
}
