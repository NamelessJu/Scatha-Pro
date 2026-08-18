package namelessju.scathapro.mixin;

import namelessju.scathapro.ScathaPro;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin
{
    @Inject(
        method = "setScreen",
        at = @At("HEAD"),
        cancellable = true
    )
    private void beforeSetScreen(Screen screen, CallbackInfo ci)
    {
        if (ScathaPro.getInstance().scathaDropsSlotMachineManager.shouldHideScreen(screen))
        {
            screen.removed();
            ci.cancel();
        }
    }
}