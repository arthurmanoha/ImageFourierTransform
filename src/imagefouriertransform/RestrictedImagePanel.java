package imagefouriertransform;

import static imagefouriertransform.ImageWindow.selectionMode.OFF;
import static imagefouriertransform.ImageWindow.selectionMode.ON;
import static imagefouriertransform.ImageWindow.selectionMode.TOGGLE;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;

/**
 *
 * @author arthu
 */
public class RestrictedImagePanel extends ImagePanel {

    private ImageWindow.selectionMode currentMode;

    private ImageWindow.selectionMode flags[][];

    private int currentMouseRow, currentMouseCol;
    private int clickRow, clickCol;

    private ArrayList<RestrictedImagePanel> listeners;

    // Show the pixels even when not selected
    private boolean displayRows;

    /**
     * Create a panel with an empty image of the specified size
     *
     * @param chunkSize
     */
    public RestrictedImagePanel(int chunkSize) {
        super(null);
        currentMode = ImageWindow.selectionMode.OFF;
        image = new MyImage(chunkSize, chunkSize, false, true);
        initializeFlags(chunkSize, chunkSize);
        initializeMouseListeners();
        displayRows = false;
    }

    /**
     * Create a panel with a specified image
     *
     * @param imageParam
     */
    public RestrictedImagePanel(MyImage imageParam) {
        super(imageParam);

        currentMode = ImageWindow.selectionMode.OFF;
        initializeFlags(image.getWidth(), image.getHeight());
        initializeMouseListeners();
        displayRows = false;
    }

    private void initializeMouseListeners() {

        listeners = new ArrayList<>();

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
                updateListeners();
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

    private void initializeFlags(int width, int height) {
        flags = new ImageWindow.selectionMode[height][];
        for (int row = 0; row < height; row++) {
            flags[row] = new ImageWindow.selectionMode[width];
            for (int col = 0; col < width; col++) {
                flags[row][col] = ImageWindow.selectionMode.ON;
            }
        }
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

        if (displayRows) {
            g.setColor(Color.gray);
            int width = g.getClipBounds().width;
            for (int row = 0; row < image.getHeight(); row++) {
                g.drawLine(0, row * zoom, width, row * zoom);
            }
            int height = g.getClipBounds().height;
            for (int col = 0; col < image.getWidth(); col++) {
                g.drawLine(col * zoom, 0, col * zoom, height);
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
        try {
            return flags[row][col] == ON;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    protected Color chooseColor(int value) {

        // Increase the display value of near-zero pixels by taking a square root twice.
        int imageValue = (int) (Math.sqrt((double) value / 255) * 255);
        imageValue = (int) (Math.sqrt((double) imageValue / 255) * 255);
        value = boundToColorValue(imageValue);
        return new Color(value, value, value);
    }

    /**
     * If this string contains a decimal dot, keep at most 2 characters after
     * it.
     *
     * @param s
     * @param nbDecimals
     * @return
     */
    private String trim(String s, int nbDecimals) {
        if (s.contains(".")) {
            int rankOfDot = s.indexOf(".");
            s += "  "; // make sure we have enough characters
            return s.substring(0, rankOfDot + 2);
        } else {
            return s;
        }
    }

    public String getPercentageActive() {
        int totalNbPixels = image.getHeight() * image.getWidth();

        int activeNbPixels = 0;
        for (int row = 0; row < image.getHeight(); row++) {
            for (int col = 0; col < image.getWidth(); col++) {
                if (flags[row][col] == ON) {
                    activeNbPixels++;
                }
            }
        }

        double fraction = (double) activeNbPixels / (double) totalNbPixels * 100;
        return trim(fraction + "", 2) + " % active";
    }

    protected void addListener(RestrictedImagePanel newListener) {
        if (listeners != null) {
            listeners.add(newListener);
        }
    }

    /**
     * Tell the listeners that this panel has changed.
     */
    private void updateListeners() {
        for (RestrictedImagePanel listener : listeners) {
            listener.update(flags);
        }
    }

    private void reset() {
        for (int row = 0; row < flags.length; row++) {
            for (int col = 0; col < flags[0].length; col++) {
                flags[row][col] = OFF;
            }
        }
    }

    /**
     * Use the new information from another panel.
     *
     * @param mask
     */
    private void update(ImageWindow.selectionMode[][] mask) {

        int flagsHeight = mask.length;
        int flagsWidth = mask[0].length;

        int nbChunkRows = this.flags.length / flagsHeight;
        int nbChunkCols = this.flags[0].length / flagsWidth;

        reset();

        // Scan the pixels of the mask, and apply them to all the chunks.
        for (int row = 0; row < flagsHeight; row++) {
            for (int col = 0; col < flagsWidth; col++) {
                // Scan all the chunks
                for (int chunkRow = 0; chunkRow < nbChunkRows; chunkRow++) {
                    for (int chunkCol = 0; chunkCol < nbChunkCols; chunkCol++) {
                        int finalRow = chunkRow * flagsHeight + row;
                        int finalCol = chunkCol * flagsWidth + col;
                        flags[finalRow][finalCol] = mask[row][col];
                    }
                }
            }
        }
        repaint();
    }

    protected void toggleDisplayRows() {
        displayRows = !displayRows;
    }
}
