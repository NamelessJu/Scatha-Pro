package namelessju.scathapro.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.miscellaneous.data.mixindata.IKeyBindsListExtraData;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(KeyBindsList.class)
public abstract class KeyBindsListMixin extends ContainerObjectSelectionList<KeyBindsList.Entry>
{
    @Shadow @Final
    private KeyBindsScreen keyBindsScreen;

    // just required for extended class, which in turn is required to be able to access addEntry()
    public KeyBindsListMixin(Minecraft minecraft, int width, int height, int y, int itemHeight)
    {
        super(minecraft, width, height, y, itemHeight);
    }

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;",
            ordinal = 0,
            opcode = Opcodes.GETFIELD
        )
    )
    private KeyMapping[] overrideKeyBindsArray(Options instance, Operation<KeyMapping[]> original)
    {
        if (!((IKeyBindsListExtraData) (keyBindsScreen)).scathapro$isScathaProOnly())
            return original.call(instance);

        return ScathaPro.getInstance().inputManager.getKeyMappings().toArray(KeyMapping[]::new);
    }

    @Inject(
        method = "<init>",
        at = @At("TAIL")
    )
    private void appendAllKeyBindsButton(KeyBindsScreen keyBindsScreen, Minecraft minecraft, CallbackInfo ci)
    {
        if (!((IKeyBindsListExtraData) (keyBindsScreen)).scathapro$isScathaProOnly())
            return;

        addEntry(new SingleWidgetEntry(null));
        addEntry(new SingleWidgetEntry(Button.builder(
            Component.literal("All Key Binds..."),
            _ -> minecraft.gui.setScreen(new KeyBindsScreen(keyBindsScreen, minecraft.options))
        ).build()));
    }


    private static final class SingleWidgetEntry extends KeyBindsList.Entry
    {
        private final @Nullable AbstractWidget widget;

        public SingleWidgetEntry(@Nullable AbstractWidget widget)
        {
            this.widget = widget;
        }

        @Override
        public void refreshEntry() {}

        @Override
        public void extractContent(final @NonNull GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final boolean hovered, final float a)
        {
            if (this.widget == null) return;

            int y = getContentY() - 2;
            widget.setPosition( getContentXMiddle() - widget.getWidth() / 2, y);
            widget.extractRenderState(graphics, mouseX, mouseY, a);
        }

        @Override
        public @NonNull List<? extends GuiEventListener> children()
        {
            return widget != null ? ImmutableList.of(widget) : ImmutableList.of();
        }

        @Override
        public @NonNull List<? extends NarratableEntry> narratables()
        {
            return widget != null ? ImmutableList.of(widget) : ImmutableList.of();
        }
    }
}