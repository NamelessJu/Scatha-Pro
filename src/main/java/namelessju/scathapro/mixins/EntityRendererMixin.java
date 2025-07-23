package namelessju.scathapro.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.Config;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin
{
    @Redirect(method = {"updateCameraAndRender", "updateRenderer"}, at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;mouseSensitivity:F"))
    private float redirectMouseSensitivity(GameSettings settings)
    {
        boolean isKeyDown = ScathaPro.getInstance().getInputManager().alternativeSensitivityKeybinding.isKeyDown();
        return isKeyDown
            ? (float) ScathaPro.getInstance().getConfig().getDouble(Config.Key.alternativeSensitivity)
            : settings.mouseSensitivity;
    }
}
