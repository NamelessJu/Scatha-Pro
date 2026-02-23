package namelessju.scathapro.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.UpdateChecker;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Field;
import java.util.function.Predicate;

public class DevCommand extends ScathaProCommand
{
    public DevCommand(ScathaPro scathaPro)
    {
        super(scathaPro);
    }
    
    @Override
    public String getCommandName()
    {
        return "scathadev";
    }
    
    @Override
    protected String[] getAliases()
    {
        return new String[] {"spdev"};
    }
    
    @Override
    protected <T> void buildCommand(LiteralArgumentBuilder<T> builder, CommandBuildContext buildContext)
    {
        Predicate<T> devModeEnabledPredicate = t -> scathaPro.config.dev.devModeEnabled.get();
        
        builder.executes(commandContext -> {
            boolean isDevModeEnabled = scathaPro.config.dev.devModeEnabled.get();
            scathaPro.chatManager.sendDevChatMessage("Developer mode is currently " + (isDevModeEnabled ? "enabled" : "disabled"));
            return Command.SINGLE_SUCCESS;
        })
        .then(LiteralArgumentBuilder.<T>literal("setDevModeEnabled")
            .executes(getMissingArgumentsCommand())
            .then(RequiredArgumentBuilder.<T, Boolean>argument("Enabled", BoolArgumentType.bool())
                .executes(commandContext -> {
                    boolean enabled = BoolArgumentType.getBool(commandContext, "Enabled");
                    scathaPro.config.dev.devModeEnabled.set(enabled);
                    scathaPro.config.save();
                    scathaPro.chatManager.sendDevChatMessage("Developer mode " + (enabled ? "enabled" : "disabled"));
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("unlockAchievement")
            .executes(getMissingArgumentsCommand())
            .then(RequiredArgumentBuilder.<T, String>argument("Achievement ID", StringArgumentType.string())
                .executes(commandContext -> {
                    String id = commandContext.getArgument("Achievement ID", String.class).trim();
                    if ("*".equals(id))
                    {
                        for (Achievement achievement : Achievement.values())
                        {
                            achievement.unlock();
                        }
                        scathaPro.chatManager.sendDevChatMessage("All achievements unlocked");
                    }
                    else
                    {
                        try
                        {
                            Achievement achievement = Achievement.valueOf(id);
                            if (scathaPro.getProfileData().unlockedAchievements.isUnlocked(achievement))
                            {
                                scathaPro.chatManager.sendErrorChatMessage("Achievement \"" + id + "\" is already unlocked");
                            }
                            else achievement.unlock();
                        }
                        catch (Exception e)
                        {
                            scathaPro.chatManager.sendErrorChatMessage("Achievement \"" + id + "\" doesn't exist");
                        }
                    }
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("trigger")
            .executes(getMissingArgumentsCommand())
            .requires(devModeEnabledPredicate)
            .then(buildAlertTitleTrigger(buildContext))
            .then(buildAlertTrigger(buildContext))
            .then(buildPetDropMessageTrigger())
            .then(buildCompareVersionsTrigger())
        );
    }
    
    private <T> LiteralArgumentBuilder<T> buildAlertTitleTrigger(CommandBuildContext buildContext)
    {
        return LiteralArgumentBuilder.<T>literal("alertTitle")
            .then(RequiredArgumentBuilder.<T, Component>argument("Title", ComponentArgument.textComponent(buildContext))
                .then(RequiredArgumentBuilder.<T, Component>argument("Subtitle", ComponentArgument.textComponent(buildContext))
                    .then(RequiredArgumentBuilder.<T, Integer>argument("Fade In Ticks", IntegerArgumentType.integer(0))
                        .then(RequiredArgumentBuilder.<T, Integer>argument("Stay Ticks", IntegerArgumentType.integer(0))
                            .then(RequiredArgumentBuilder.<T, Integer>argument("Fade Out Ticks", IntegerArgumentType.integer(0))
                                .executes(commandContext -> {
                                    Component title = commandContext.getArgument("Title", Component.class);
                                    Component subtitle = commandContext.getArgument("Subtitle", Component.class);
                                    int fadeInTicks = IntegerArgumentType.getInteger(commandContext, "Fade In Ticks");
                                    int stayTicks = IntegerArgumentType.getInteger(commandContext, "Stay Ticks");
                                    int fadeOutTicks = IntegerArgumentType.getInteger(commandContext, "Fade Out Ticks");
                                    scathaPro.alertTitleOverlay.displayTitle(title, subtitle, fadeInTicks, stayTicks, fadeOutTicks);
                                    return Command.SINGLE_SUCCESS;
                                })
                            )
                        )
                    )
                )
            );
    }
    
    private <T> LiteralArgumentBuilder<T> buildAlertTrigger(CommandBuildContext buildContext)
    {
        return LiteralArgumentBuilder.<T>literal("alert")
            .then(RequiredArgumentBuilder.<T, String>argument("Alert ID", StringArgumentType.string())
                .then(RequiredArgumentBuilder.<T, Component>argument("Dynamic Text", ComponentArgument.textComponent(buildContext))
                    .executes(commandContext -> {
                        String alertID = commandContext.getArgument("Alert ID", String.class);
                        Alert alert;
                        try
                        {
                            Field alertField = scathaPro.alertManager.getClass().getDeclaredField(alertID);
                            alert = (Alert) alertField.get(scathaPro.alertManager);
                        }
                        catch (Exception e)
                        {
                            scathaPro.chatManager.sendErrorChatMessage("Alert \"" + alertID + "\" not found");
                            return 0;
                        }
                        
                        Component dynamicText = commandContext.getArgument("Dynamic Text", Component.class);
                        alert.play(dynamicText);
                        
                        return Command.SINGLE_SUCCESS;
                    })
                )
            );
    }
    
    private <T> LiteralArgumentBuilder<T> buildPetDropMessageTrigger()
    {
        return LiteralArgumentBuilder.<T>literal("petDropMessage")
            .then(RequiredArgumentBuilder.<T, Boolean>argument("Parse Legacy Formatting", BoolArgumentType.bool())
                .executes(commandContext -> {
                    if (BoolArgumentType.getBool(commandContext, "Parse Legacy Formatting"))
                    {
                        scathaPro.chatManager.sendChatMessage(Component.empty().append("§6§lPET DROP! §9Scatha"), false);
                    }
                    else scathaPro.chatManager.sendChatMessage(Constants.getPetDropMessage(Rarity.EPIC), false);
                    return Command.SINGLE_SUCCESS;
                })
            );
    }
    
    private <T> LiteralArgumentBuilder<T> buildCompareVersionsTrigger()
    {
        return LiteralArgumentBuilder.<T>literal("compareVersions")
            .then(RequiredArgumentBuilder.<T, String>argument("From", StringArgumentType.string())
                .then(RequiredArgumentBuilder.<T, String>argument("To", StringArgumentType.string())
                    .executes(commandContext -> {
                        String fromVersion = commandContext.getArgument("From", String.class);
                        String toVersion = commandContext.getArgument("To", String.class);
                        int result = UpdateChecker.compareVersions(fromVersion, toVersion);
                        String message;
                        if (result > 0) message = fromVersion + " < " + toVersion;
                        else if (result < 0) message = fromVersion + " > " + toVersion;
                        else message = fromVersion + " = " + toVersion;
                        scathaPro.chatManager.sendDevChatMessage(message);
                        return Command.SINGLE_SUCCESS;
                    })
                )
            );
    }
}
