package neandertal.jaugre.core.data;

import java.awt.Point;
import java.util.Arrays;


/**
 * Edgel, a point with direction
 * @author neandertal
 */
public class Edgel
{
    private int x;
    private int y;
    private EdgelTypeEnum type;
    private float[] direction;
    
    public int getX()
    {
        return x;
    }
    public void setX(int x)
    {
        this.x = x;
    }
    public int getY()
    {
        return y;
    }
    public void setY(int y)
    {
        this.y = y;
    }
    public EdgelTypeEnum getType()
    {
        return type;
    }
    public void setType(EdgelTypeEnum typeArg)
    {
        type = typeArg;
    }
    
    public float[] getDirection()
    {
        return direction;
    }
    
    
    public void setDirection(float[] directionArg)
    {
        direction = directionArg;
    }
    
    public Point getPoint()
    {
        return new Point(x, y);
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", type=").append(type);
        sb.append(", direction=").append(Arrays.toString(direction));
        return sb.toString();
    }
}
