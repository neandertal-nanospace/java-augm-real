package neandertal.jaugre.core;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import neandertal.jaugre.core.data.Container;
import neandertal.jaugre.core.data.Region;


/**
 * STEP 1.
 * Image is divided in small regions of NxM pixels for easier computation later on.
 * 
 * @author neandertal
 */
public class RegionSplitter
{
    /**
     * Default region width
     */
    public static final int DEFAULT_REGION_WIDTH = 40;
    
    /**
     * Default region height
     */
    public static final int DEFAULT_REGION_HEIGHT = 40;
    
    /**
     * Inset on the image. Basically a frame, which denotes pixels, ehich won't be processed.
     * This is the minimum possible inset from all edges.
     */
    public static final int DEFAULT_INSET = 2;

    /**
     * Image is split to regions with default width and height
     * 
     * @param image
     * @return map of region names and regions
     */
    public static Map<String, Region> splitToRegions(Container image)
    {
        return splitToRegions(image, DEFAULT_REGION_WIDTH, DEFAULT_REGION_HEIGHT);
    }

    /**
     * Image is split to regions with width and height using default insets
     * 
     * @param image contains all info
     * @param regionWidthArg region width
     * @param regionHeightArg region height
     * @return map of region names and regions
     */
    public static Map<String, Region> splitToRegions(Container image, int regionWidthArg, int regionHeightArg)
    {
        return splitToRegions(image, regionWidthArg, regionHeightArg, DEFAULT_INSET, DEFAULT_INSET, DEFAULT_INSET, DEFAULT_INSET);
    }

    /**
     * Image is split to regions with width and height according insets
     * 
     * @param image contains all info
     * @param regionWidthArg custom region width
     * @param regionHeightArg custom region height
     * @param insetTop custom top inset
     * @param insetLeft custom left inset
     * @param insetBottom custom bottom inset
     * @param insetRight custom right inset
     * @return map of region names and regions
     */
    public static Map<String, Region> splitToRegions(Container image, int regionWidthArg, int regionHeightArg, int insetTop, int insetLeft, int insetBottom,
            int insetRight)
    {
        if (image == null || image.getImage() == null)
        {
            throw new IllegalArgumentException("Image can't be NULL");
        }

        if (regionWidthArg <= 0)
        {
            throw new IllegalArgumentException("Region width must be positive!");
        }

        if (regionHeightArg <= 0)
        {
            throw new IllegalArgumentException("Region height must be positive!");
        }

        if (insetTop < DEFAULT_INSET || insetLeft < DEFAULT_INSET || insetBottom < DEFAULT_INSET || insetRight < DEFAULT_INSET)
        {
            throw new IllegalArgumentException("Insets need to be positive");
        }

        splitImageToRegions(image, regionWidthArg, regionHeightArg, insetTop, insetLeft, insetBottom, insetRight);

        return image.getRegionsMap();
    }

    private static void splitImageToRegions(Container image, int regWidth, int regHeight, int top, int left, int bottom, int right)
    {
        Map<String, Region> regionsMap = new HashMap<String, Region>();

        BufferedImage img = image.getImage();

        int imgHeight = img.getHeight() - bottom;
        int imgWidth = img.getWidth() - right;

        for (int k = left, s = 0; k < imgWidth; k += regWidth, s++)
        {
            for (int l = top, t = 0; l < imgHeight; l += regHeight, t++)
            {
                Region reg = new Region(s + "x" + t, k, l, Math.min(regWidth, imgWidth - k), Math.min(regHeight, imgHeight - l));
                regionsMap.put(reg.getName(), reg);
            }// for
        }// for

        image.setTopInset(top);
        image.setBottomInset(bottom);
        image.setLeftInset(left);
        image.setRightInset(right);
        image.setRegionsMap(regionsMap);
    }
}
