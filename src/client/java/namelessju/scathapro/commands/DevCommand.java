package namelessju.scathapro.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.JsonOps;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import namelessju.scathapro.UpdateChecker;
import namelessju.scathapro.achievements.Achievement;
import namelessju.scathapro.alerts.Alert;
import namelessju.scathapro.miscellaneous.data.enums.Rarity;
import namelessju.scathapro.util.JsonUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
        Predicate<T> devModeRequirement = t -> scathaPro.config.dev.devModeEnabled.get();
        
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
        .then(LiteralArgumentBuilder.<T>literal("getEntityData")
            .executes(commandContext -> {
                LocalPlayer player = scathaPro.minecraft.player;
                if (player == null)
                {
                    scathaPro.chatManager.sendChatErrorMessage("Local player missing");
                    return Command.SINGLE_SUCCESS;
                }
                
                AABB entityAABB = AABB.ofSize(player.position().add(0D, 1D, 0D), 20D, 8D, 20D);
                List<Entity> nearbyEntities = player.level().getEntities(player, entityAABB);
                
                JsonArray resultsArray = new JsonArray();
                for (Entity entity : nearbyEntities)
                {
                    JsonObject entityObject = new JsonObject();
                    entityObject.add("type", new JsonPrimitive(entity.getClass().getName()));
                    
                    TagValueOutput nbtOutput = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
                    entity.save(nbtOutput);
                    entityObject.add("data", NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, nbtOutput.buildResult()));
                    
                    resultsArray.add(entityObject);
                }
                
                String jsonString = JsonUtil.toString(resultsArray, true);
                
                scathaPro.minecraft.keyboardHandler.setClipboard(jsonString);
                int amount = resultsArray.size();
                scathaPro.chatManager.sendDevChatMessage(
                    Component.literal("Data of nearby entities copied to clipboard")
                        .append(
                            Component.literal(" (" + amount + " " + (amount == 1 ? "entity" : "entities") + " found)")
                                .withStyle(ChatFormatting.GRAY)
                        )
                );
                return Command.SINGLE_SUCCESS;
            })
        )
        .then(LiteralArgumentBuilder.<T>literal("unlockAchievement")
            .requires(devModeRequirement)
            .executes(getMissingArgumentsCommand())
            .then(RequiredArgumentBuilder.<T, AchievementArgumentType.Result>argument("Achievement ID", new AchievementArgumentType())
                .executes(commandContext -> {
                    AchievementArgumentType.Result achievementResult = commandContext.getArgument("Achievement ID", AchievementArgumentType.Result.class);
                    if (achievementResult.isWildcard())
                    {
                        for (Achievement achievement : Achievement.values())
                        {
                            achievement.unlock();
                        }
                        scathaPro.chatManager.sendDevChatMessage("All achievements unlocked");
                    }
                    else
                    {
                        Achievement achievement = achievementResult.getAchievement();
                        if (scathaPro.getProfileData().unlockedAchievements.isUnlocked(achievement))
                        {
                            scathaPro.chatManager.sendChatErrorMessage("Achievement \"" + achievement.achievementName + "\" is already unlocked");
                        }
                        else achievement.unlock();
                    }
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("revokeAchievement")
            .requires(devModeRequirement)
            .executes(getMissingArgumentsCommand())
            .then(RequiredArgumentBuilder.<T, AchievementArgumentType.Result>argument("Achievement ID", new AchievementArgumentType())
                .executes(commandContext -> {
                    AchievementArgumentType.Result achievementResult = commandContext.getArgument("Achievement ID", AchievementArgumentType.Result.class);
                    if (achievementResult.isWildcard())
                    {
                        for (Achievement a : Achievement.values())
                        {
                            scathaPro.achievementManager.revokeAchievement(a);
                        }
                        scathaPro.chatManager.sendDevChatMessage("All achievements revoked");
                    }
                    else
                    {
                        Achievement achievement = achievementResult.getAchievement();
                        if (scathaPro.achievementManager.revokeAchievement(achievement))
                        {
                            scathaPro.chatManager.sendDevChatMessage("Achievement \"" + achievement.achievementName + "\" revoked");
                        }
                        else scathaPro.chatManager.sendChatErrorMessage("Achievement \"" + achievement.achievementName + "\" is not unlocked");
                    }
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .then(LiteralArgumentBuilder.<T>literal("trigger")
            .requires(devModeRequirement)
            .executes(getMissingArgumentsCommand())
            .then(buildAlertTitleTrigger(buildContext))
            .then(buildAlertTrigger(buildContext))
            .then(buildPetDropMessageTrigger())
            .then(buildCompareVersionsTrigger())
            .then(buildItemPopupTrigger(buildContext))
        );
    }
    
    private <T> LiteralArgumentBuilder<T> buildAlertTitleTrigger(CommandBuildContext buildContext)
    {
        return LiteralArgumentBuilder.<T>literal("alertTitle")
            .executes(getMissingArgumentsCommand())
            .then(RequiredArgumentBuilder.<T, Component>argument("Title", ComponentArgument.textComponent(buildContext))
                .executes(getMissingArgumentsCommand())
                .then(RequiredArgumentBuilder.<T, Component>argument("Subtitle", ComponentArgument.textComponent(buildContext))
                    .executes(getMissingArgumentsCommand())
                    .then(RequiredArgumentBuilder.<T, Integer>argument("Fade In Ticks", IntegerArgumentType.integer(0))
                        .executes(getMissingArgumentsCommand())
                        .then(RequiredArgumentBuilder.<T, Integer>argument("Stay Ticks", IntegerArgumentType.integer(0))
                            .executes(getMissingArgumentsCommand())
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
            .executes(getMissingArgumentsCommand())
            .then(RequiredArgumentBuilder.<T, String>argument("Alert Field", StringArgumentType.string())
                .executes(getMissingArgumentsCommand())
                .then(RequiredArgumentBuilder.<T, Component>argument("Dynamic Text", ComponentArgument.textComponent(buildContext))
                    .executes(commandContext -> {
                        String alertFieldName = commandContext.getArgument("Alert Field", String.class);
                        Alert alert;
                        try
                        {
                            Field alertField = scathaPro.alertManager.getClass().getDeclaredField(alertFieldName);
                            alert = (Alert) alertField.get(scathaPro.alertManager);
                        }
                        catch (Exception e)
                        {
                            scathaPro.chatManager.sendChatErrorMessage("Alert \"" + alertFieldName + "\" not found");
                            return 0;
                        }
                        
                        Component dynamicText = commandContext.getArgument("Dynamic Text", Component.class);
                        alert.play(scathaPro, dynamicText);
                        
                        return Command.SINGLE_SUCCESS;
                    })
                )
            );
    }
    
    private <T> LiteralArgumentBuilder<T> buildPetDropMessageTrigger()
    {
        return LiteralArgumentBuilder.<T>literal("petDropMessage")
            .executes(getMissingArgumentsCommand())
            .then(RequiredArgumentBuilder.<T, Boolean>argument("Parse Legacy Formatting", BoolArgumentType.bool())
                .executes(commandContext -> {
                    if (BoolArgumentType.getBool(commandContext, "Parse Legacy Formatting"))
                    {
                        scathaPro.chatManager.sendChatMessage(Component.empty().append("§6§lPET DROP! §9Scatha"), false);
                    }
                    else scathaPro.chatManager.sendChatMessage(Constants.generatePetDropMessage(Rarity.EPIC), false);
                    return Command.SINGLE_SUCCESS;
                })
            );
    }
    
    private <T> LiteralArgumentBuilder<T> buildCompareVersionsTrigger()
    {
        return LiteralArgumentBuilder.<T>literal("compareVersions")
            .executes(getMissingArgumentsCommand())
            .then(RequiredArgumentBuilder.<T, String>argument("From", StringArgumentType.string())
                .executes(getMissingArgumentsCommand())
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
    
    private <T> LiteralArgumentBuilder<T> buildItemPopupTrigger(CommandBuildContext buildContext)
    {
        return LiteralArgumentBuilder.<T>literal("itemPopup")
            .executes(getMissingArgumentsCommand())
            .then(RequiredArgumentBuilder.<T, ItemInput>argument("Item", ItemArgument.item(buildContext))
                .executes(getMissingArgumentsCommand())
                .then(RequiredArgumentBuilder.<T, Integer>argument("Animation Ticks", IntegerArgumentType.integer(1))
                    .executes(getMissingArgumentsCommand())
                    .then(RequiredArgumentBuilder.<T, Boolean>argument("Alternative Rotation Animation Curve", BoolArgumentType.bool())
                        .executes(getMissingArgumentsCommand())
                        .then(RequiredArgumentBuilder.<T, Boolean>argument("Angled", BoolArgumentType.bool())
                            .executes(context -> {
                                ItemInput itemInput = ItemArgument.getItem(context, "Item");
                                int animationTicks = IntegerArgumentType.getInteger(context, "Animation Ticks");
                                boolean alternativeRotAnimCurve = BoolArgumentType.getBool(context, "Alternative Rotation Animation Curve");
                                boolean angled = BoolArgumentType.getBool(context, "Angled");;
                                scathaPro.itemPopupRenderer.popup(
                                    itemInput.createItemStack(1, false), animationTicks, alternativeRotAnimCurve, angled
                                );
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                )
            );
    }
    
    
    private static class AchievementArgumentType implements ArgumentType<AchievementArgumentType.Result>
    {
        public static class Result
        {
            private final Achievement achievement;
            
            private Result(Achievement achievement)
            {
                this.achievement = achievement;
            }
            
            public static Result of(Achievement achievement)
            {
                return new Result(achievement);
            }
            
            public static Result wildcard()
            {
                return new Result(null);
            }
            
            public boolean isWildcard()
            {
                return achievement == null;
            }
            
            public Achievement getAchievement()
            {
                if (isWildcard()) throw new RuntimeException("Cannot get achievement from wildcard result");
                return achievement;
            }
        }
        
        @Override
        public Result parse(StringReader reader) throws CommandSyntaxException
        {
            if (reader.canRead() && reader.peek() == '*')
            {
                reader.skip();
                return Result.wildcard();
            }
            
            String id = reader.readUnquotedString();
            try
            {
                return Result.of(Achievement.valueOf(id));
            }
            catch (Exception e)
            {
                throw new SimpleCommandExceptionType(Component.literal("Achievement with ID \"" + id + "\" doesn't exist")).create();
            }
        }
        
        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder)
        {
            builder.suggest("*", Component.literal("All achievements"));
            for (Achievement achievement : Achievement.values())
            {
                builder.suggest(achievement.id, Component.literal(achievement.achievementName));
            }
            return builder.buildFuture();
        }
    }
}
