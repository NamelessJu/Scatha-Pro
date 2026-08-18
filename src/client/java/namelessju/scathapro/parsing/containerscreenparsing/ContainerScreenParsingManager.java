package namelessju.scathapro.parsing.containerscreenparsing;

import com.google.common.collect.Lists;
import namelessju.scathapro.Constants;
import namelessju.scathapro.ScathaPro;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.util.StringDecomposer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ContainerScreenParsingManager
{
    private final ScathaPro scathaPro;

    private final List<ContainerScreenParser> parsers = Lists.newArrayList();
    private final List<ActiveParser> activeParsers = Lists.newArrayList();

    private ContainerScreen lastCheckedChest = null;
    private int guiTickCounter = 0;
    private int currentParsingTickCountIndex = -1;

    public final WormBestiaryParser wormBestiaryParser;
    public final ProfileStatsParser profileStatsParser;

    public ContainerScreenParsingManager(ScathaPro scathaPro)
    {
        this.scathaPro = scathaPro;

        registerParser(wormBestiaryParser = new WormBestiaryParser(scathaPro));
        registerParser(profileStatsParser = new ProfileStatsParser(scathaPro));
        registerParser(new WitchesStewParser(scathaPro));
        registerParser(new AttributesMenuParser(scathaPro));
    }

    public void registerParser(ContainerScreenParser parser)
    {
        parsers.add(parser);
    }

    public void tick()
    {
        Screen currentScreen = scathaPro.minecraft.screen;
        if (!(currentScreen instanceof ContainerScreen containerScreen))
        {
            finishActiveParsers();
            if (lastCheckedChest != null) profileStatsParser.enabled = false;
            lastCheckedChest = null;
            return;
        }

        if (lastCheckedChest != null && lastCheckedChest == containerScreen)
        {
            if (currentParsingTickCountIndex < 0 || currentParsingTickCountIndex >= Constants.chestGuiParserTickCounts.length) return;
        }
        else
        {
            ScathaPro.LOGGER.debug(getLogMsg("New chest screen opened"));

            finishActiveParsers();

            lastCheckedChest = containerScreen;
            guiTickCounter = -1; // -1 so this tick is 0 after incrementing
            currentParsingTickCountIndex = 0;

            updateParserStates();

            String chestName = StringDecomposer.getPlainText(containerScreen.getTitle());
            for (ContainerScreenParser parser : parsers)
            {
                ActiveParser activeParser = ActiveParser.tryMakeParser(chestName, parser);
                if (activeParser != null) activeParsers.add(activeParser);
            }
            int activeParserCount = activeParsers.size();
            if (activeParserCount == 0)
            {
                ScathaPro.LOGGER.debug(getLogMsg("No (enabled) parsers found for chest screen with name \"" + chestName + "\""));
                currentParsingTickCountIndex = -1;
                return;
            }
            else ScathaPro.LOGGER.debug(getLogMsg("Found " + activeParserCount + " parsers for chest screen with name \"" + chestName + "\""));
        }

        guiTickCounter ++;

        if (guiTickCounter >= Constants.chestGuiParserTickCounts[currentParsingTickCountIndex])
        {
            ScathaPro.LOGGER.debug(getLogMsg("Parsing try #" + (currentParsingTickCountIndex + 1) + " (" + Constants.chestGuiParserTickCounts[currentParsingTickCountIndex] + " ticks after opening GUI)"));

            currentParsingTickCountIndex ++;
            boolean isLastTry = currentParsingTickCountIndex >= Constants.chestGuiParserTickCounts.length;

            Container container = containerScreen.getMenu().getContainer();

            boolean allParsersFinished = true;
            for (ActiveParser activeParser : activeParsers)
            {
                boolean parserFinished = activeParser.parse(container, isLastTry);
                if (!parserFinished) allParsersFinished = false;
            }

            if (allParsersFinished)
            {
                ScathaPro.LOGGER.debug(getLogMsg("All parsers finished"));
                activeParsers.clear();
                currentParsingTickCountIndex = -1;
            }
            else if (isLastTry)
            {
                ScathaPro.LOGGER.warn(getLogMsg("Ran out of parsing tries without all parsers finishing"));
            }
        }
    }

    private void finishActiveParsers()
    {
        if (activeParsers.isEmpty()) return;
        for (ActiveParser activeParser : activeParsers)
        {
            if (activeParser.isFinished()) continue;
            activeParser.parser.onFinishParsing();
            ScathaPro.LOGGER.debug(getLogMsg("Parser {} wasn't finished on menu close"), activeParser.parser.getClass().getSimpleName());
        }
        activeParsers.clear();
    }

    private void updateParserStates()
    {
        wormBestiaryParser.enabled = scathaPro.config.miscellaneous.automaticStatsParsingEnabled.get();
    }

    private static String getLogMsg(String text)
    {
        return "Chest Screen Parsing: " + text;
    }


    private static class ActiveParser
    {
        public final ContainerScreenParser parser;
        public final int[] slots;
        public final boolean[] finishedSlots;
        public boolean finished = false;

        public static ActiveParser tryMakeParser(String chestName, ContainerScreenParser parser)
        {
            if (parser.enabled && parser.shouldParse(chestName)) return new ActiveParser(parser);
            return null;
        }

        private ActiveParser(ContainerScreenParser parser)
        {
            this.parser = parser;
            this.slots = parser.getSlotNumbers();
            this.finishedSlots = new boolean[slots.length];

            parser.onStartParsing();
        }

        public boolean isFinished()
        {
            return finished;
        }

        public boolean parse(Container container, boolean isLastTry)
        {
            if (finished) return true;

            boolean finishedAllSlots = true;
            for (int slotReferenceIndex = 0; slotReferenceIndex < slots.length; slotReferenceIndex++)
            {
                if (finishedSlots[slotReferenceIndex]) continue;

                int slot = slots[slotReferenceIndex];
                if (0 <= slot && slot < container.getContainerSize())
                {
                    ItemStack itemStack = container.getItem(slot);
                    if (itemStack.isEmpty())
                    {
                        finishedAllSlots = false;
                        continue;
                    }
                    parser.tryParse(itemStack, slot);
                }
                else ScathaPro.LOGGER.warn(ContainerScreenParsingManager.getLogMsg("Slot index " + slot + " out of bounds (" + parser.getClass().getSimpleName() + ")"));
                finishedSlots[slotReferenceIndex] = true;
            }

            if (finishedAllSlots || (isLastTry && !parser.requiresFilledSlots()))
            {
                finished = true;
                parser.onFinishParsing();
            }
            return finished;
        }
    }
}