package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin
{
    @Shadow
    private @Nullable Screen screen;

    @Inject(
        method = "setScreen",
        at = @At("RETURN")
    )
    private void afterSetScreen(CallbackInfo ci)
    {
        if (screen == null) return;

        if (ScathaPro.getInstance().scathaDropsSlotMachineManager.shouldHideScreen(screen))
        {
            screen.onClose();
        }
    }
}