package neandertal.jaugre.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import neandertal.jaugre.core.data.Chain;
import neandertal.jaugre.core.data.Container;
import neandertal.jaugre.core.data.LineSegment;


/**
 * STEP 7.
 * Finds chains of lines. We try to find chains of 3 to 4 lines. A chain is a
 * list of lines where the end of one of the lines hits the start of another.
 * @author neandertal
 */
public class ChainsDetector
{
    // Threshold for 2 lines to be parallel
    public static final float DEFAULT_LINES_COMPATIBILITY = 0.92f;
    // Maximum squared distance between edges of lines
    public static final float DEFAULT_SQUARED_LINES_DISTANCE = 16f;
    // Minimum chained lines to take into consideration
    public static final float DEFAULT_MIN_LINES = 3;
    // Maximum chained lines
    public static final float DEFAULT_MAX_LINES = 4;

    /**
     * Find chains of lines, which could make a quadrangle
     * 
     * @param image
     * @return
     */
    public static Collection<Chain> findChains(Container image)
    {
        if (image == null)
        {
            throw new IllegalArgumentException("Image can't be NULL!");
        }

        if (image.getCornerSegments() == null)
        {
            throw new IllegalArgumentException("Corner segments can't be NULL!");
        }

        findChainsInternal(image);

        return image.getChains();
    }

    //For simplicity we assume every line can have max 1 previous and next line
    private static void findChainsInternal(Container image)
    {
        // create chain element for each line
        List<ChainElement> chainElementsList = new LinkedList<ChainElement>();
        for (LineSegment line : image.getCornerSegments())
        {
            ChainElement chEl = new ChainElement();
            chEl.line = line;
            chainElementsList.add(chEl);
        }

        // link chain elements
        for (int i = 0; i < chainElementsList.size(); i++)
        {
            // every line try to find its connecting lines
            ChainElement chainElement = chainElementsList.get(i);
            for (int j = i + 1; j < chainElementsList.size(); j++)
            {
                ChainElement toCheck = chainElementsList.get(j);
                // check if to add at end
                if (checkIfLinesConnect(chainElement.line, toCheck.line))
                {
                    chainElement.next = toCheck;
                    toCheck.previous = chainElement;
                    continue;// It's very unlikely 2 lines to form a loop
                }

                // check if to add at start
                if (checkIfLinesConnect(toCheck.line, chainElement.line))
                {
                    toCheck.next = chainElement;
                    chainElement.previous = toCheck;
                }
            }// for
        }// for

        // find chains recursively
        Set<Chain> allChains = growChains(chainElementsList);

        image.setChains(allChains);
    }// findChainsInternal

    // Find the chains, we assume every chain element can have max 1 previous and 1 next element
    //but it is possible to form a loop
    private static Set<Chain> growChains(List<ChainElement> cheinElementsList)
    {
        List<List<ChainElement>> chains = new LinkedList<List<ChainElement>>();

        while (!cheinElementsList.isEmpty())
        {
            ChainElement chainElement = cheinElementsList.remove(0);

            // lonely elements are discarded
            if (chainElement.next == null && chainElement.previous == null)
            {
                continue;
            }

            //build chain
            List<ChainElement> newChain = new LinkedList<ChainElement>();
            newChain.add(chainElement);
            chains.add(newChain);
            
            //extend beginning
            ChainElement prev = chainElement.previous;
            while (prev != null && !newChain.contains(prev))
            {
                newChain.add(0, prev);
                prev = prev.previous;
            }
            
            //extends end
            ChainElement next = chainElement.next;
            while (next != null && !newChain.contains(next))
            {
                newChain.add(next);
                next = next.next;
            }
            
            //remove iterated elements
            cheinElementsList.removeAll(newChain);
        }

        // finally get only appropriate chains
        Set<Chain> allChains = new HashSet<Chain>();
        for (List<ChainElement> chainList : chains)
        {
            if (chainList.size() >= DEFAULT_MIN_LINES &&  chainList.size() <= DEFAULT_MAX_LINES)
            {
                allChains.add(createChain(chainList));
            }
        }

        return allChains;
    }

    // Check if end of first line is at the start of the second one
    private static boolean checkIfLinesConnect(LineSegment segment, LineSegment toCompare)
    {
        // same segment
        if (segment == toCompare)
        {
            return false;
        }

        // check if lines are parallel
        if (SegmentsFinder.isOrientationCompatible(segment.getDirection(), toCompare.getDirection(),
                DEFAULT_LINES_COMPATIBILITY))
        {
            return false;
        }

        // Distance between edges is too great
        if (SegmentsMerger.getSquaredDistance(segment.getEnd(), toCompare.getStart()) > DEFAULT_SQUARED_LINES_DISTANCE)
        {
            return false;
        }

        // Dot product of directions, so that only clock-wise connected lines
        // remain
        if (!checkDirections(segment.getDirection(), toCompare.getDirection()))
        {
            return false;
        }

        return true;
    }

    // check if directions are compatible
    private static boolean checkDirections(float[] dirA, float[] dirB)
    {
        return dirA[0] * dirB[1] - dirA[1] * dirB[0] >= 0;
    }

    private static Chain createChain(List<ChainElement> chElements)
    {
        Chain ch = new Chain();
        for (ChainElement chEl : chElements)
        {
            ch.addAtEnd(chEl.line);
        }

        return ch;
    }

    // Chain element
    private static class ChainElement
    {
        private LineSegment line;
        private ChainElement previous;
        private ChainElement next;

        public ChainElement(){}
    }
}
