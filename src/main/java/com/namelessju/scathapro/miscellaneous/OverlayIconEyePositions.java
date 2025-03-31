package com.namelessju.scathapro.miscellaneous;

import net.minecraft.util.MathHelper;

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
        this(new OverlayIconEyePositions.EyePosition(leftX, leftY), null);
    }
    public OverlayIconEyePositions(float leftX, float leftY, float rightX, float rightY)
    {
        this(new OverlayIconEyePositions.EyePosition(leftX, leftY), new OverlayIconEyePositions.EyePosition(rightX, rightY));
    }
    
    private OverlayIconEyePositions(EyePosition leftEyePosition, EyePosition rightEyePosition)
    {
        this.leftEyePosition = leftEyePosition;
        this.rightEyePosition = rightEyePosition;
    }
    
    public static class EyePosition
    {
        public final float x;
        public final float y;
        
        private EyePosition(float x, float y)
        {
            this.x = MathHelper.clamp_float(x, 0f, 1f);
            this.y = MathHelper.clamp_float(y, 0f, 1f);
        }
    }
}
