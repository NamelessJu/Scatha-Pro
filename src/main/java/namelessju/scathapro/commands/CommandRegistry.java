package namelessju.scathapro.commands;

import namelessju.scathapro.ScathaPro;
import net.minecraftforge.client.ClientCommandHandler;

public class CommandRegistry
{
    public final MainCommand mainCommand;
    public final ChancesCommand chancesCommand;
    public final AverageMoneyCommand averageMoneyCommand;
    public final DevCommand devCommand;
    
    public CommandRegistry(ScathaPro scathaPro)
    {
        mainCommand = new MainCommand(scathaPro);
        chancesCommand = new ChancesCommand();
        averageMoneyCommand = new AverageMoneyCommand(scathaPro);
        devCommand = new DevCommand(scathaPro);
    }
    
    public void registerCommands()
    {
        ClientCommandHandler.instance.registerCommand(mainCommand);
        ClientCommandHandler.instance.registerCommand(chancesCommand);
        ClientCommandHandler.instance.registerCommand(averageMoneyCommand);
        ClientCommandHandler.instance.registerCommand(devCommand);
    }
}
