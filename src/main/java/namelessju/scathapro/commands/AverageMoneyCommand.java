package namelessju.scathapro.commands;

import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.AverageMoneyGui;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

public class AverageMoneyCommand extends CommandBase
{
    public static final String COMMAND_NAME = "scathamoney";
    
    private final ScathaPro scathaPro;
    
    public AverageMoneyCommand(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    @Override
    public String getCommandName()
    {
        return COMMAND_NAME;
    }
    
    @Override
    public String getCommandUsage(ICommandSender sender)
    {
        return "/" + COMMAND_NAME;
    }
    
    @Override
    public int getRequiredPermissionLevel()
    {
        return 0;
    }
    
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        scathaPro.variables.openGuiNextTick = new AverageMoneyGui(scathaPro, null);
    }
}
