package imagefouriertransform;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author arthu
 */
public class MyImage {

    Complex[][] array2D;

    /**
     * Create an image from file.
     *
     * @param filename
     */
    public MyImage(String filename) {
        try {
            BufferedImage image = ImageIO.read(new File(filename));

            array2D = new Complex[image.getHeight()][image.getWidth()];

            for (int row = 0; row < image.getHeight(); row++) {
                for (int col = 0; col < image.getWidth(); col++) {
                    int colorInt = image.getRGB(col, row);
                    Color c = new Color(colorInt);
                    int grayLevel = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
                    array2D[row][col] = new Complex(grayLevel, 0);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MyImage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create empty image with given dimensions
     *
     * @param height
     * @param width
     * @param setSymmetrical
     */
    public MyImage(int height, int width, boolean setSymmetrical) {
        this(height, width, setSymmetrical, false);
    }

    /**
     * Create an empty image with given dimensions, with the option of setting
     * it to be symmetrical and/or empty.
     *
     * @param height
     * @param width
     * @param setSymmetrical
     * @param setEmpty
     */
    public MyImage(int height, int width, boolean setSymmetrical, boolean setEmpty) {
        array2D = new Complex[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (setEmpty) {
                    array2D[row][col] = new Complex(0, 0);
                } else {
                    array2D[row][col] = new Complex(new Random().nextInt(255), 0);
                }
            }
        }
        if (setSymmetrical) {
            // Swipe all lines
            for (int row = 0; row < height; row++) {
                // Set each cell after the diagonal, i.e. in the top-right half matrix.
                for (int col = row + 1; col < width; col++) {
                    array2D[row][col] = array2D[col][row];
                }
            }
        }
    }

    /**
     * Create an image by computing the sum of two images.
     *
     * @param imgA
     * @param imgB
     */
    public MyImage(MyImage imgA, MyImage imgB) {

        int height = imgA.getHeight();
        int width = imgA.getWidth();
        this.array2D = new Complex[height][width];

        for (int row = 0; row < array2D.length; row++) {
            for (int col = 0; col < array2D[0].length; col++) {
                Complex sum = (imgA.array2D[row][col]).add(imgB.array2D[row][col]);
                this.array2D[row][col] = sum;
            }
        }
    }

    /**
     * Multiply all pixels of the image by the same value
     *
     * @return
     */
    public MyImage multiply(double multiplicator) {

        MyImage multipliedImage = this.createEmptyClone();

        for (int row = 0; row < array2D.length; row++) {
            for (int col = 0; col < array2D[0].length; col++) {
                multipliedImage.array2D[row][col] = this.array2D[row][col].multiply(multiplicator);
            }
        }
        return multipliedImage;
    }

    int getHeight() {
        return array2D.length;
    }

    int getWidth() {
        return array2D[0].length;
    }

    int getRealPart(int row, int col) {
        return array2D[row][col].getRealPart();
    }

    Complex get(int row, int col) {
        try {
            return array2D[row][col];
        } catch (ArrayIndexOutOfBoundsException e) {
            return new Complex();
        }
    }

    void set(int row, int col, Complex newVal) {
        array2D[row][col] = newVal;
    }

    MyImage createEmptyClone() {
        return new MyImage(this.getHeight(), this.getWidth(), false, true);
    }

    /**
     * Return value of highest pixel
     *
     * @return
     */
    int getMax() {
        int result = 0;
        for (int row = 0; row < array2D.length; row++) {
            for (int col = 0; col < array2D[0].length; col++) {
                int newVal = array2D[row][col].getRealPart();
                if (newVal > result) {
                    result = newVal;
                }
            }
        }
        return result;
    }

    int getMin() {
        int result = 0;
        for (int row = 0; row < array2D.length; row++) {
            for (int col = 0; col < array2D[0].length; col++) {
                int newVal = array2D[row][col].getRealPart();
                if (newVal < result) {
                    result = newVal;
                }
            }
        }
        return result;
    }
}
