package namelessju.scathapro.util;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.overlay.MainOverlay;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.function.Consumer;

@Deprecated
public final class PartialScreenshot
{
    private PartialScreenshot() {}
    
    /**
     * Takes a screenshot of a certain area in the scaled resolution used by UI
     */
    public static void takePartialGuiScreenshot(Minecraft minecraft, int x, int y, int width, int height, Consumer<NativeImage> consumer)
    {
        int scale = minecraft.getWindow().getGuiScale();
        takePartialScreenshot(
            minecraft.getMainRenderTarget(),
            x * scale, y * scale, width * scale, height * scale,
            consumer
        );
    }

    /**
     * Takes a screenshot of a certain area in the screen resolution
     */
    public static void takePartialScreenshot(RenderTarget renderTarget, int x, int y, int width, int height, Consumer<NativeImage> consumer)
    {
        int screenWidth = renderTarget.width;
        int screenHeight = renderTarget.height;
        if (x < 0 || y < 0 ||
            x + width > screenWidth ||
            y + height > screenHeight) {
            throw new IllegalArgumentException("Sub-rectangle outside of framebuffer bounds");
        }
        GpuTexture gpuTexture = renderTarget.getColorTexture();
        if (gpuTexture == null)
        {
            throw new IllegalStateException("Tried to capture screenshot of an incomplete framebuffer");
        }
        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(
            () -> "Screenshot buffer", 9,
            (long) screenWidth * screenHeight * gpuTexture.getFormat().pixelSize()
        );
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        commandEncoder.copyTextureToBuffer(gpuTexture, gpuBuffer, 0L, () -> {
            try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(gpuBuffer, true, false))
            {
                NativeImage screenshot = new NativeImage(width, height, false);
                
                for (int pixelY = 0; pixelY < height; pixelY++)
                {
                    for (int pixelX = 0; pixelX < width; pixelX++)
                    {
                        int pixelColorABGR = mappedView.data().getInt(((x + pixelX) + (screenHeight - (y + pixelY) - 1) * screenWidth) * gpuTexture.getFormat().pixelSize());
                        screenshot.setPixelABGR(pixelX, pixelY, pixelColorABGR);
                    }
                }
                
                consumer.accept(screenshot);
            }
            
            gpuBuffer.close();
        }, 0);
    }
    
    public static void takeOverlayScreenshot(ScathaPro scathaPro)
    {
        MainOverlay overlay = scathaPro.mainOverlay;
        
        if (!overlay.isVisible() || !overlay.isOverlayRenderAllowed())
        {
            scathaPro.chatManager.sendErrorChatMessage("Cannot take overlay screenshot while overlay isn't visible");
            return;
        }
        
        takePartialGuiScreenshot(
            scathaPro.minecraft,
            overlay.getX(), overlay.getY(), overlay.getScaledWidth(), overlay.getScaledHeight(),
            saveToFileConsumer(scathaPro, ScathaPro.MOD_NAME + "_Overlay")
        );
    }
    
    public static void takeChatScreenshot(ScathaPro scathaPro, @Nullable String screenshotNamePart)
    {
        // TODO:
        
        scathaPro.chatManager.sendChatMessage(Component.literal("Work in Progress").withStyle(ChatFormatting.YELLOW));
        
        /*
        takePartialGuiScreenshot(
            scathaPro.minecraft,
            0, 0, 100, 100,
            saveToFileConsumer(scathaPro,
                ScathaPro.MOD_NAME + "_" + (screenshotNamePart != null ? screenshotNamePart : "Chat")
            )
        );
        */
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Consumer<NativeImage> saveToFileConsumer(@NonNull ScathaPro scathaPro, @NonNull String fileName)
    {
        return nativeImage -> {
            File screenshotsDirectory = new File(scathaPro.minecraft.gameDirectory, "screenshots");
            screenshotsDirectory.mkdir();
            File screenshotFile = FileUtil.getUniqueFile(screenshotsDirectory,
                fileName + "_" + Util.getFilenameFormattedDateTime() + ".png"
            );
            
            Util.ioPool()
                .execute(() -> {
                    try
                    {
                        try
                        {
                            nativeImage.writeToFile(screenshotFile);
                            scathaPro.chatManager.sendChatMessage(
                                Component.translatable("screenshot.success", Component.literal(screenshotFile.getName())
                                .withStyle(ChatFormatting.UNDERLINE)
                                .withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(screenshotFile.getAbsoluteFile())))
                            ));
                        }
                        catch (Throwable e)
                        {
                            if (nativeImage != null)
                            {
                                try
                                {
                                    nativeImage.close();
                                }
                                catch (Throwable e2)
                                {
                                    e.addSuppressed(e2);
                                }
                            }
                            
                            throw e;
                        }
                        
                        nativeImage.close();
                    }
                    catch (Exception e)
                    {
                        ScathaPro.LOGGER.warn("Couldn't save screenshot", e);
                        scathaPro.chatManager.sendChatMessage(Component.translatable("screenshot.failure", e.getMessage()));
                    }
                });
        };
    }
}
