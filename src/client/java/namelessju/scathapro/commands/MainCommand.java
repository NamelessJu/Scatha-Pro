package namelessju.scathapro.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.UpdateChecker;
import namelessju.scathapro.files.PersistentData;
import namelessju.scathapro.gui.menus.screens.AchievementListScreen;
import namelessju.scathapro.gui.menus.screens.settings.MainSettingsScreen;
import namelessju.scathapro.managers.ChatManager;
import namelessju.scathapro.miscellaneous.data.enums.WitchesStew;
import namelessju.scathapro.util.FileUtil;
import namelessju.scathapro.util.TextUtil;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.IOException;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class MainCommand extends ScathaProCommand
{
    private final ChatManager chatManager;

    public MainCommand(ScathaPro scathaPro)
    {
        super(scathaPro);
        chatManager = scathaPro.chatManager;
    }

    @Override
    public String getCommandName()
    {
        return "scathapro";
    }

    @Override
    protected String[] getAliases()
    {
        return new String[] {"sp"};
    }

    @Override
    protected <T> void buildCommand(LiteralArgumentBuilder<T> builder, CommandBuildContext buildContext)
    {
        builder.executes(_ -> {
            sendHelp(1);
            return Command.SINGLE_SUCCESS;
        })
        .then(LiteralArgumentBuilder.<T>literal("help")
            .executes(_ -> {
                sendHelp(1);
                return Command.SINGLE_SUCCESS;
            })
            .then(RequiredArgumentBuilder.<T, Integer>argument("Page", IntegerArgumentType.integer(1, 2))
                .executes(context -> {
                    sendHelp(IntegerArgumentType.getInteger(context, "Page"));
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("settings")
            .executes(_ -> {
                scathaPro.runNextTick(() -> scathaPro.minecraft.gui.setScreen(new MainSettingsScreen(scathaPro, null)));
                return Command.SINGLE_SUCCESS;
            })
            .then(LiteralArgumentBuilder.<T>literal("reset")
                .executes(_ -> {
                    scathaPro.config.reset();
                    scathaPro.config.save();

                    scathaPro.mainOverlay.updateAll();

                    chatManager.sendChatMessage(Component.literal("Settings reset to default!").setStyle(ChatManager.HIGHLIGHT_STYLE));
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("achievements")
            .executes(_ -> {
                scathaPro.runNextTick(() -> scathaPro.minecraft.gui.setScreen(new AchievementListScreen(scathaPro, null)));
                return Command.SINGLE_SUCCESS;
            })
        )
        .then(LiteralArgumentBuilder.<T>literal("dailyStreak")
            .executes(_ -> {
                handleDailyStreak();
                return Command.SINGLE_SUCCESS;
            })
        )
        .then(LiteralArgumentBuilder.<T>literal("profileStats")
            .executes(_ -> {
                handleProfileStats();
                return Command.SINGLE_SUCCESS;
            })
            .then(LiteralArgumentBuilder.<T>literal("updateGlobal")
                .executes(_ -> {
                    handleProfileStatsUpdate(false);
                    return Command.SINGLE_SUCCESS;
                })
                .then(LiteralArgumentBuilder.<T>literal("confirm")
                    .executes(_ -> {
                        handleProfileStatsUpdate(true);
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
            .then(LiteralArgumentBuilder.<T>literal("setWitchesStewEaten")
                .executes(getMissingArgumentsCommand())
                .then(RequiredArgumentBuilder.<T, WitchesStew>argument("Witches Stew", new WitchesStewArgumentType())
                    .executes(getMissingArgumentsCommand())
                    .then(RequiredArgumentBuilder.<T, Boolean>argument("Eaten", BoolArgumentType.bool())
                        .executes(context -> {
                            WitchesStew stew = context.getArgument("Witches Stew", WitchesStew.class);
                            boolean eaten = BoolArgumentType.getBool(context, "Eaten");
                            handleWitchesStewMagicFindUpdate(stew, eaten);
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("setPetDrops")
            .executes(getMissingArgumentsCommand())
            .then(RequiredArgumentBuilder.<T, Integer>argument("Rare", IntegerArgumentType.integer(0, Constants.maxLegitPetDropsAmount))
                .executes(getMissingArgumentsCommand())
                .then(RequiredArgumentBuilder.<T, Integer>argument("Epic", IntegerArgumentType.integer(0, Constants.maxLegitPetDropsAmount))
                    .executes(getMissingArgumentsCommand())
                    .then(RequiredArgumentBuilder.<T, Integer>argument("Legendary", IntegerArgumentType.integer(0, Constants.maxLegitPetDropsAmount))
                        .executes(context -> {
                            int rare = IntegerArgumentType.getInteger(context, "Rare");
                            int epic = IntegerArgumentType.getInteger(context, "Epic");
                            int legendary = IntegerArgumentType.getInteger(context, "Legendary");

                            scathaPro.getProfileData().rarePetDrops.set(rare);
                            scathaPro.getProfileData().epicPetDrops.set(epic);
                            scathaPro.getProfileData().legendaryPetDrops.set(legendary);
                            scathaPro.persistentData.save();

                            chatManager.sendChatMessage(
                                Component.literal("Scatha pet drop amounts changed\n")
                                .append(
                                    Component.literal("(Achievements will update on next game start or Scatha pet drop)")
                                        .withColor(TextColor.GRAY)
                                )
                            );

                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("checkUpdate")
            .executes(_ -> {
                scathaPro.chatManager.sendChatMessage(Component.literal("Checking for update...").withColor(TextColor.GRAY));
                UpdateChecker.checkForUpdate(scathaPro, true);
                return Command.SINGLE_SUCCESS;
            })
        )
        .then(LiteralArgumentBuilder.<T>literal("overlay")
            .executes(getMissingArgumentsCommand())
            .then(LiteralArgumentBuilder.<T>literal("toggleVisibility")
                .executes(_ -> {
                    scathaPro.mainOverlay.toggleShown();
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(LiteralArgumentBuilder.<T>literal("toggleEnabled")
                .executes(_ -> {
                    scathaPro.mainOverlay.toggleEnabled();
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("backup")
            .executes(getMissingArgumentsCommand())
            .then(LiteralArgumentBuilder.<T>literal("full")
                .executes(_ -> {
                    scathaPro.persistentData.save();
                    scathaPro.config.save();
                    scathaPro.backupManager.backup();
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(LiteralArgumentBuilder.<T>literal("persistentData")
                .executes(_ -> {
                    scathaPro.persistentData.save();
                    scathaPro.backupManager.backupPersistentData();
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("persistentDataFile")
            .executes(_ -> {
                try
                {
                    FileUtil.openFileInExplorer(scathaPro.persistentData.getFile());
                    scathaPro.chatManager.sendChatMessage("Persistent data file opened in file explorer");
                }
                catch (IOException e)
                {
                    scathaPro.chatManager.sendChatErrorMessage("Error while opening persistent data file in explorer");
                    ScathaPro.LOGGER.error("Exception while opening persistent data file in explorer", e);
                    return 0;
                }
                return Command.SINGLE_SUCCESS;
            })
        )
        .then(LiteralArgumentBuilder.<T>literal("debugLogs")
            .executes(_ -> {
                boolean isEnabled = ScathaPro.LOGGER.isDebugEnabled();
                scathaPro.chatManager.sendChatMessage("Debug logs are currently " + (isEnabled ? "enabled" : "disabled"));
                return Command.SINGLE_SUCCESS;
            })
            .then(RequiredArgumentBuilder.<T, Boolean>argument("Enabled", BoolArgumentType.bool())
                .executes(context -> {
                    boolean enable = BoolArgumentType.getBool(context, "Enabled");
                    if (enable == ScathaPro.LOGGER.isDebugEnabled())
                    {
                        scathaPro.chatManager.sendChatErrorMessage("Debug logs are already " + (enable ? "enabled" : "disabled"));
                        return 0;
                    }
                    Configurator.setLevel(ScathaPro.LOGGER.getName(), enable ? Level.DEBUG : Level.INFO);
                    scathaPro.chatManager.sendChatMessage(
                        Component.literal("Debug logs " + (enable ? "enabled" : "disabled") + ".")
                        .append(Component.literal(" (Will revert to default on next game start)").withColor(TextColor.GRAY))
                    );
                    return Command.SINGLE_SUCCESS;
                })
            )
        );
    }

    private void sendHelp(int page)
    {
        chatManager.sendChatDivider();
        chatManager.sendChatMessage(Component.literal("Available commands:").withStyle(ChatManager.HIGHLIGHT_STYLE));

        switch (page)
        {
            case 1:
                new HelpMessageBuilder(this, "The base command (shows this help message)")
                    .withAlias().buildAndSend(chatManager);
                new HelpMessageBuilder(this, "settings", "Opens the mod's settings menu")
                    .withClickAction().buildAndSend(chatManager);
                new HelpMessageBuilder(this, "achievements", "Opens the achievements menu")
                    .withClickAction().buildAndSend(chatManager);
                new HelpMessageBuilder(scathaPro.scathaChancesCommand, "Check/calculate Scatha pet drop chances")
                    .withClickAction().withAlias().buildAndSend(chatManager);
                new HelpMessageBuilder(scathaPro.averageMoneyCommand, "Calculate average Scatha farming profits")
                    .withClickAction().withAlias().buildAndSend(chatManager);
                new HelpMessageBuilder(this, "dailyStreak", "Shows information about your daily Scatha farming streak")
                    .withClickAction().buildAndSend(chatManager);
                new HelpMessageBuilder(this, "profileStats", "Check/update the values that the mod uses when displaying profile stats")
                    .withClickAction().buildAndSend(chatManager);
                break;

            case 2:
                new HelpMessageBuilder(this, "setPetDrops <rare> <epic> <legendary>", "Set your pet drop counter to the specified numbers").buildAndSend(chatManager);
                new HelpMessageBuilder(this, "toggleOverlay", "Toggles the overlay visibility")
                    .withClickAction().buildAndSend(chatManager);
                new HelpMessageBuilder(this, "checkUpdate", "Check for mod updates")
                    .withClickAction().buildAndSend(chatManager);
                new HelpMessageBuilder(this, "settings reset", "Reset all settings").buildAndSend(chatManager);
                new HelpMessageBuilder(this, "backup <type>", "Creates a backup of this mod's save files").buildAndSend(chatManager);
                new HelpMessageBuilder(this, "persistentDataFile", "Open the mod's persistent data file path in the file explorer")
                    .withClickAction().buildAndSend(chatManager);
                break;

            default:
                chatManager.sendChatErrorMessage("Invalid page number!");
        }

        chatManager.sendChatMessage(
            Component.literal("Help page " + page + "/2 ").withColor(TextColor.WHITE)
                .append(Component.empty().withColor(TextColor.GRAY)
                    .append(Component.literal("("))
                    .append(
                        Component.literal("/" + getCommandName() + " help <page>")
                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.SuggestCommand("/" + getCommandName() + " help ")))
                    )
                    .append(Component.literal(")"))
                ),
            false
        );

        chatManager.sendChatDivider();
    }

    private void handleDailyStreak()
    {

        boolean farmedToday = TimeUtil.today().equals(
            scathaPro.getProfileData().lastScathaFarmedDate.get()
        );
        int streak = scathaPro.getProfileData().scathaFarmingStreak.get();
        int highScore = scathaPro.getProfileData().scathaFarmingStreakHighScore.get();

        chatManager.sendChatDivider();
        chatManager.sendChatMessage(Component.empty()
            .append(Component.literal("Daily Scatha farming streak:\n").withStyle(ChatManager.HIGHLIGHT_STYLE))
            .append("Current streak: ")
            .append(Component.literal(streak + (streak != 1 ? " days" : " day") + "\n").withColor(TextColor.GREEN))
            .append("Highest streak: ")
            .append(Component.literal(highScore + (highScore != 1 ? " days" : " day") + "\n").withColor(TextColor.GOLD))
            .append(farmedToday
                ? Component.literal(UnicodeSymbol.heavyCheckMark + " You have farmed Scathas today!").withColor(TextColor.GREEN)
                : Component.literal(UnicodeSymbol.heavyMultiplicationX + " You haven't yet farmed Scathas today...").withColor(TextColor.RED)
            )
        );
        chatManager.sendChatDivider();
    }

    private void handleProfileStats()
    {
        String updateGlobalStatsCommand = "/" + scathaPro.mainCommand.getCommandName() + " profileStats updateGlobal";
        Component updateGlobalStatsComponent = Component.literal(" ").withColor(TextColor.GRAY)
            .append("(")
            .append(Component.literal("update").setStyle(Style.EMPTY
                .withUnderlined(true)
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to run:\n" + updateGlobalStatsCommand).withColor(TextColor.GRAY)))
                .withClickEvent(new ClickEvent.RunCommand(updateGlobalStatsCommand))
            ))
            .append(")");
        String updateBestiaryMagicFindCommand = "/be Stoneworm";
        Component updateBestiaryMagicFindComponent = Component.literal(" ").withColor(TextColor.GRAY)
            .append("(")
            .append(Component.literal("update").setStyle(Style.EMPTY
                .withUnderlined(true)
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to run:\n" + updateBestiaryMagicFindCommand).withColor(TextColor.GRAY)))
                .withClickEvent(new ClickEvent.RunCommand(updateBestiaryMagicFindCommand))
            ))
            .append(")");
        String setWitchesStewMagicFindCommand = "/" + scathaPro.mainCommand.getCommandName() + " profileStats setWitchesStewEaten ";
        Component setWitchesStewMagicFindComponent = Component.literal(" ").withColor(TextColor.GRAY)
            .append("(")
            .append(Component.literal("update").setStyle(Style.EMPTY
                .withUnderlined(true)
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to suggest:\n" + setWitchesStewMagicFindCommand).withColor(TextColor.GRAY)))
                .withClickEvent(new ClickEvent.SuggestCommand(setWitchesStewMagicFindCommand))
            ))
            .append(")");

        chatManager.sendChatDivider();
        chatManager.sendChatMessage(Component.literal("Saved Scatha farming profile stats:").setStyle(ChatManager.HIGHLIGHT_STYLE));
        chatManager.sendChatMessage(Component.literal(" ").append(
                scathaPro.persistentDataProfileManager.getEffectiveMagicFindComponent(false).append(" Effective Magic Find")
            ), false);
        chatManager.sendChatMessage(Component.empty()
            .append(Component.literal(" ├ ").withColor(TextColor.GRAY))
            .append(
                scathaPro.persistentDataProfileManager.getTotalMagicFindComponent(false, true).append(" Total Magic Find")
            ), false);
        chatManager.sendChatMessage(Component.empty()
            .append(Component.literal(" │ ├ ").withColor(TextColor.GRAY))
            .append(
                scathaPro.persistentDataProfileManager.getGlobalMagicFindComponent(true).append(" Global Magic Find")
            )
            .append(updateGlobalStatsComponent), false);
        chatManager.sendChatMessage(Component.empty()
            .append(Component.literal(" │ ├ ").withColor(TextColor.GRAY))
            .append(
                scathaPro.persistentDataProfileManager.getBestiaryMagicFindComponent(true).append(" Worm Bestiary Magic Find")
            )
            .append(updateBestiaryMagicFindComponent), false);
        chatManager.sendChatMessage(Component.empty()
            .append(Component.literal(" │ └ ").withColor(TextColor.GRAY))
            .append(
                scathaPro.persistentDataProfileManager.getWitchesStewMagicFindComponent(true).append(" Witches Stew Scatha Magic Find")
                    .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(
                        Component.literal("Magic Find from specific Year of the\nWitch stews that apply to Scathas").withColor(TextColor.GRAY)
                    )))
            )
            .append(setWitchesStewMagicFindComponent), false
        );
        chatManager.sendChatMessage(Component.empty()
            .append(Component.literal(" └ ").withColor(TextColor.GRAY))
            .append(
                scathaPro.persistentDataProfileManager.getPetLuckComponent(true).append(" Pet Luck")
            )
            .append(updateGlobalStatsComponent), false);
        chatManager.sendChatDivider();
    }

    private void handleProfileStatsUpdate(boolean confirmed)
    {
        if (confirmed)
        {
            LocalPlayer player = scathaPro.minecraft.player;
            if (player != null)
            {
                scathaPro.containerScreenParsingManager.profileStatsParser.enabled = true;
                player.connection.sendCommand("stats");
            }
        }
        else
        {
            String confirmCommand = "/" + scathaPro.mainCommand.getCommandName() + " profileStats updateGlobal confirm";

            chatManager.sendChatDivider();
            chatManager.sendChatMessage(Component.literal("Equip everything (armor, pet, weapon) you use when killing a Scatha and then ")
                .setStyle(ChatManager.HIGHLIGHT_STYLE)
                .append(Component.literal("click here to confirm").setStyle(Style.EMPTY
                    .withColor(TextColor.GREEN).withUnderlined(true)
                    .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal("Opens the Skyblock menu and\nupdates the saved profile stats").withColor(TextColor.GRAY)
                    ))
                    .withClickEvent(new ClickEvent.RunCommand(confirmCommand))
                ))
                .append(Component.literal(" (or use \"" + confirmCommand + "\")").withColor(TextColor.GRAY)));
            chatManager.sendChatDivider();
        }
    }

    private void handleWitchesStewMagicFindUpdate(WitchesStew stew, boolean eaten)
    {
        PersistentData.ProfileData profileData = scathaPro.getProfileData();
        int previousMagicFind = profileData.witchesStewsEaten.getMagicFind();

        profileData.witchesStewsEaten.setEaten(stew, eaten);
        scathaPro.persistentData.save();
        scathaPro.mainOverlay.updateProfileStats();

        scathaPro.chatManager.sendChatMessage(Component.empty()
            .append("Marked " + stew.stewName + " stew (")
            .append(stew.mobTypeComponent)
            .append(") as " + (!eaten ? "not " : "") + "eaten")
        );
        int newMagicFind = profileData.witchesStewsEaten.getMagicFind();
        if (newMagicFind != previousMagicFind)
        {
            scathaPro.chatManager.sendChatMessage(Component.empty().withColor(TextColor.GRAY)
                .append("Updated Witches Stew Scatha Magic Find (")
                .append(TextUtil.numberToComponentOrObf(previousMagicFind, 2, false, RoundingMode.HALF_UP))
                .append(" " + UnicodeSymbol.hypixelArrowRight + " ")
                .append(TextUtil.numberToComponentOrObf(newMagicFind, 2, false, RoundingMode.HALF_UP))
                .append(")")
            );
        }
    }


    private static class WitchesStewArgumentType implements ArgumentType<WitchesStew>
    {
        @Override
        public WitchesStew parse(StringReader reader) throws CommandSyntaxException
        {
            String id = reader.readUnquotedString();
            try
            {
                return WitchesStew.valueOf(id.toUpperCase(Locale.ROOT));
            }
            catch (Exception e)
            {
                throw new SimpleCommandExceptionType(Component.literal("Witches Stew \"" + id + "\" not found")).create();
            }
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
        {
            for (WitchesStew stew : WitchesStew.values())
            {
                builder.suggest(stew.name().toLowerCase(Locale.ROOT), Component.literal(stew.stewName));
            }
            return builder.buildFuture();
        }
    }
}