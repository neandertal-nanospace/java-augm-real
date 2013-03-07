package neandertal.jaugre.core.data;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Image encapsulation, including all detected edgels, regions, etc.
 * TODO: cleanup of some of the data should be made between the steps,
 * so that memory is released of unnecessary objects.
 * @author neandertal
 */
public class Container
{
    private BufferedImage image;
    private int topInset;
    private int leftInset;
    private int bottomInset;
    private int rightInset;
    
    private Map<String, Region> name2RegionMap;
    private Map<String, List<Edgel>> region2EdgelsMap;
    private Map<String, List<LineSegment>> region2SegmentsMap;
    private List<LineSegment> mergedSegments;
    private List<LineSegment> extendedSegments;
    private List<LineSegment> cornerSegments;
    private Collection<Chain> chains;
    private Collection<Quadrangle> quadrangles;
    
    public Container(BufferedImage bImageArg)
    {
        image = bImageArg;
    }
    
    public BufferedImage getImage()
    {
        return image;
    }
    
    public Map<String, Region> getRegionsMap()
    {
        return name2RegionMap;
    }
    
    public Collection<Region> getRegionsCollection()
    {
        if (name2RegionMap == null)
        {
            return null;
        }
        
        return name2RegionMap.values();
    }
    
    public Map<String, List<Edgel>> getEdgelsMap()
    {
        return region2EdgelsMap;
    }
    
    public Collection<Edgel> getEdgelsCollection()
    {
        if (region2EdgelsMap == null)
        {
            return null;
        }
        
        List<Edgel> edgels = new LinkedList<Edgel>();
        Iterator<List<Edgel>> iter = region2EdgelsMap.values().iterator();
        while (iter.hasNext())
        {
            List<Edgel> listEdgel = iter.next();
            edgels.addAll(listEdgel);
        }
        
        return edgels;
    }
    
    public Map<String, List<LineSegment>> getSegmentsMap()
    {
        return region2SegmentsMap;
    }
    
    public Collection<LineSegment> getSegmentsCollection()
    {
        if (region2SegmentsMap == null)
        {
            return null;
        }
        
        List<LineSegment> segments = new LinkedList<LineSegment>();
        Iterator<List<LineSegment>> iter = region2SegmentsMap.values().iterator();
        while (iter.hasNext())
        {
            List<LineSegment> listSeg = iter.next();
            segments.addAll(listSeg);
        }
        
        return segments;
    }
    
    public void setRegionsMap(Map<String, Region> name2RegionMap)
    {
        this.name2RegionMap = name2RegionMap;
    }
    
    public void setEdgelsMap(Map<String, List<Edgel>> region2EdgelsMap)
    {
        this.region2EdgelsMap = region2EdgelsMap;
    }
    
    public void setSegmentsMap(Map<String, List<LineSegment>> region2SegmentsMap)
    {
        this.region2SegmentsMap = region2SegmentsMap;
    }
    
    public List<LineSegment> getMergedSegments()
    {
        return mergedSegments;
    }
    
    public void setMergedSegments(List<LineSegment> mergedSegments)
    {
        this.mergedSegments = mergedSegments;
    }
    
    public List<LineSegment> getExtendedSegments()
    {
        return extendedSegments;
    }
    
    public void setExtendedSegments(List<LineSegment> extendedSegments)
    {
        this.extendedSegments = extendedSegments;
    }
    
    public List<LineSegment> getCornerSegments()
    {
        return cornerSegments;
    }
    
    public void setCornerSegments(List<LineSegment> cornerSegments)
    {
        this.cornerSegments = cornerSegments;
    }
    
    public int getRightInset()
    {
        return rightInset;
    }
    
    public int getLeftInset()
    {
        return leftInset;
    }
    
    public int getTopInset()
    {
        return topInset;
    }
    
    public int getBottomInset()
    {
        return bottomInset;
    }
    
    public void setBottomInset(int bottomInset)
    {
        this.bottomInset = bottomInset;
    }
    
    public void setLeftInset(int leftInset)
    {
        this.leftInset = leftInset;
    }
    
    public void setRightInset(int rightInset)
    {
        this.rightInset = rightInset;
    }
    
    public void setTopInset(int topInset)
    {
        this.topInset = topInset;
    }
    
    public Collection<Chain> getChains()
    {
        return chains;
    }
    
    public void setChains(Collection<Chain> chains)
    {
        this.chains = chains;
    }

    public Collection<Quadrangle> getQuadrangles()
    {
        return quadrangles;
    }

    public void setQuadrangles(Collection<Quadrangle> quadrangles)
    {
        this.quadrangles = quadrangles;
    }
}
