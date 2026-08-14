package namelessju.scathapro.gui.menus.screens;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FakeBanScreen extends DisconnectedScreen
{
    private final @Nullable Runnable onClose;

    @SuppressWarnings("DataFlowIssue")
    public FakeBanScreen(String reason, @Nullable Runnable onClose)
    {
        super(null, Component.translatable("connect.failed"),
            new DisconnectionDetails(Component.empty().withColor(TextColor.GRAY)
                .append(Component.literal("You are permanently banned from this server!\n").withColor(TextColor.RED))
                .append("\n")
                .append("Reason: ").append(Component.literal(reason).withColor(TextColor.WHITE)).append("\n")
                .append("Find out more: ").append(
                        Component.literal("https://www.hypixel.net/appeal")
                            .withStyle(Style.EMPTY.withColor(TextColor.AQUA).withUnderlined(true))
                    ).append("\n")
                .append("\n")
                .append("Ban ID: ").append(Component.literal("#URB4NN3D").withColor(TextColor.WHITE)).append("\n")
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
    public void extractBackground(@NonNull GuiGraphicsExtractor guiGraphics, int i, int j, float f)
    {
        // Always draw main menu background
        // Note: GuiMixin cancels GUI rendering while this screen is open,
        // otherwise the crosshair would show up in front of the panorama
        this.extractPanorama(guiGraphics, f);
        this.extractBlurredBackground(guiGraphics);
        this.extractMenuBackground(guiGraphics);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}