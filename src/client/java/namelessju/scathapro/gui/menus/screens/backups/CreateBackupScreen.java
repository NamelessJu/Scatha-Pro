package namelessju.scathapro.gui.menus.screens.backups;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ScathaProLayoutScreen;
import namelessju.scathapro.managers.BackupManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;

public class CreateBackupScreen extends ScathaProLayoutScreen
{
    public CreateBackupScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, Component.literal("Create Backup"), true, parentScreen);
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader();

        GridBuilder gridBuilder = new GridBuilder();
        gridBuilder.addFullWidth(Button.builder(
            Component.literal("Create Full Backup"), _ -> {
                scathaPro.persistentData.save();
                scathaPro.config.save();
                scathaPro.backupManager.backup(null, false);
                onClose();
            }
        ).tooltip(Tooltip.create(Component.literal(BackupManager.BackupType.FULL.tooltip).withColor(TextColor.GRAY))).build());
        gridBuilder.addFullWidth(Button.builder(
            Component.literal("Create Persistent Data Backup"), _ -> {
                scathaPro.persistentData.save();
                scathaPro.backupManager.backupPersistentData(null, false);
                onClose();
            }
        ).tooltip(Tooltip.create(Component.literal(BackupManager.BackupType.PERSISTENT_DATA.tooltip).withColor(TextColor.GRAY))).build());
        layout.addToContents(gridBuilder.getGrid());

        addFooter(doneButton(CommonComponents.GUI_CANCEL, 200));
    }
}