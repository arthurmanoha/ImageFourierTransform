package imagefouriertransform;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/**
 *
 * @author arthu
 */
public class ImagePanel extends JPanel {

    protected MyImage image;

    // Apparent size of a single pixel
    protected int zoom;

    // When this panel represents a Fourier image, the values must be remapped
    boolean mustChangeScale;

    private boolean displayValues;

    protected boolean isMouseButtonPressed;

    private Color errorColor = Color.orange;

    public ImagePanel(MyImage imageParam) {
        image = imageParam;
        zoom = 10;
        displayValues = false;
        mustChangeScale = false;
        isMouseButtonPressed = false;

    }

    public ImagePanel(MyImage imageParam, boolean mustChangeScaleParam) {
        this(imageParam);
        this.mustChangeScale = mustChangeScaleParam;
    }

    public MyImage getImage() {
        return image;
    }

    public void setImage(MyImage newImage) {
        this.image = newImage;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, g.getClipBounds().width, g.getClipBounds().height);

        for (int row = 0; row < image.getHeight(); row++) {
            for (int col = 0; col < image.getWidth(); col++) {

                // /////////////////////////////////////////////////////////////
                // Paint pixel for image
                // /////////////////////////////////////////////////////////////
                int imageValue = image.getRealPart(row, col);
                // [0, 255]
                g.setColor(chooseColor(imageValue));
                g.fillRect(col * zoom, row * zoom, zoom, zoom);

                if (displayValues) {
                    g.setColor(Color.CYAN.darker());
                    g.drawString("" + imageValue, (int) ((col + 0.5) * zoom), (int) ((row + 0.5) * zoom));
                }
            }
        }

        // Draw a rectangle around the image
        g.setColor(Color.red);
        g.drawRect(0, 0, image.getWidth() * zoom, image.getHeight() * zoom);
    }

    public void zoomIn() {
        zoom *= 2;
        computePreferredSize();
        repaint();
    }

    public void zoomOut() {
        zoom /= 2;
        computePreferredSize();
        repaint();
    }

    protected void computePreferredSize() {
        int prefWidth = zoom * image.getWidth();
        int prefHeight = zoom * image.getHeight();
        setPreferredSize(new Dimension(prefWidth, prefHeight));
        setSize(new Dimension(prefWidth, prefHeight));
    }

    protected void toggleDisplayValues() {
        displayValues = !displayValues;
    }

    protected int boundToColorValue(int value) {

        if (value > 255) {
            value = 255;
        } else if (value < 0) {
            value = 0;
        }
        return value;
    }

    protected Color chooseColor(int value) {

        value = boundToColorValue(value);
        return new Color(value, value, value);
    }

}
