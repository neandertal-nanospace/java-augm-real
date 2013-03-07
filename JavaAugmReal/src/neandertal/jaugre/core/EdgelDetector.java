package neandertal.jaugre.core;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import neandertal.jaugre.core.data.Container;
import neandertal.jaugre.core.data.Edgel;
import neandertal.jaugre.core.data.EdgelTypeEnum;
import neandertal.jaugre.core.data.Region;


/**
 * STEP 2.
 * Detects edgels in image, for every region. Edgels are detected along
 * scan lines, which are at X pixel distance. Both horizontal and vertical
 * scan lines are used, which produces vertical and horizontal edgels.
 * <br/>
 * Uses:<br/>
 * <a href="http://www.stat.wisc.edu/~mchung/teaching/MIA/reading/diffusion.gaussian.kernel.pdf">Gaussian Kernel</a><br/>
 * <a href="http://homepages.inf.ed.ac.uk/rbf/HIPR2/convolve.htm">Convolution</a><br/>
 * <a href="http://homepages.inf.ed.ac.uk/rbf/HIPR2/sobel.htm">Sobel Kernel</a>
 * 
 * @author neandertal
 * 
 */
public class EdgelDetector
{
    /** Distance between the scan lines */
    public static final int DEFAULT_SCANLINE_DISTANCE = 5;

    /**
     * Gauss kernel, used to calculate each point Gauss function 
     */
    public static final int[] GAUSS_KERNEL = new int[] { -3, -5, 0, 5, 3 };

    /**
     * Sobel kernel, used to calculate gradient of a point
     */
    public static final int[] SOBEL_KERNEL = new int[] { 1, 2, 1, 0, 0, 0, -1, -2, -1 };

    /**
     * Frame - pixels within the frame can not be processed.
     */
    private static final int pixFrame = GAUSS_KERNEL.length / 2;

    /**
     * Lower limit for the Gauss function for each RGB channel pixel. Only if in
     * all 3 channels the pixel Gauss value exceeds this threshold, can it be
     * candidate for an edgel.
     */
    public static final int DEFAULT_THRESHOLD = 256;// TODO why this value?

    /**
     * Detects edgels in image, for every region.
     * 
     * @param image contains all needed data
     * @return map of region names to the list of region's edgels
     */
    public static Map<String, List<Edgel>> detectEdgels(Container image)
    {
        return detectEdgels(image, DEFAULT_THRESHOLD);
    }

    /**
     * Detects edgels in image, for every region. Using given threshold for the
     * RGB channels.
     * 
     * @param image contains all needed data
     * @param thresholdArg custom threshold for all channels
     * @return map of region names to the list of region's edgels
     */
    public static Map<String, List<Edgel>> detectEdgels(Container image, int thresholdArg)
    {
        return detectEdgels(image, thresholdArg, DEFAULT_SCANLINE_DISTANCE);
    }

    /**
     * Detects edgels in image, for every region. Using given distance between
     * scan lines and given threshold for the RGB channels.
     * 
     * @param image contains all needed data
     * @param thresholdArg custom threshold for all channels
     * @param scanLineDistArg distance between scan lines
     * @return map of region names to the list of region's edgels
     */
    public static Map<String, List<Edgel>> detectEdgels(Container image, int thresholdArg, int scanLineDistArg)
    {
        return detectEdgels(image, thresholdArg, thresholdArg, thresholdArg, scanLineDistArg, scanLineDistArg);
    }

    /**
     * Detects edgels in image, for every region. Using given distance between
     * scan lines in horizontal and vertical scan, and given threshold for every
     * R,G,B channel.
     * 
     * @param image contains all needed data
     * @param redThresholdArg custom threshold for R channel
     * @param greenThresholdArg custom threshold for G channels
     * @param blueThresholdArg custom threshold for B channels
     * @param vScanLineDistArg distance between vertical scan lines
     * @param hScanLineDistArg distance between horizontal scan lines
     * @return map of region names to the list of region's edgels
     */
    public static Map<String, List<Edgel>> detectEdgels(Container image, int redThresholdArg, int greenThresholdArg,
            int blueThresholdArg, int vScanLineDistArg, int hScanLineDistArg)
    {
        if (image == null || image.getImage() == null)
        {
            throw new IllegalArgumentException("Image can't be NULL!");
        }

        if (image.getRegionsMap() == null)
        {
            throw new IllegalArgumentException("Regions map can't be NULL!");
        }

        if (vScanLineDistArg <= 0 || hScanLineDistArg <= 0)
        {
            throw new IllegalArgumentException("Scan line distance must be positive!");
        }

        if (redThresholdArg > 2040 || greenThresholdArg > 2040 || blueThresholdArg > 2040)
        {
            throw new IllegalArgumentException("Threshold is out of range!");
        }

        internalDetectEdgels(image, redThresholdArg, greenThresholdArg, blueThresholdArg, vScanLineDistArg,
                hScanLineDistArg);

        return image.getEdgelsMap();
    }

    // Detect edgels for each region
    private static void internalDetectEdgels(Container image, int redThreshold, int greenThreshold, int blueThreshold,
            int vScanLineDist, int hScanLineDist)
    {
        Map<String, List<Edgel>> edgelsPerRegion = new HashMap<String, List<Edgel>>();

        Collection<Region> regions = image.getRegionsMap().values();

        Iterator<Region> iter = regions.iterator();
        while (iter.hasNext())
        {
            Region region = iter.next();
            List<Edgel> edgelsInRegion = detectEdgelsInRegion(region, image.getImage(), redThreshold, greenThreshold,
                    blueThreshold, vScanLineDist, hScanLineDist);

            edgelsPerRegion.put(region.getName(), edgelsInRegion);
        }

        image.setEdgelsMap(edgelsPerRegion);
    }

    // Return list of edgels in the given region
    private static List<Edgel> detectEdgelsInRegion(Region region, BufferedImage img, int redThreshold,
            int greenThreshold, int blueThreshold, int vScanLineDistance, int hScanLineDistance)
    {
        List<Edgel> edgelsList = new LinkedList<Edgel>();

        // Vertical
        int upper = region.getX() + region.getWidth();
        for (int i = region.getX(); i < upper; i += vScanLineDistance)
        {
            int[] scanline = new int[region.getHeight() + 2 * pixFrame];
            scanline = img.getRGB(i, region.getY() - pixFrame, 1, scanline.length, scanline, 0, 1);

            int[] vEdgelsPos = detectEdgelsInScanline(scanline, redThreshold, greenThreshold, blueThreshold);

            for (int j = 0; j < vEdgelsPos.length; j++)
            {
                Edgel edgel = new Edgel();
                edgel.setX(i);
                edgel.setY(region.getY() - pixFrame + vEdgelsPos[j]);
                edgel.setType(EdgelTypeEnum.VERTICAl);
                int[] area = img.getRGB(edgel.getX() - 1, edgel.getY() - 1, 3, 3, null, 0, 3);
                edgel.setDirection(calculateDirection(area));

                edgelsList.add(edgel);
            }
        }

        // horizontal
        upper = region.getY() + region.getHeight();
        for (int i = region.getY(); i < upper; i += hScanLineDistance)
        {
            int[] scanline = new int[region.getWidth() + 2 * pixFrame];
            scanline = img.getRGB(region.getX() - pixFrame, i, scanline.length, 1, scanline, 0, 1);

            int[] hEdgelsPos = detectEdgelsInScanline(scanline, redThreshold, greenThreshold, blueThreshold);

            for (int j = 0; j < hEdgelsPos.length; j++)
            {
                Edgel edgel = new Edgel();
                edgel.setX(region.getX() - pixFrame + hEdgelsPos[j]);
                edgel.setY(i);
                edgel.setType(EdgelTypeEnum.HORIZONTAL);
                int[] area = img.getRGB(edgel.getX() - 1, edgel.getY() - 1, 3, 3, null, 0, 3);
                edgel.setDirection(calculateDirection(area));

                edgelsList.add(edgel);
            }
        }

        return edgelsList;
    }

    // Return array with the positions of the edgels in the scan line
    private static int[] detectEdgelsInScanline(int[] scanline, int redThreshold, int greenThreshold, int blueThreshold)
    {
        int[] redChannel = new int[scanline.length];
        int[] greenChannel = new int[scanline.length];
        int[] blueChannel = new int[scanline.length];

        // extract channels
        for (int i = 0; i < scanline.length; i++)
        {
            redChannel[i] = getRedColor(scanline[i]);
            greenChannel[i] = -256;// process on demand
            blueChannel[i] = -256;// process on demand
        }

        int foundEdgels = 0;
        int[] fEdgelsPos = new int[scanline.length / 2];
        int prev2 = 0;
        int prev1 = 0;
        int current = 0;
        for (int i = pixFrame; i < scanline.length - pixFrame; i++)
        {
            prev2 = prev1;
            prev1 = current;
            current = 0;

            int outputRed = calculateRedOutputValue(redChannel, i);
            if (outputRed < redThreshold)
            {
                // Not edge for red channel
                continue;
            }

            int outputGreen = calculateGreenOutputValue(greenChannel, i, scanline);
            if (outputGreen < greenThreshold)
            {
                // Not edge for green channel
                continue;
            }

            int outputBlue = calculateBlueOutputValue(blueChannel, i, scanline);
            if (outputBlue < blueThreshold)
            {
                // Not edge for blue channel
                continue;
            }

            // check for local maxima
            current = outputRed;
            if (prev1 > 0 && prev1 >= prev2 && prev1 > current)
            {
                // previous one is an edgel
                fEdgelsPos[foundEdgels] = i - 1;
                foundEdgels++;
            }
        }

        return Arrays.copyOf(fEdgelsPos, foundEdgels);
    }

    //Calculate Gauss function for pixel at pos for the R channel
    protected static int calculateRedOutputValue(int[] redChannel, int pos)
    {
        int output = 0;
        output += redChannel[pos - 2] * GAUSS_KERNEL[0];
        output += redChannel[pos - 1] * GAUSS_KERNEL[1];
        output += redChannel[pos + 1] * GAUSS_KERNEL[3];
        output += redChannel[pos + 2] * GAUSS_KERNEL[4];

        return Math.abs(output);
    }

    //Calculate Gauss function for pixel at position for the G channel
    private static int calculateGreenOutputValue(int[] greenChannel, int pos, int[] scanline)
    {
        int output = 0;
        output += getGreenValue(greenChannel, pos - 2, scanline) * GAUSS_KERNEL[0];
        output += getGreenValue(greenChannel, pos - 1, scanline) * GAUSS_KERNEL[1];
        output += getGreenValue(greenChannel, pos + 1, scanline) * GAUSS_KERNEL[3];
        output += getGreenValue(greenChannel, pos + 2, scanline) * GAUSS_KERNEL[4];

        return Math.abs(output);
    }

    //Lazily fill the green channel of the scan line
    private static int getGreenValue(int[] greenChannel, int k, int[] scanline)
    {
        if (greenChannel[k] < -255)
        {
            greenChannel[k] = (scanline[k] >> 8) & 0xFF;
        }
        return greenChannel[k];
    }

    //Calculate Gauss function for pixel at position for the B channel
    private static int calculateBlueOutputValue(int[] blueChannel, int pos, int[] scanline)
    {
        int output = 0;
        output += getBlueValue(blueChannel, pos - 2, scanline) * GAUSS_KERNEL[0];
        output += getBlueValue(blueChannel, pos - 1, scanline) * GAUSS_KERNEL[1];
        output += getBlueValue(blueChannel, pos + 1, scanline) * GAUSS_KERNEL[3];
        output += getBlueValue(blueChannel, pos + 2, scanline) * GAUSS_KERNEL[4];

        return Math.abs(output);
    }

    //Lazily fill the blue channel of the scan line
    private static int getBlueValue(int[] blueChannel, int k, int[] scanline)
    {
        if (blueChannel[k] < -255)
        {
            blueChannel[k] = scanline[k] & 0xFF;
        }
        return blueChannel[k];
    }

    /**
     * Using Sobel kernel calculate direction of the edgel
     * 
     * @param area area around the pixel, needed to apply the Sobel matrix
     * @return direction normalized vector
     */
    protected static float[] calculateDirection(int[] area)
    {
        float[] result = new float[2];

        // calculate x
        result[0] += getRedColor(area[0]);
        result[0] -= getRedColor(area[8]);
        result[1] = result[0];

        result[0] -= getRedColor(area[2]);
        result[0] += getRedColor(area[3]) * 2;
        result[0] -= getRedColor(area[5]) * 2;
        result[0] += getRedColor(area[6]);

        // calculate y
        result[1] += getRedColor(area[1]) * 2;
        result[1] += getRedColor(area[2]);
        result[1] -= getRedColor(area[6]);
        result[1] -= getRedColor(area[7]) * 2;

        // normalize vector
        float length = (float) Math.sqrt(result[0] * result[0] + result[1] * result[1]);
        result[0] = result[0] / length;
        result[1] = result[1] / length;
        return result;
    }

    private static int getRedColor(int pix)
    {
        return (pix >> 16) & 0xFF;
    }
}
