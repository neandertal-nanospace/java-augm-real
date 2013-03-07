package neandertal.jaugre.gui.imageframe;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

public class Corner extends JComponent
{
    private static final long serialVersionUID = 1L;

    protected void paintComponent(Graphics g)
    {
        // Fill me with dirty brown/orange.
        g.setColor(new Color(230, 163, 4));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
