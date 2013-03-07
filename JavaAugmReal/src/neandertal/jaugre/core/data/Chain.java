package neandertal.jaugre.core.data;

import java.util.ArrayList;
import java.util.List;


/**
 * Chain of lines
 * @author neandertal
 */
public class Chain
{
    private List<LineSegment> lines;
    
    public Chain()
    {
        lines = new ArrayList<LineSegment>(4);
    }
    
    public List<LineSegment> getLines()
    {
        return lines;
    }
    
    public void addAtEnd(LineSegment line)
    {
        lines.add(line);
    }
    
    public void addAtStart(LineSegment line)
    {
        lines.add(0, line);
    }
    
    public int getLength()
    {
        return lines.size();
    }
    
    public boolean contains(LineSegment line)
    {
        return lines.contains(line);
    }
    
    @Override
    protected Chain clone()
    {
        Chain clone = new Chain();
        clone.lines.addAll(getLines());
        return clone;
    }

    public LineSegment last()
    {
        if (lines.isEmpty()) return null;
        
        return lines.get(lines.size() - 1);
    }
    
    public LineSegment first()
    {
        if (lines.isEmpty()) return null;
        
        return lines.get(0);
    }
}
