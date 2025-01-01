package com.namelessju.scathapro.miscellaneous;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class StackedScissorCheck
{
    private static class ScissorCheck
    {
        public final int x, y, width, height;
        
        public ScissorCheck(int x, int y, int width, int height)
        {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
    
    private static int currentIndex = -1;
    private static ScissorCheck[] stack = new ScissorCheck[4];
    
    public static void pushCheck(int x, int y, int width, int height)
    {
        if (currentIndex >= stack.length - 1) throw new IllegalStateException("Max scissor stack size (" + stack.length + ") exceeded.");
        currentIndex ++;
        stack[currentIndex] = new ScissorCheck(x, y, width, height);
        updateScissor();
    }
    
    public static void popCheck()
    {
        if (currentIndex < 0) return;
        stack[currentIndex] = null;
        currentIndex --;
        updateScissor();
    }
    
    public static void clearStack()
    {
        for (int i = 0; i < stack.length; i ++)
        {
            stack[i] = null;
        }
        currentIndex = -1;
        updateScissor();
    }
    
    private static void updateScissor()
    {
        if (currentIndex < 0)
        {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            return;
        }

        ScissorCheck checkCombination = null;
        for (int i = 0; i <= currentIndex; i ++)
        {
            ScissorCheck nextCheck = stack[i];
            
            if (checkCombination == null)
            {
                checkCombination = nextCheck;
                continue;
            }
            
            int dx1 = checkCombination.x + checkCombination.width;
            int dy1 = checkCombination.y + checkCombination.height;
            int dx2 = nextCheck.x + nextCheck.width;
            int dy2 = nextCheck.y + nextCheck.height;
            
            int xMax = Math.max(checkCombination.x, nextCheck.x);
            int yMax = Math.max(checkCombination.y, nextCheck.y);
            int dxMin = Math.min(dx1, dx2);
            int dyMin = Math.min(dy1, dy2);
            
            checkCombination = new ScissorCheck(xMax, yMax, Math.max(xMax, dxMin) - xMax, Math.max(yMax, dyMin) - yMax);
        }
        
        if (checkCombination == null)
        {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            return;
        }
        
        int scaleFactor = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(checkCombination.x * scaleFactor, Minecraft.getMinecraft().displayHeight - (checkCombination.y + checkCombination.height) * scaleFactor, checkCombination.width * scaleFactor, checkCombination.height * scaleFactor);
    }
    
    
    private StackedScissorCheck() {}
}
