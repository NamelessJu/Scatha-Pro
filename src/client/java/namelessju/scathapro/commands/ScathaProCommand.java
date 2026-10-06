package namelessju.scathapro.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.managers.ChatManager;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

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
        return _ -> {
            scathaPro.chatManager.sendChatErrorMessage("Missing arguments");
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
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Run \"" + clickCommand + "\"").withColor(TextColor.GRAY)))
                    .withClickEvent(new ClickEvent.RunCommand(clickCommand))
                );
            }

            message.append(commandSyntaxText);
            Style grayItalicStyle = Style.EMPTY.withColor(TextColor.GRAY).withItalic(true);
            if (alias != null) message.append(Component.literal(" (alias: " + alias + ")").withStyle(grayItalicStyle));
            message.append(":");
            message.append(Component.literal(" " + description).withStyle(grayItalicStyle));

            return message;
        }

        public void buildAndSend(ChatManager chatManager)
        {
            chatManager.sendChatMessage(build(), false);
        }
    }

    protected static class EnumArgumentType<T extends Enum<T>> implements ArgumentType<T>
    {
        private final Class<T> enumClass;

        public EnumArgumentType(Class<T> enumClass)
        {
            this.enumClass = enumClass;
        }

        @Override
        public T parse(StringReader reader) throws CommandSyntaxException
        {
            String value = reader.readUnquotedString();
            try
            {
                return Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
            }
            catch (Exception e)
            {
                throw new SimpleCommandExceptionType(Component.literal("\"" + value + "\" is not a valid value")).create();
            }
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
        {
            for (T value : enumClass.getEnumConstants())
            {
                builder.suggest(value.name().toLowerCase(Locale.ROOT));
            }
            return builder.buildFuture();
        }
    }
}