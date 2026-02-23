package namelessju.scathapro.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.screens.settings.MainSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsScreen.class)
public abstract class OptionsScreenMixin
{
    @Unique
    private Button scathaProSettingsButton = null;
    
    @Inject(
        method = "init",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToContents(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;",
            ordinal = 0
        )
    )
    private void addModSettingsButton(CallbackInfo ci, @Local(ordinal = 0) GridLayout.RowHelper rowHelper)
    {
        OptionsScreen screen = (OptionsScreen) (Object) this;
        rowHelper.addChild(scathaProSettingsButton = Button.builder(CommonComponents.EMPTY,
                    button -> Minecraft.getInstance().setScreen(new MainSettingsScreen(ScathaPro.getInstance(), screen))
                ).bounds(0, 0, 150, 20).build()
        );
        updateScathaProSettingsButtonText();
    }
    
    @Inject(
        method = "repositionElements",
        at = @At("RETURN")
    )
    private void repositionElements(CallbackInfo ci)
    {
        updateScathaProSettingsButtonText();
    }
    
    @Unique
    private void updateScathaProSettingsButtonText()
    {
        if (scathaProSettingsButton == null) return;
        scathaProSettingsButton.setMessage(Component.literal(ScathaPro.getInstance().getModDisplayName() + " Settings..."));
    }
}
