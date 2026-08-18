package namelessju.scathapro.gui.menus.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ItemWidget extends AbstractWidget
{
    private @Nullable ItemStack itemStack;

    public ItemWidget(int x, int y, int width, int height, @Nullable ItemStack itemStack)
    {
        super(x, y, width, height, Component.empty());
        setItem(itemStack);
    }

    public void setItem(@Nullable ItemStack itemStack)
    {
        this.itemStack = itemStack;
        this.message = itemStack != null && !itemStack.isEmpty() ? itemStack.getHoverName() : Component.literal("Empty");
    }

    @Override
    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        if (itemStack == null || itemStack.isEmpty()) return;

        graphics.pose().pushMatrix();
        graphics.pose().translate( getX() + getWidth()*0.5f, getY() + getHeight()*0.5f);
        graphics.pose().scale(Math.min(getWidth()/16f, getHeight()/16f));
        graphics.pose().translate(-8f, -8f);
        graphics.fakeItem(itemStack, 0, 0);
        graphics.pose().popMatrix();
    }

    @Override
    protected void updateWidgetNarration(final NarrationElementOutput output)
    {
        output.add(NarratedElementType.TITLE, Component.translatable("narration.item", this.message));
    }

    @Override
    public void playDownSound(final @NonNull SoundManager soundManager) {}
}