package namelessju.scathapro.gui.menus.screens.backups;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.ConfigScreen;
import namelessju.scathapro.gui.menus.framework.widgets.BetterStringWidget;
import namelessju.scathapro.gui.menus.framework.widgets.lists.ScathaProGuiList;
import namelessju.scathapro.gui.menus.framework.widgets.sliders.IntegerSlider;
import namelessju.scathapro.gui.menus.screens.InfoMessageScreen;
import namelessju.scathapro.managers.BackupManager;
import namelessju.scathapro.util.FileUtil;
import namelessju.scathapro.util.TimeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class BackupsScreen extends ConfigScreen
{
    private DirectoryWatcher directoryWatcher;
    private long refreshTickTimer = 0L;

    private Button deleteButton;
    private Button loadButton;

    public BackupsScreen(ScathaPro scathaPro, Screen parentScreen)
    {
        super(scathaPro, "Backups", parentScreen);
        directoryWatcher = DirectoryWatcher.create(scathaPro.backupManager.backupDirectory.toPath());
    }

    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        addTitleHeader(
            booleanConfigButton("Automatic Backups", scathaPro.config.miscellaneous.automaticBackupsEnabled,
                    _ -> Tooltip.create(
                        Component.literal("Creates a full backup of this mod's save files when a new version is installed")
                            .withColor(TextColor.GRAY)
                    ), null
                ),
            new IntegerSlider(
                    0, 0, 150, 20,
                    Component.literal("Max. Full Backups"),
                    2, 25, config.miscellaneous.fullBackupsLimit.get(),
                    value -> config.miscellaneous.fullBackupsLimit.set(value > 2 ? value : 0)
                )
                .setValueComponentSupplier(
                    integer -> integer > 2 ? IntegerSlider.COMPONENT_SUPPLIER.apply(integer) : Component.literal("No Limit")
                )
                .setTooltip(Component.literal("Deletes the oldest full backup(s) once the total amount of full backups exceeds this limit\n"
                        + "(Changing this setting doesn't immediately trigger deletions, it takes effect whenever the next full backup is made)")
                .withColor(TextColor.GRAY))
        );

        List list = new List(minecraft, layout);
        File[] backupFiles = scathaPro.backupManager.backupDirectory.listFiles();
        if (backupFiles != null)
        {
            Arrays.sort(backupFiles, BackupManager.FILE_COMPARATOR_BY_MODIFIED_DESC);
            for (File file : backupFiles) list.addEntry(new BackupEntry(file, list));
        }
        addScrollList(list);

        GridBuilder gridBuilder = new GridBuilder();

        LinearLayout backupActionsLayout = LinearLayout.horizontal().spacing(4);
        int halfCellWidth = (150 - 4) / 2;
        backupActionsLayout.addChild(loadButton = Button.builder(
                Component.literal("Load"), _ -> {
                    if (list.getSelected() instanceof BackupEntry entry) entry.loadBackup();
                }
            ).width(halfCellWidth).build());
        loadButton.active = false;
        backupActionsLayout.addChild(deleteButton = Button.builder(
                Component.literal("Delete"), _ -> {
                    if (list.getSelected() instanceof BackupEntry entry)
                    {
                        minecraft.gui.setScreen(new ConfirmScreen(
                            confirmed -> {
                                Screen targetScreen = this;
                                if (confirmed)
                                {
                                    try
                                    {
                                        Files.delete(entry.file.toPath());
                                    }
                                    catch (IOException e)
                                    {
                                        ScathaPro.LOGGER.error("Exception while trying to delete backup file", e);
                                        targetScreen = new InfoMessageScreen(this,
                                            Component.literal("Deletion Error").withColor(TextColor.RED),
                                            Component.empty()
                                                .append("\"" + entry.file.getName() + "\" couldn't be deleted!\n\n")
                                                .append(Component.literal(e.getMessage()).withColor(TextColor.GRAY))
                                        );
                                    }
                                }
                                minecraft.gui.setScreen(targetScreen);
                            },
                            Component.literal("Delete " + scathaPro.getModDisplayName() + " Backup"),
                            Component.literal("Do you really want to delete \"" + entry.file.getName() + "\"?"),
                            Component.literal("Delete"),
                            CommonComponents.GUI_CANCEL
                        ));
                    }
                }
            ).width(halfCellWidth).build());
        deleteButton.active = false;
        gridBuilder.addSingleCell(backupActionsLayout);

        gridBuilder.addSingleCell(Button.builder(
            Component.literal("Create Backup..."), _ -> minecraft.gui.setScreen(new CreateBackupScreen(scathaPro, this))
        ).build());
        gridBuilder.addSingleCell(Button.builder(
                Component.literal("Open Backups Folder"), _ -> {
                    try
                    {
                        FileUtil.openFileInExplorer(scathaPro.backupManager.backupDirectory);
                    }
                    catch (IOException e)
                    {
                        ScathaPro.LOGGER.error("Failed to open backups directory in explorer", e);
                    }
                }
            ).build());
        gridBuilder.addSingleCell(doneButton(CommonComponents.GUI_BACK, 150));
        addLayoutFooter(gridBuilder.getGrid());
    }

    public void refresh()
    {
        refreshTickTimer = 0L;
        rebuildWidgets();
    }

    @Override
    public void tick()
    {
        if (directoryWatcher != null)
        {
            try
            {
                if (directoryWatcher.pollForChanges())
                {
                    refreshTickTimer = 20L;
                }
            }
            catch (IOException e)
            {
                ScathaPro.LOGGER.warn("Failed to poll for backup directory changes, stopping");
                closeWatcher();
            }
        }

        if (refreshTickTimer > 0L && --refreshTickTimer == 0L)
        {
            refresh();
        }
    }

    @Override
    public void onClose()
    {
        closeWatcher();
        super.onClose();
    }

    private void closeWatcher()
    {
        if (directoryWatcher != null)
        {
            try
            {
                directoryWatcher.close();
                directoryWatcher = null;
            }
            catch (Exception ignored) {}
        }
    }

    private class List extends ScathaProGuiList
    {
        public List(Minecraft minecraft, HeaderAndFooterLayout layout)
        {
            super(minecraft, BackupsScreen.this, layout);
        }

        @Override
        protected boolean entriesCanBeSelected()
        {
            return true;
        }

        @Override
        public void setSelected(Entry entry)
        {
            super.setSelected(entry);

            if (entry instanceof BackupEntry backupEntry)
            {
                loadButton.active = backupEntry.backupType != null;
                deleteButton.active = true;
            }
            else
            {
                loadButton.active = false;
                deleteButton.active = false;
            }
        }
    }

    private class BackupEntry extends ScathaProGuiList.Entry
    {
        public final File file;
        public final BackupManager.BackupType backupType;

        public BackupEntry(File file, ScathaProGuiList list)
        {
            this.file = file;
            this.backupType = BackupManager.getBackupTypeFrom(file);

            Component timestampComponent = Component.literal(TimeUtil.formatDateTime(config, file.lastModified())).withColor(TextColor.GRAY);
            int timestampWidth = font.width(timestampComponent.getVisualOrderText());

            BetterStringWidget fileNameText = new BetterStringWidget(Component.literal(file.getName()), font);
            fileNameText.setMaxWidth(list.getRowWidth() - timestampWidth - 6 - 4, StringWidget.TextOverflow.SCROLLING);
            addPositionedChild(
                3, ScathaProGuiList.DEFAULT_ENTRY_CONTENT_HEIGHT / 2 - font.lineHeight,
                fileNameText
            );

            addPositionedChild(
                list.getRowWidth() - timestampWidth - 3, ScathaProGuiList.DEFAULT_ENTRY_CONTENT_HEIGHT / 2 - font.lineHeight,
                new StringWidget(timestampComponent, font)
            );

            StringWidget typeText = new StringWidget(
                Component.empty().withColor(TextColor.GRAY)
                    .append(backupType != null ? Component.literal(backupType.displayName) : Component.literal("Invalid").withColor(TextColor.RED)),
                font
            );
            if (backupType != null)
            {
                typeText.setTooltip(Tooltip.create(Component.literal(backupType.tooltip).withColor(TextColor.GRAY)));
            }
            addPositionedChild(
                3, ScathaProGuiList.DEFAULT_ENTRY_CONTENT_HEIGHT / 2 + 1,
                typeText
            );
        }

        @Override
        public boolean mouseClicked(final @NonNull MouseButtonEvent event, final boolean doubleClick)
        {
            if (doubleClick) loadBackup();
            return true;
        }

        public void loadBackup()
        {
            if (backupType == null) return;

            minecraft.gui.setScreen(new ConfirmScreen(
                confirmed -> {
                    Screen targetScreen = BackupsScreen.this;
                    if (confirmed)
                    {
                        try
                        {
                            scathaPro.backupManager.loadBackup(file);
                            targetScreen = new InfoMessageScreen(BackupsScreen.this,
                                Component.literal("Backup Loaded"),
                                Component.literal("\"" + file.getName() + "\" successfully loaded!\n\n")
                            );
                        }
                        catch (Exception e)
                        {
                            ScathaPro.LOGGER.error("Exception while trying to load backup file", e);
                            targetScreen = new InfoMessageScreen(BackupsScreen.this,
                                Component.literal("Loading Error").withColor(TextColor.RED),
                                Component.empty()
                                    .append("\"" + file.getName() + "\" couldn't be loaded!\n\n")
                                    .append(Component.literal(e.getMessage()).withColor(TextColor.GRAY))
                            );
                        }
                    }
                    minecraft.gui.setScreen(targetScreen);
                },
                Component.literal("Load " + scathaPro.getModDisplayName() + " Backup"),
                Component.empty()
                    .append("Do you really want to load \"" + file.getName() + "\"?\n")
                    .append(Component.literal("This will irreversibly override the according data!\n").withColor(TextColor.YELLOW))
                    .append("If you're unsure about this you can create another\nbackup first to preserve the current state."),
                Component.literal("Load"),
                CommonComponents.GUI_CANCEL
            ));
        }
    }
}