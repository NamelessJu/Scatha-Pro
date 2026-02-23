package namelessju.scathapro.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.gui.menus.screens.AverageMoneyCalculatorScreen;
import net.minecraft.commands.CommandBuildContext;

public class AverageMoneyCommand extends ScathaProCommand
{
    public AverageMoneyCommand(ScathaPro scathaPro)
    {
        super(scathaPro);
    }
    
    @Override
    public String getCommandName()
    {
        return "scathamoney";
    }
    
    @Override
    protected String[] getAliases()
    {
        return new String[0];
    }
    
    @Override
    protected <T> void buildCommand(LiteralArgumentBuilder<T> builder, CommandBuildContext buildContext)
    {
        builder.executes(commandContext -> {
            scathaPro.runNextTick(() -> scathaPro.minecraft.setScreen(new AverageMoneyCalculatorScreen(scathaPro, null)));
            return Command.SINGLE_SUCCESS;
        });
    }
}