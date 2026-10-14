package imagefouriertransform;

/**
 *
 * @author arthu
 */
public class ImageFourierTransform {

    public static void main(String[] args) {

        String folder = "C:\\Users\\arthu\\Documents\\Programmation\\Java\\ImageFourierTransform\\src\\imagefouriertransform\\";
        String filenameA = "a_128_64.bmp";
        String completeFilenameA = folder + filenameA;
        MyImage imageA = new MyImage(completeFilenameA);

        ImageWindow viewer = new ImageWindow(imageA);

    }

}
