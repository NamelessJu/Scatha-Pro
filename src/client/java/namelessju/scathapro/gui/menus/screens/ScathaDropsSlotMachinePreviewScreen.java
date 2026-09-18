package namelessju.scathapro.gui.menus.screens;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ScathaProLayoutScreen;
import namelessju.scathapro.miscellaneous.data.ScathaPetDrop;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class ScathaDropsSlotMachinePreviewScreen extends ScathaProLayoutScreen
{
    public ScathaDropsSlotMachinePreviewScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, Component.literal("Scatha Drops Slot Machine Preview"), true, parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();
        addFooter(doneButton(CommonComponents.GUI_CANCEL, 200));
    }

    @Override
    public void added()
    {
        super.added();

        scathaPro.scathaDropsSlotMachineManager.startRolling();
        scathaPro.scathaDropsSlotMachineManager.setPetDrop(
            new ScathaPetDrop(Rarity.LEGENDARY, false, false), false
        );
    }

    @Override
    public void tick()
    {
        super.tick();

        if (!scathaPro.scathaDropsSlotMachineManager.isRolling())
        {
            onClose();
        }
    }

    @Override
    public void onClose()
    {
        scathaPro.scathaDropsSlotMachineManager.reset();
        super.onClose();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a)
    {
        scathaPro.scathaDropsSlotMachineManager.extractHudRenderState(graphics, minecraft.getDeltaTracker());

        super.extractRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}