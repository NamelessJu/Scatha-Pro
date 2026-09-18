package namelessju.scathapro.gui.menus.screens;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ScathaProLayoutScreen;
import namelessju.scathapro.miscellaneous.data.UnconnectedProfileDataImport;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;

public class UnconnectedProfileDataImportScreen extends ScathaProLayoutScreen
{
    private final UnconnectedProfileDataImport profileDataImport;

    private Button confirmButton, cancelButton;
    private int buttonEnableTickTimer = 40;

    public UnconnectedProfileDataImportScreen(ScathaPro scathaPro, Screen parentScreen,
                                              UnconnectedProfileDataImport profileDataImport)
    {
        super(scathaPro, Component.literal("Update Profile Data"), true, parentScreen);
        this.profileDataImport = profileDataImport;
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        MutableComponent description = Component.empty();
        description.append("""
                Found existing data from a previous mod version
                that is not connected to a SkyBlock profile yet:

                """);
        description.append(profileDataImport.getDataPreview()).append("\n");
        description.append("\nDo you want to assign that data to the current profile?");
        layout.addToContents(new MultiLineTextWidget(description, font).setCentered(true));

        LinearLayout footerLayout = LinearLayout.horizontal().spacing(10);
        footerLayout.addChild(
            confirmButton = Button.builder(
                Component.literal("Confirm"),
                _ -> onResponse(true)
            ).width(150).build(),
            LayoutSettings::alignHorizontallyCenter
        );
        footerLayout.addChild(
            cancelButton = Button.builder(
                Component.literal("Cancel"),
                _ -> onResponse(false)
            ).width(150).build(),
            LayoutSettings::alignHorizontallyCenter
        );
        addLayoutFooter(footerLayout);
        confirmButton.active = false;
        cancelButton.active = false;
    }

    @Override
    public void tick()
    {
        if (buttonEnableTickTimer > 0)
        {
            buttonEnableTickTimer --;
            if (buttonEnableTickTimer == 0)
            {
                confirmButton.active = true;
                cancelButton.active = true;
            }
        }
    }

    private void onResponse(boolean shouldImport)
    {
        if (shouldImport) profileDataImport.handleImport(scathaPro);
        else profileDataImport.handleDialogShown(scathaPro);
        onClose();
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }
}