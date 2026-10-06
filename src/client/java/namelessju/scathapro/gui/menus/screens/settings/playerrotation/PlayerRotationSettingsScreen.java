package namelessju.scathapro.gui.menus.screens.settings.playerrotation;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public abstract class PlayerRotationSettingsScreen extends ConfigScreen
{
    private static final Identifier CROSSHAIR_SPRITE = Identifier.withDefaultNamespace("hud/crosshair");

    public PlayerRotationSettingsScreen(ScathaPro scathaPro, String title, Screen parentScreen)
    {
        super(scathaPro, title, parentScreen);
    }

    @Override
    public void extractBackground(@NonNull GuiGraphicsExtractor guiGraphics, int i, int j, float f)
    {
        super.extractBackground(guiGraphics, i, j, f);

        guiGraphics.nextStratum();
        guiGraphics.blitSprite(RenderPipelines.CROSSHAIR, CROSSHAIR_SPRITE,
            (guiGraphics.guiWidth() - 15) / 2, (guiGraphics.guiHeight() - 15) / 2, 15, 15
        );

        scathaPro.crosshairOverlay.renderPreview(guiGraphics, minecraft.getDeltaTracker());
    }
}