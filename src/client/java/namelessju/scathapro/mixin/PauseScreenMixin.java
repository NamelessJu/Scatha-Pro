package namelessju.scathapro.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.ImageButton;
import namelessju.scathapro.gui.menus.screens.AchievementListScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen
{
    @Unique
    private Button advancementsButton = null;
    @Unique
    private Button scathaProAchievementsButton = null;
    
    protected PauseScreenMixin(Component component)
    {
        super(component);
    }
    
    @ModifyExpressionValue(
        method = "createPauseMenu",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/PauseScreen;openScreenButton(Lnet/minecraft/network/chat/Component;Ljava/util/function/Supplier;)Lnet/minecraft/client/gui/components/Button;",
            ordinal = 0
        )
    )
    private Button getAdvancementsButton(Button button)
    {
        advancementsButton = button;
        return button;
    }
    
    @Inject(method = "createPauseMenu", at = @At("RETURN"))
    private void afterCreatePauseMenu(CallbackInfo ci)
    {
        if (advancementsButton == null) return;
        
        scathaProAchievementsButton = new ImageButton(
            0, 0, 0, 0,
            "screen/achievements/button_icon.png", 64, 64,
            button -> minecraft.setScreen(new AchievementListScreen(ScathaPro.getInstance(), this))
        );
        scathaProAchievementsButton.setTooltip(Tooltip.create(
            Component.literal(ScathaPro.getInstance().getModDisplayName() + " Achievements")
                .withStyle(ChatFormatting.GRAY)
        ));
        addRenderableWidget(scathaProAchievementsButton);
        setAchievementsButtonRectangle();
    }
    
    @Unique
    private void setAchievementsButtonRectangle()
    {
        if (scathaProAchievementsButton == null || advancementsButton == null) return;
        scathaProAchievementsButton.setRectangle(20, 20, advancementsButton.getX() - 24, advancementsButton.getY());
    }
}
