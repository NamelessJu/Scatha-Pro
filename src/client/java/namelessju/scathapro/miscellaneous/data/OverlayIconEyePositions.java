package namelessju.scathapro.miscellaneous.data;

import net.minecraft.util.Mth;

public class OverlayIconEyePositions
{
    public final EyePosition leftEyePosition;
    public final EyePosition rightEyePosition;

    
    public OverlayIconEyePositions()
    {
        this(null, null);
    }
    public OverlayIconEyePositions(float leftX, float leftY)
    {
        this(new EyePosition(leftX, leftY), null);
    }
    public OverlayIconEyePositions(float leftX, float leftY, float rightX, float rightY)
    {
        this(new EyePosition(leftX, leftY), new EyePosition(rightX, rightY));
    }
    
    private OverlayIconEyePositions(EyePosition leftEyePosition, EyePosition rightEyePosition)
    {
        this.leftEyePosition = leftEyePosition;
        this.rightEyePosition = rightEyePosition;
    }
    
    public record EyePosition(float x, float y)
    {
        public EyePosition(float x, float y)
        {
            this.x = Mth.clamp(x, 0f, 1f);
            this.y = Mth.clamp(y, 0f, 1f);
        }
    }
}
