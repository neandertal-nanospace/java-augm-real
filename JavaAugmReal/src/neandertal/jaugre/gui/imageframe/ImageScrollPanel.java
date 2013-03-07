package neandertal.jaugre.gui.imageframe;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;


public class ImageScrollPanel extends JPanel implements ItemListener
{
    private static final long serialVersionUID = 1L;
    private Rule columnView;
    private Rule rowView;
    private JToggleButton isMetric;
    private ScrollablePicture picture;

    public ImageScrollPanel(Image img)
    {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        // Create the row and column headers.
        columnView = new Rule(Rule.HORIZONTAL, true);
        rowView = new Rule(Rule.VERTICAL, true);

        if (img != null)
        {
            columnView.setPreferredWidth(img.getWidth(null));
            rowView.setPreferredHeight(img.getHeight(null));
        } else
        {
            columnView.setPreferredWidth(320);
            rowView.setPreferredHeight(480);
        }

        // Create the corners.
        JPanel buttonCorner = new JPanel(); // use FlowLayout
        isMetric = new JToggleButton("cm", true);
        isMetric.setFont(new Font("SansSerif", Font.PLAIN, 11));
        isMetric.setMargin(new Insets(2, 2, 2, 2));
        isMetric.addItemListener(this);
        buttonCorner.add(isMetric);

        // Set up the scroll pane.
        picture = new ScrollablePicture(new ImageIcon(img), columnView.getIncrement());
        JScrollPane pictureScrollPane = new JScrollPane(picture);
        pictureScrollPane.setPreferredSize(new Dimension(300, 250));
        pictureScrollPane.setViewportBorder(BorderFactory.createLineBorder(Color.black));

        pictureScrollPane.setColumnHeaderView(columnView);
        pictureScrollPane.setRowHeaderView(rowView);

        // Set the corners.
        // In theory, to support internationalization you would change
        // UPPER_LEFT_CORNER to UPPER_LEADING_CORNER,
        // LOWER_LEFT_CORNER to LOWER_LEADING_CORNER, and
        // UPPER_RIGHT_CORNER to UPPER_TRAILING_CORNER. In practice,
        // bug #4467063 makes that impossible (in 1.4, at least).
        pictureScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, buttonCorner);
        pictureScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, new Corner());
        pictureScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new Corner());

        // Put it in this panel.
        add(pictureScrollPane);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    }

    public void itemStateChanged(ItemEvent e)
    {
        if (e.getStateChange() == ItemEvent.SELECTED)
        {
            // Turn it to metric.
            rowView.setIsMetric(true);
            columnView.setIsMetric(true);
        } else
        {
            // Turn it to inches.
            rowView.setIsMetric(false);
            columnView.setIsMetric(false);
        }
        picture.setMaxUnitIncrement(rowView.getIncrement());
    }

}
