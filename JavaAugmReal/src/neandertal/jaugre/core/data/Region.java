package neandertal.jaugre.core.data;

/**
 * Denotes a region in the image
 * @author neandertal
 */
public class Region
{
    private String name;
    private int x;
    private int y;
    private int width;
    private int height;

    public Region(String name, int x, int y, int width, int height)
    {
        super();
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    public String getName()
    {
        return name;
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", name=").append(name);
        sb.append(", x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", width=").append(width);
        sb.append(", height=").append(height);
        return sb.toString();
    }
}
