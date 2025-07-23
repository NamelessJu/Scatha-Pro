package namelessju.scathapro.managers;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import namelessju.scathapro.GlobalVariables;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.overlay.Overlay;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;

public class ScreenshotManager
{
    /**
     * Takes a screenshot of a certain area in the scaled resolution used by UI
     */
    public static BufferedImage takePartialScreenshotScaled(int x, int y, int width, int height)
    {
        int scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        return takePartialScreenshot(x * scale, y * scale, width * scale, height * scale);
    }

    /**
     * Takes a screenshot of a certain area in the screen resolution
     */
    public static BufferedImage takePartialScreenshot(int x, int y, int width, int height)
    {
        try
        {
            int pixelAmount = width * height;
            IntBuffer pixelBuffer = BufferUtils.createIntBuffer(pixelAmount);
            int[] pixelValues = new int[pixelAmount];
            
            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            GL11.glReadPixels(x, Minecraft.getMinecraft().displayHeight - y - height, width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
            
            pixelBuffer.get(pixelValues);
            TextureUtil.processPixelValues(pixelValues, width, height);
            
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            bufferedImage.setRGB(0, 0, width, height, pixelValues, 0, width);
            return bufferedImage;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void takeOverlayScreenshot()
    {
        Overlay overlay = ScathaPro.getInstance().getOverlay();
        
        if (!overlay.isVisible() || !overlay.isOverlayDrawAllowed())
        {
            TextUtil.sendModErrorMessage("Cannot take overlay screenshot while overlay isn't visible");
            return;
        }
        
        BufferedImage screenshot = takePartialScreenshotScaled(overlay.getX(), overlay.getY(), overlay.getScaledWidth(), overlay.getScaledHeight());
        if (screenshot != null)
        {
            Util.copyToClipboard(screenshot);
            TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Overlay screenshot saved to clipboard");
        }
        else TextUtil.sendModErrorMessage("Failed to take overlay screenshot");
    }
    
    public static void takeChatScreenshot()
    {
        GuiIngame guiIngame = Minecraft.getMinecraft().ingameGUI;
        GuiNewChat chat = guiIngame.getChatGUI();
        
        chat.resetScroll();
        
        GlobalVariables variables = ScathaPro.getInstance().variables;
        variables.runAfterNextRender.add(() -> {
            int chatHeightUnscaled;
            if (variables.chatLines != null)
            {
                chatHeightUnscaled = 0;
                for (int i = 0; i < variables.chatLines.size() && i < chat.getLineCount(); i ++)
                {
                    ChatLine line = variables.chatLines.get(i);
                    if (chat.getChatOpen() || guiIngame.getUpdateCounter() - line.getUpdatedCounter() < 200)
                    {
                        chatHeightUnscaled += 9;
                    }
                    else break;
                }                
            }
            else chatHeightUnscaled = chat.getChatHeight();
            
            if (chatHeightUnscaled <= 0)
            {
                TextUtil.sendModErrorMessage("Cannot take chat screenshot while no message is visible without the chat being open");
                return;
            }
            
            ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
            int scaledHeight = scaledResolution.getScaledHeight();
            int scaleFactor = scaledResolution.getScaleFactor();
            
            float chatScale = chat.getChatScale();
            int chatHeightFullyScaled = MathHelper.ceiling_float_int(((float) chatHeightUnscaled * chatScale) * scaleFactor);
            
            BufferedImage screenshot = takePartialScreenshot(2 * scaleFactor, (scaledHeight - 28) * scaleFactor - chatHeightFullyScaled, (chat.getChatWidth() + 4) * scaleFactor, chatHeightFullyScaled);
            if (screenshot != null)
            {
                Util.copyToClipboard(screenshot);
                TextUtil.sendModChatMessage(EnumChatFormatting.GRAY + "Chat screenshot saved to clipboard");
            }
            else TextUtil.sendModErrorMessage("Failed to take chat screenshot");
        });
    }
    
    
    private ScreenshotManager() {}
}
