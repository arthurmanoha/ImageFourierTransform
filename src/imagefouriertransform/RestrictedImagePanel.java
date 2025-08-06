package imagefouriertransform;

import static imagefouriertransform.ImageWindow.selectionMode.OFF;
import static imagefouriertransform.ImageWindow.selectionMode.ON;
import static imagefouriertransform.ImageWindow.selectionMode.TOGGLE;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 *
 * @author arthu
 */
public class RestrictedImagePanel extends ImagePanel {

    private ImageWindow.selectionMode currentMode;

    private ImageWindow.selectionMode flags[][];

    private int currentMouseRow, currentMouseCol;
    private int clickRow, clickCol;

    public RestrictedImagePanel(MyImage imageParam) {
        super(imageParam);

        flags = new ImageWindow.selectionMode[image.getHeight()][];
        for (int row = 0; row < image.getHeight(); row++) {
            flags[row] = new ImageWindow.selectionMode[image.getWidth()];
            for (int col = 0; col < image.getWidth(); col++) {
                flags[row][col] = ImageWindow.selectionMode.ON;
            }
        }

        currentMode = ImageWindow.selectionMode.ON;

        this.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                clickRow = e.getY() / zoom;
                clickCol = e.getX() / zoom;
                isMouseButtonPressed = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int releaseRow = e.getY() / zoom;
                int releaseCol = e.getX() / zoom;

                int rowStart = Math.min(clickRow, releaseRow);
                int rowEnd = Math.max(clickRow, releaseRow);
                int colStart = Math.min(clickCol, releaseCol);
                int colEnd = Math.max(clickCol, releaseCol);
                for (int row = rowStart; row < rowEnd; row++) {
                    for (int col = colStart; col < colEnd; col++) {
                        try {
                            // Flag the current pixel with the current mode.
                            if (currentMode == TOGGLE) {
                                flags[row][col] = (flags[row][col] == ON ? OFF : ON);
                            } else {
                                flags[row][col] = currentMode;
                            }
                        } catch (ArrayIndexOutOfBoundsException exception) {
                            // Do not try to set flags outside the array.
                        }
                    }
                }
                isMouseButtonPressed = false;
                repaint();
            }
        });

        this.addMouseMotionListener(
                new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                currentMouseRow = e.getY() / zoom;
                currentMouseCol = e.getX() / zoom;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                currentMouseRow = e.getY() / zoom;
                currentMouseCol = e.getX() / zoom;
                repaint();
            }
        });
    }

    void setMode(ImageWindow.selectionMode newMode) {
        currentMode = newMode;
    }

    /**
     * Paint the image as usual, then add the markers for the disabled
     * pixels.
     *
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int row = 0; row < image.getHeight(); row++) {
            for (int col = 0; col < image.getWidth(); col++) {

                if (flags[row][col] == OFF) {
                    g.drawLine(col * zoom, row * zoom, (col + 1) * zoom, (row + 1) * zoom);
                    g.drawLine((col + 1) * zoom, row * zoom, col * zoom, (row + 1) * zoom);
                }
            }
        }
        if (isMouseButtonPressed) {
            // Draw a rectangle around the pixels that will be set.

            int xDraw0 = clickCol * zoom;
            int yDraw0 = clickRow * zoom;
            int xDraw1 = currentMouseCol * zoom;
            int yDraw1 = currentMouseRow * zoom;
            g.setColor(Color.white);
            g.drawLine(xDraw0, yDraw0, xDraw1, yDraw0);
            g.drawLine(xDraw0, yDraw1, xDraw1, yDraw1);
            g.drawLine(xDraw0, yDraw0, xDraw0, yDraw1);
            g.drawLine(xDraw1, yDraw0, xDraw1, yDraw1);
        }
    }

    protected boolean isActive(int row, int col) {
        return flags[row][col] == ON;
    }

    @Override
    protected Color chooseColor(int value) {

        // Increase the display value of near-zero pixels by taking a square root twice.
        int imageValue = (int) (Math.sqrt((double) value / 255) * 255);
        imageValue = (int) (Math.sqrt((double) imageValue / 255) * 255);
        value = boundToColorValue(imageValue);
        return new Color(value, value, value);
    }
}
