package neandertal.jaugre.core.data;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * Line segment, denotes a part of an edge, detected by combining edgels.
 * Has a start, end, and direction, as well as inliner Edgels.
 * @author neandertal
 */
public class LineSegment
{
    private Edgel start;
    private Edgel end;
    private float[] direction;
    private List<Edgel> inliners;
    private boolean startCorner;
    private boolean endCorner;
    
    public LineSegment(Edgel s, Edgel e)
    {
        start = s;
        end = e;
        direction = s.getDirection();
        inliners = new LinkedList<Edgel>();
        inliners.add(s);
        inliners.add(e);
    }
    
    private LineSegment() {}
    
    public void setStart(Edgel start)
    {
        this.start = start;
    }
    
    public void setEnd(Edgel end)
    {
        this.end = end;
    }
    
    public Edgel getEnd()
    {
        return end;
    }
    
    public Edgel getStart()
    {
        return start;
    }
    
    public float[] getDirection()
    {
        return direction;
    }
    
    public void setDirection(float[] direction)
    {
        this.direction = direction;
    }
    
    public List<Edgel> getInliners()
    {
        return inliners;
    }
    
    public void addInliner(Edgel e)
    {
        inliners.add(1, e);
    }
    
    public boolean isStartCorner()
    {
        return startCorner;
    }
    
    public void setStartCorner(boolean startCorner)
    {
        this.startCorner = startCorner;
    }
    
    public boolean isEndCorner()
    {
        return endCorner;
    }
    
    public void setEndCorner(boolean endCorner)
    {
        this.endCorner = endCorner;
    }
    
    @Override
    public LineSegment clone()
    {
        LineSegment ls = new LineSegment();
        ls.start = start;
        ls.end = end;
        ls.direction = new float[]{direction[0], direction[1]};
        ls.inliners = new LinkedList<Edgel>();
        ls.startCorner = startCorner;
        ls.endCorner = endCorner;
        return ls;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", start=[").append(start.toString()).append("]");
        sb.append(", end=[").append(end.toString()).append("]");;
        sb.append(", direction=").append(Arrays.toString(direction));
        sb.append(", inliners=").append(Arrays.toString(inliners.toArray()));
        sb.append(", startCorner=").append(startCorner);
        sb.append(", endCorner=").append(endCorner);
        return sb.toString();
    }
}
