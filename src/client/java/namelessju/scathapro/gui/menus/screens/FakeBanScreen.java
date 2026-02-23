package namelessju.scathapro.gui.menus.screens;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FakeBanScreen extends DisconnectedScreen
{
    private final @Nullable Runnable onClose;
    
    @SuppressWarnings("DataFlowIssue")
    public FakeBanScreen(String reason, @Nullable Runnable onClose)
    {
        super(null, Component.translatable("connect.failed"),
            new DisconnectionDetails(Component.empty().withStyle(ChatFormatting.GRAY)
                .append(Component.literal("You are permanently banned from this server!\n").withStyle(ChatFormatting.RED))
                .append("\n")
                .append("Reason: ").append(Component.literal(reason).withStyle(ChatFormatting.WHITE)).append("\n")
                .append("Find out more: ").append(
                        Component.literal("https://www.hypixel.net/appeal")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE)
                    ).append("\n")
                .append("\n")
                .append("Ban ID: ").append(Component.literal("#URB4NN3D").withStyle(ChatFormatting.WHITE)).append("\n")
                .append("Sharing your Ban ID may affect the processing of your appeal!")
            ),
            Component.translatable("gui.toMenu")
        );
        this.onClose = onClose;
    }
    
    @Override
    protected void init()
    {
        super.init();
        
        minecraft.getSoundManager().stop();
    }
    
    public void removed()
    {
        super.removed();
        
        if (onClose != null) onClose.run();
    }
    
    @Override
    public void renderBackground(@NonNull GuiGraphics guiGraphics, int i, int j, float f)
    {
        // Always draw main menu background
        // Note: GuiMixin cancels GUI rendering while this screen is open,
        // otherwise the crosshair would show up in front of the panorama
        this.renderPanorama(guiGraphics, f);
        this.renderBlurredBackground(guiGraphics);
        this.renderMenuBackground(guiGraphics);
    }
    
    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
