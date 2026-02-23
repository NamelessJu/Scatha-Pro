package namelessju.scathapro.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.UpdateChecker;
import namelessju.scathapro.gui.menus.screens.AchievementListScreen;
import namelessju.scathapro.gui.menus.screens.settings.MainSettingsScreen;
import namelessju.scathapro.managers.ChatManager;
import namelessju.scathapro.util.FileUtil;
import namelessju.scathapro.util.PartialScreenshot;
import namelessju.scathapro.util.TimeUtil;
import namelessju.scathapro.util.UnicodeSymbol;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

import java.io.IOException;

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
        builder.executes(context -> {
            sendHelp(1);
            return Command.SINGLE_SUCCESS;
        })
        .then(LiteralArgumentBuilder.<T>literal("help")
            .executes(context -> {
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
            .executes(context -> {
                scathaPro.runNextTick(() -> scathaPro.minecraft.setScreen(new MainSettingsScreen(scathaPro, null)));
                return Command.SINGLE_SUCCESS;
            })
            .then(LiteralArgumentBuilder.<T>literal("reset")
                .executes(context -> {
                    scathaPro.config.reset();
                    scathaPro.config.save();
                    
                    if (scathaPro.config.alerts.scathaPetDropAlertEnabled.get())
                    {
                        scathaPro.coreManager.previousScathaPets = null;
                    }
                    
                    scathaPro.mainOverlay.updateAll();
                    
                    chatManager.sendChatMessage(Component.literal("Settings reset to default!").setStyle(ChatManager.HIGHLIGHT_STYLE));
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("achievements")
            .executes(context -> {
                scathaPro.runNextTick(() -> scathaPro.minecraft.setScreen(new AchievementListScreen(scathaPro, null)));
                return Command.SINGLE_SUCCESS;
            })
        )
        .then(LiteralArgumentBuilder.<T>literal("dailyStreak")
            .executes(context -> {
                handleDailyStreak();
                return Command.SINGLE_SUCCESS;
            })
        )
        .then(LiteralArgumentBuilder.<T>literal("profileStats")
            .executes(context -> {
                handleProfileStats();
                return Command.SINGLE_SUCCESS;
            })
            .then(LiteralArgumentBuilder.<T>literal("update")
                .executes(context -> {
                    handleProfileStatsUpdate(false);
                    return Command.SINGLE_SUCCESS;
                })
                .then(LiteralArgumentBuilder.<T>literal("confirm")
                    .executes(context -> {
                        handleProfileStatsUpdate(true);
                        return Command.SINGLE_SUCCESS;
                    })
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
                            
                            scathaPro.mainOverlay.updatePetDrops();
                            
                            chatManager.sendChatMessage(
                                Component.literal("Scatha pet drop amounts changed\n")
                                .append(Component.literal("(Achievements will update on next game start or Scatha pet drop)").withStyle(ChatFormatting.GRAY))
                            );
                            
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("checkUpdate")
            .executes(context -> {
                scathaPro.chatManager.sendChatMessage(Component.literal("Checking for update...").withStyle(ChatFormatting.GRAY));
                UpdateChecker.checkForUpdate(scathaPro, true);
                return Command.SINGLE_SUCCESS;
            })
        )
        .then(LiteralArgumentBuilder.<T>literal("toggleOverlay")
            .executes(context -> {
                scathaPro.mainOverlay.toggleVisibility();
                scathaPro.chatManager.sendChatMessage("Overlay " + (scathaPro.mainOverlay.isVisible() ? "enabled" : "disabled"));
                return Command.SINGLE_SUCCESS;
            })
        )
        .then(LiteralArgumentBuilder.<T>literal("backup")
            .executes(getMissingArgumentsCommand())
            .then(LiteralArgumentBuilder.<T>literal("full")
                .executes(context -> {
                    scathaPro.persistentData.save();
                    scathaPro.config.save();
                    scathaPro.saveFilesManager.backup();
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(LiteralArgumentBuilder.<T>literal("persistentData")
                .executes(context -> {
                    scathaPro.persistentData.save();
                    scathaPro.saveFilesManager.backupPersistentData();
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("persistentDataFile")
            .executes(context -> {
                try
                {
                    FileUtil.openFileInExplorer(scathaPro.persistentData.getFile());
                    scathaPro.chatManager.sendChatMessage("Persistent data file opened in file explorer");
                }
                catch (IOException e)
                {
                    scathaPro.chatManager.sendErrorChatMessage("Error while opening persistent data file in explorer");
                    ScathaPro.LOGGER.error("Exception while opening persistent data file in explorer", e);
                }
                return Command.SINGLE_SUCCESS;
            })
        );
        /*
        .then(LiteralArgumentBuilder.<T>literal("screenshot")
            .executes(getMissingArgumentsCommand())
            .then(LiteralArgumentBuilder.<T>literal("chat")
                .executes(context -> {
                    PartialScreenshot.takeChatScreenshot(scathaPro, null);
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(LiteralArgumentBuilder.<T>literal("overlay")
                .executes(context -> {
                    PartialScreenshot.takeOverlayScreenshot(scathaPro);
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        */
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
                /*
                new HelpMessageBuilder(this, "screenshot <area>", "Take a screenshot of a specific area of the screen")
                    .withClickAction("/" + getCommandName() + " screenshot").buildAndSend(chatManager);
                */
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
                chatManager.sendErrorChatMessage("Invalid page number!");
        }
        
        chatManager.sendChatMessage(
            Component.literal("Help page " + page + "/2 ").withStyle(ChatFormatting.WHITE)
                .append(Component.empty().withStyle(ChatFormatting.GRAY)
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
        int highscore = scathaPro.getProfileData().scathaFarmingStreakHighscore.get();
        
        chatManager.sendChatDivider();
        chatManager.sendChatMessage(Component.empty()
            .append(Component.literal("Daily Scatha farming streak:\n").withStyle(ChatManager.HIGHLIGHT_STYLE))
            .append("Current streak: ")
            .append(Component.literal(streak + (streak != 1 ? " days" : " day") + "\n").withStyle(ChatFormatting.GREEN))
            .append("Highest streak: ")
            .append(Component.literal(highscore + (highscore != 1 ? " days" : " day") + "\n").withStyle(ChatFormatting.GOLD))
            .append(farmedToday
                ? Component.literal(UnicodeSymbol.heavyCheckMark + " You have farmed Scathas today!").withStyle(ChatFormatting.GREEN)
                : Component.literal(UnicodeSymbol.heavyX + " You haven't yet farmed Scathas today...").withStyle(ChatFormatting.RED)
            )
        );
        chatManager.sendChatDivider();
    }
    
    private void handleProfileStats()
    {
        chatManager.sendChatDivider();
        chatManager.sendChatMessage(Component.literal("Saved Scatha farming profile stats:").setStyle(ChatManager.HIGHLIGHT_STYLE));
        chatManager.sendChatMessage(Component.literal(" ").append(
            scathaPro.persistentDataProfileManager.getMagicFindComponent(true).append(" Magic Find")
        ), false);
        chatManager.sendChatMessage(Component.literal(" ").append(
            scathaPro.persistentDataProfileManager.getBestiaryMagicFindString(true).append(" Worm Bestiary Magic Find")
        ), false);
        chatManager.sendChatMessage(Component.literal(" ").append(
            scathaPro.persistentDataProfileManager.getPetLuckComponent(true).append(" Pet Luck")
        ), false);
        
        String updateCommand = "/" + scathaPro.mainCommand.getCommandName() + " profileStats update";
        
        chatManager.sendChatMessage(Component.empty().withStyle(ChatFormatting.GRAY)
            .append("Update Magic Find and Pet Luck using ")
            .append(Component.literal(updateCommand).setStyle(Style.EMPTY
                .applyFormat(ChatFormatting.UNDERLINE)
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to run the command").withStyle(ChatFormatting.GRAY)))
                .withClickEvent(new ClickEvent.RunCommand(updateCommand))
            ))
            .append(" and bestiary Magic Find by ")
            .append(Component.literal("opening the worm bestiary").setStyle(Style.EMPTY
                .applyFormat(ChatFormatting.UNDERLINE)
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to run \"/be worms\"").withStyle(ChatFormatting.GRAY)))
                .withClickEvent(new ClickEvent.RunCommand("/be worms"))
            )),
            false
        );
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
            String confirmCommand = "/" + scathaPro.mainCommand.getCommandName() + " profileStats update confirm";
            
            chatManager.sendChatDivider();
            chatManager.sendChatMessage(Component.literal("Equip everything (armor, pet, weapon) you use when killing a Scatha and then ")
                .setStyle(ChatManager.HIGHLIGHT_STYLE)
                .append(Component.literal("click here to confirm").setStyle(Style.EMPTY
                    .applyFormats(ChatFormatting.GREEN, ChatFormatting.UNDERLINE)
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Opens the Skyblock menu and\nupdates the saved profile stats").withStyle(ChatFormatting.GRAY)))
                    .withClickEvent(new ClickEvent.RunCommand(confirmCommand))
                ))
                .append(Component.literal(" (or use \"" + confirmCommand + "\")").withStyle(ChatFormatting.GRAY)));
            chatManager.sendChatDivider();
        }
    }
}
