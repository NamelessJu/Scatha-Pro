package namelessju.scathapro;

import namelessju.scathapro.gui.menus.MainSettingsGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

import java.util.Collections;
import java.util.Set;

public class ForgeConfigGuiFactory implements IModGuiFactory
{
    @Override
    public void initialize(Minecraft minecraft)
    {
    
    }
    
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass()
    {
        return MainSettingsGui.class;
    }
    
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories()
    {
        return Collections.emptySet();
    }
    
    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement runtimeOptionCategoryElement)
    {
        return null;
    }
}
