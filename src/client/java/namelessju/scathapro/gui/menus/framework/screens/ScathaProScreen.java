package namelessju.scathapro.gui.menus.framework.screens;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.widgets.KeyMappingButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class ScathaProScreen extends Screen
{
    protected final ScathaPro scathaPro;
    protected final Screen parentScreen;
    protected final boolean addModTitle;
    protected final StringWidget titleWidget;

    public ScathaProScreen(ScathaPro scathaPro, Component titleComponent, boolean addModTitle, Screen parentScreen)
    {
        super(titleComponent);
        this.scathaPro = scathaPro;
        this.parentScreen = parentScreen;
        this.addModTitle = addModTitle;
        titleWidget = new StringWidget(CommonComponents.EMPTY, font);
    }

    @Override
    public @NonNull Component getTitle()
    {
        return addModTitle
            ? Component.empty().append(scathaPro.getModDisplayName() + " - ").append(super.getTitle())
            : super.getTitle();
    }

    public void updateTitleWidget()
    {
        titleWidget.setMessage(getTitle());
    }

    @Override
    public void added()
    {
        updateTitleWidget();
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return !KeyMappingButton.isAnySelected();
    }

    @Override
    public void onClose()
    {
        KeyMappingButton.clearSelection();
        minecraft.gui.setScreen(parentScreen);
    }


    public static Component sectionHeaderStyle(Component component)
    {
        //? if >= 26.3 {
        return Component.empty().append(component).setStyle(Style.EMPTY.withBold(true).withUnderlined(true));
        //? } else {
        /*return component;
         *///? }
    }

    public StringWidget label(int x, int y, Component component)
    {
        StringWidget label = new StringWidget(component, font);
        label.setPosition(x, y);
        return label;
    }

    private static final CycleButton.ValueListSupplier<Boolean> BOOLEAN_VALUES_SUPPLIER = CycleButton.ValueListSupplier.create(List.of(true, false));
    public static CycleButton.Builder<Boolean> booleanButtonBuilder(boolean initialValue)
    {
        return CycleButton.builder(
            value -> value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF,
                initialValue
            )
            .withValues(BOOLEAN_VALUES_SUPPLIER);
    }

    public Button subScreenButton(String text, BiFunction<ScathaPro, Screen, Screen> screenConstructor)
    {
        return subScreenButtonBuilder(Component.literal(text), () -> screenConstructor.apply(scathaPro, this)).build();
    }

    public Button.Builder subScreenButtonBuilder(Component component, Supplier<Screen> screenSupplier)
    {
        return Button.builder(component, _ -> scathaPro.minecraft.gui.setScreen(screenSupplier.get()));
    }

    public Button doneButton()
    {
        return doneButton(CommonComponents.GUI_DONE, 200);
    }

    public Button doneButton(Component component, int width)
    {
        return Button.builder(component, _ -> onClose()).width(width).build();
    }

    public static <T> Collection<Optional<T>> getNullableOptions(T[] options)
    {
        List<Optional<T>> list = Lists.newArrayList();
        list.add(Optional.empty());
        for (T value : options) list.add(Optional.ofNullable(value));
        return list;
    }


    public static class DirectoryWatcher implements AutoCloseable
    {
        private final WatchService watcher;
        private final Path packPath;

        private DirectoryWatcher(Path path) throws IOException
        {
            this.packPath = path;
            this.watcher = path.getFileSystem().newWatchService();

            try
            {
                this.watch(path);
                DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);

                try
                {
                    for (Path childPath : directoryStream)
                    {
                        if (Files.isDirectory(childPath, LinkOption.NOFOLLOW_LINKS))
                        {
                            this.watch(childPath);
                        }
                    }
                }
                catch (Throwable e)
                {
                    try
                    {
                        directoryStream.close();
                    }
                    catch (Throwable e2)
                    {
                        e.addSuppressed(e2);
                    }

                    throw e;
                }

                directoryStream.close();
            }
            catch (Exception e)
            {
                this.watcher.close();
                throw e;
            }
        }

        public static @Nullable DirectoryWatcher create(Path path)
        {
            try
            {
                return new DirectoryWatcher(path);
            }
            catch (IOException e)
            {
                ScathaPro.LOGGER.warn("Failed to initialize DirectoryWatcher for {}", path, e);
                return null;
            }
        }

        private void watch(Path path) throws IOException
        {
            path.register(this.watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
            );
        }

        public boolean pollForChanges() throws IOException
        {
            boolean changesFound = false;

            WatchKey watchKey;
            while ((watchKey = this.watcher.poll()) != null)
            {
                for (WatchEvent<?> watchEvent : watchKey.pollEvents())
                {
                    changesFound = true;
                    if (watchKey.watchable() == this.packPath && watchEvent.kind() == StandardWatchEventKinds.ENTRY_CREATE)
                    {
                        Path path = this.packPath.resolve((Path) watchEvent.context());
                        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
                        {
                            this.watch(path);
                        }
                    }
                }

                watchKey.reset();
            }

            return changesFound;
        }

        public void close() throws IOException
        {
            this.watcher.close();
        }
    }
}