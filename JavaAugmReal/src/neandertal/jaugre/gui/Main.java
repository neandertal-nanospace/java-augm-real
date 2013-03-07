package neandertal.jaugre.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

import neandertal.jaugre.core.ChainsDetector;
import neandertal.jaugre.core.CornerDetector;
import neandertal.jaugre.core.EdgelDetector;
import neandertal.jaugre.core.QuadranglesExtractor;
import neandertal.jaugre.core.RegionSplitter;
import neandertal.jaugre.core.SegmentsExtendor;
import neandertal.jaugre.core.SegmentsFinder;
import neandertal.jaugre.core.SegmentsMerger;
import neandertal.jaugre.core.Tools;
import neandertal.jaugre.core.data.Container;
import neandertal.jaugre.gui.imageframe.ImageScrollPanel;


public class Main
{
    /**
     * Create the GUI and show it. For thread safety, this method should be
     * invoked from the event-dispatching thread.
     */
    private static void createAndShowGUI()
    {
        // Create and set up the window.
        JFrame frame = new JFrame("...");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        BufferedImage img = loadImage("test0.jpg");
        
        if (img == null)
        {
            throw new IllegalArgumentException("No image");
        }

        //Algorithm
        Container image = new Container(img);
        //split into regions
        RegionSplitter.splitToRegions(image);
        //find edgels in regions
        EdgelDetector.detectEdgels(image);
        //find line segments in region
        SegmentsFinder.detectSegments(image);
        //merge line segments in image
        SegmentsMerger.mergeSegments(image);
        //extend line segments in image
        SegmentsExtendor.extendSegments(image);
        //find lines with corners
        CornerDetector.detectCorners(image);
        //Detect chains
        ChainsDetector.findChains(image);
        //Extract Quadrangles
        QuadranglesExtractor.extractQuadrangles(image);
        
        //print numbers
        System.out.println("Edgels  : " + image.getEdgelsCollection().size());
        System.out.println("Segments: " + image.getSegmentsCollection().size());
        System.out.println("Merged  : " + image.getMergedSegments().size());
        System.out.println("Extended: " + image.getExtendedSegments().size());
        System.out.println("Cornered: " + image.getCornerSegments().size());
        System.out.println("Chains  : " + image.getChains().size());
        System.out.println("Quadrang: " + image.getQuadrangles().size());
        
        BufferedImage imgWithSegments = Tools.cloneImage(img);
        Tools.drawRegions(imgWithSegments, image.getRegionsCollection());
        
        //Tools.drawEdgels(imgWithSegments, image.getEdgelsCollection());
        //Tools.drawLineSegments(imgWithSegments, image.getSegmentsCollection());
        //Tools.drawLineSegments(imgWithSegments, image.getMergedSegments());
        //Tools.drawLineSegments(imgWithSegments, image.getExtendedSegments());
        Tools.drawLineSegments(imgWithSegments, image.getCornerSegments());
        //Tools.drawChains(imgWithSegments, image.getChains());
        Tools.drawQuadrangles(imgWithSegments, image.getQuadrangles());
        
        // Create and set up the content pane.
        JComponent newContentPane = new ImageScrollPanel(imgWithSegments);
        newContentPane.setOpaque(true); // content panes must be opaque
        frame.setContentPane(newContentPane);

        frame.setPreferredSize(new Dimension(600, 400));

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args)
    {
        // Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                createAndShowGUI();
            }
        });
    }

    protected static BufferedImage loadImage(String path)
    {
        URL imgURL = Main.class.getClassLoader().getResource(path);

        if (imgURL != null)
        {
            try
            {
                return ImageIO.read(imgURL);
            } 
            catch (IOException e)
            {
                System.err.println("Couldn't find file: " + path);
                return null;
            }
        }
        else
        {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
