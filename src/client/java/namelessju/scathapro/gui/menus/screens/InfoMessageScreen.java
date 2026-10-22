package namelessju.scathapro.gui.menus.screens;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class InfoMessageScreen extends Screen
{
    private final Screen parentScreen;

    private LinearLayout layout;
    private final Component description;
    private final Component okButtonText;

    private AbstractWidget[] extraWidgets = null;

    public InfoMessageScreen(Screen parent, Component title, Component description)
    {
        this(parent, title, description, null);
    }

    public InfoMessageScreen(Screen parent, Component title, Component description, @Nullable Component okButtonText)
    {
        super(title);
        this.parentScreen = parent;
        this.description = description;
        this.okButtonText = okButtonText;
    }

    public void setExtraWidgets(AbstractWidget[] extraWidgets)
    {
        this.extraWidgets = extraWidgets;
    }

    @Override
    protected void init()
    {
        layout = LinearLayout.vertical().spacing(8);
        layout.defaultCellSetting().alignHorizontallyCenter();

        layout.addChild(new StringWidget(getTitle(), font));
        layout.addChild(new MultiLineTextWidget(description, font).setMaxWidth(310).setCentered(true));
        if (extraWidgets != null)
        {
            for (AbstractWidget widget : extraWidgets)
            {
                layout.addChild(widget);
            }
        }

        LinearLayout buttonLayout = this.layout.addChild(LinearLayout.horizontal().spacing(4));
        buttonLayout.defaultCellSetting().paddingTop(16);
        buttonLayout.addChild(Button.builder(
                okButtonText == null ? CommonComponents.GUI_OK : okButtonText,
                _ -> onClose()
            ).width(200).build());

        layout.visitWidgets(this::addRenderableWidget);

        repositionElements();
    }

    @Override
    protected void repositionElements()
    {
        if (layout != null)
        {
            layout.arrangeElements();
            FrameLayout.centerInRectangle(layout, getRectangle());
        }
    }

    @Override
    public void onClose()
    {
        minecraft.gui.setScreen(parentScreen);
    }
}