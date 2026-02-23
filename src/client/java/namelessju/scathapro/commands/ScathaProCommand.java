package namelessju.scathapro.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.ChatManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public abstract class ScathaProCommand
{
    protected final ScathaPro scathaPro;
    
    public ScathaProCommand(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;
    }
    
    public abstract String getCommandName();
    protected abstract String[] getAliases();
    protected abstract <T> void buildCommand(LiteralArgumentBuilder<T> builder, CommandBuildContext buildContext);
    
    public <T> void register(CommandDispatcher<T> dispatcher, CommandBuildContext buildContext)
    {
        LiteralArgumentBuilder<T> commandBuilder = LiteralArgumentBuilder.literal(getCommandName());
        buildCommand(commandBuilder, buildContext);
        
        dispatcher.register(commandBuilder);
        
        String[] aliases = getAliases();
        if (aliases == null) return;
        for (String alias : aliases) dispatcher.register(redirect(alias, commandBuilder));
    }
    
    /**
     * Creates a new argument and redirects it to another
     */
    protected <T> LiteralArgumentBuilder<T> redirect(String argumentName, ArgumentBuilder<T, ?> redirectTarget)
    {
        return LiteralArgumentBuilder.<T>literal(argumentName).redirect(redirectTarget.build()).executes(redirectTarget.getCommand());
    }
    
    public <T> Command<T> getMissingArgumentsCommand()
    {
        return context -> {
            scathaPro.chatManager.sendErrorChatMessage("Missing arguments");
            return Command.SINGLE_SUCCESS;
        };
    }
    
    protected static class HelpMessageBuilder
    {
        private final @NonNull ScathaProCommand command;
        private final @NonNull String description;
        private final @Nullable String parameters;
        
        private @Nullable String alias = null;
        private boolean allowClick = false;
        private @Nullable String clickCommand = null;
        
        public HelpMessageBuilder(@NonNull ScathaProCommand command, @NonNull String description)
        {
            this(command, null, description);
        }
        
        public HelpMessageBuilder(@NonNull ScathaProCommand command, @Nullable String parameters, @NonNull String description)
        {
            this.command = command;
            this.description = description;
            this.parameters = parameters;
        }
        
        /**
         * Automatically uses the aliases from the command
         */
        public HelpMessageBuilder withAlias()
        {
            String[] aliases = command.getAliases();
            if (aliases.length == 0)
            {
                this.alias = null;
                return this;
            }
            
            StringBuilder aliasBuilder = new StringBuilder();
            for (String alias : aliases)
            {
                if (!aliasBuilder.isEmpty()) aliasBuilder.append(", ");
                aliasBuilder.append("/").append(alias);
            }
            this.alias = aliasBuilder.toString();
            return this;
        }
        
        public HelpMessageBuilder withAlias(String alias)
        {
            this.alias = alias;
            return this;
        }
        
        public HelpMessageBuilder withClickAction()
        {
            this.clickCommand = null;
            allowClick = true;
            return this;
        }
        
        public HelpMessageBuilder withClickAction(String clickCommand)
        {
            this.clickCommand = clickCommand;
            return this;
        }
        
        public Component build()
        {
            MutableComponent message = Component.empty();
            String commandString = "/" + command.getCommandName() + (parameters != null ? " " + parameters : "");
            MutableComponent commandSyntaxText = Component.literal(commandString);
            
            String clickCommand = null;
            if (this.clickCommand != null) clickCommand = this.clickCommand;
            else if (allowClick) clickCommand = commandString;
            if (clickCommand != null)
            {
                commandSyntaxText.withStyle(Style.EMPTY
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Run \"" + clickCommand + "\"").withStyle(ChatFormatting.GRAY)))
                    .withClickEvent(new ClickEvent.RunCommand(clickCommand))
                );
            }
            
            message.append(commandSyntaxText);
            if (alias != null) message.append(Component.literal(" (alias: " + alias + ")").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            message.append(":");
            message.append(Component.literal(" " + description).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            
            return message;
        }
        
        public void buildAndSend(ChatManager chatManager)
        {
            chatManager.sendChatMessage(build(), false);
        }
    }
}
