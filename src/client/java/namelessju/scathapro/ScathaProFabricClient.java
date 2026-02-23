package namelessju.scathapro;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ScathaProFabricClient extends ScathaPro implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        ClientCommandRegistrationCallback.EVENT.register(this::registerCommands);
        
        onInitialization();
    }
    
    @Override
    public Path getBaseSaveDirectoryPath()
    {
        return FabricLoader.getInstance().getConfigDir();
    }
}