package namelessju.scathapro.gui.menus.screens;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.framework.screens.LayoutScreen;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class InfoMessageScreen extends LayoutScreen
{
    private final Component description;
    private final Component doneButtonText;
    
    public InfoMessageScreen(ScathaPro scathaPro, Screen parent, Component title, Component description)
    {
        this(scathaPro, parent, title, description, null);
    }
    
    public InfoMessageScreen(ScathaPro scathaPro, Screen parent, Component title, Component description, Component doneButtonText)
    {
        super(scathaPro, title, false, parent);
        this.description = description;
        this.doneButtonText = doneButtonText;
    }
    
    @Override
    protected void initLayout(@NonNull HeaderAndFooterLayout layout)
    {
        LinearLayout messageLayout = LinearLayout.vertical().spacing(10);
        messageLayout.addChild(new StringWidget(getTitle(), font), LayoutSettings::alignHorizontallyCenter);
        messageLayout.addChild(new MultiLineTextWidget(description, font).setMaxWidth(310), LayoutSettings::alignHorizontallyCenter);
        layout.addToContents(messageLayout, LayoutSettings::alignHorizontallyCenter);
        
        addFooter(doneButton(doneButtonText == null ? CommonComponents.GUI_OK : doneButtonText, 200));
    }
}
