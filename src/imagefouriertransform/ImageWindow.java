package imagefouriertransform;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import static java.lang.Math.PI;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author arthu
 */
public class ImageWindow extends JFrame implements ComponentListener {

    JPanel imagePanel;
    JScrollPane scroll;
    JPanel buttonsPanel;
    ImagePanel originalImagePanel;
    RestrictedImagePanel transformedImagePanel;
    ImagePanel reconstructedImagePanel;
    JPanel maskSideBar;
    RestrictedImagePanel maskPanel;
    JLabel labelTransformed;

    int imageInitWidth = 200;
    int imageInitHeight = 100;

    ArrayList<JButton> buttonList = new ArrayList<>();

    int chunkSize = 8;
    boolean useCenteredChunks = true;
    double percentage; // [0; 100]

    @Override
    public void componentResized(ComponentEvent e) {
        scroll.revalidate();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
    }

    @Override
    public void componentShown(ComponentEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
    }

    // The different ways we can interact with the pixels of the transformed image
    enum selectionMode {
        ON, OFF, TOGGLE
    }

    private void prepareComponent(JComponent comp, int col, int row, GridBagLayout layout) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = col;
        constraints.gridy = row;
        layout.setConstraints(comp, constraints);
        percentage = 0;
    }

    public ImageWindow(MyImage imgA) {
        super();

        this.addComponentListener(this);

        this.setLayout(new BorderLayout());

        imagePanel = new WrappingPanel();

        JPanel controlPanel = new JPanel();
        createButtonsForControlPanel(controlPanel);

        this.add(controlPanel, BorderLayout.SOUTH);

        //      Add the images
        // First image
        originalImagePanel = new ImagePanel(imgA);
        originalImagePanel.setSize(imageInitWidth, imageInitHeight);
        imagePanel.add(originalImagePanel);

        // Transformed image
        transformedImagePanel = new RestrictedImagePanel(imgA.createEmptyClone(), chunkSize);
        transformedImagePanel.setSize(imageInitWidth, imageInitHeight);
        imagePanel.add(transformedImagePanel);

        // Reconstructed image
        reconstructedImagePanel = new ImagePanel(imgA.createEmptyClone());
        reconstructedImagePanel.setSize(imageInitWidth, imageInitHeight);
        imagePanel.add(reconstructedImagePanel);

        labelTransformed = new JLabel();
        imagePanel.add(labelTransformed);

        scroll = new JScrollPane(imagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        this.add(scroll, BorderLayout.CENTER);

        // Mask panel
        maskSideBar = new JPanel();
        maskSideBar.setLayout(new BoxLayout(maskSideBar, BoxLayout.Y_AXIS));
        maskPanel = new RestrictedImagePanel(chunkSize);
        maskPanel.zoom *= 5;
        maskPanel.toggleDisplayRows();
        maskSideBar.add(maskPanel);
        maskPanel.setAlignmentX(1);

        JButton maskZoomInButton = new JButton("+");
        JButton maskZoomOutButton = new JButton("-");
        maskZoomInButton.setAlignmentX(1);
        maskZoomOutButton.setAlignmentX(1);

        maskZoomInButton.addActionListener((e) -> {
            maskPanel.zoomIn();
            maskZoomInButton.setSize(maskZoomInButton.getSize().width + 5, maskZoomInButton.getSize().height + 5);
            maskZoomOutButton.setSize(maskZoomOutButton.getSize().width + 5, maskZoomOutButton.getSize().height + 5);
            maskSideBar.revalidate();
        });
        maskSideBar.add(maskZoomInButton);
        maskZoomOutButton.addActionListener((e) -> {
            maskPanel.zoomOut();
            maskZoomInButton.setSize(maskZoomInButton.getSize().width - 5, maskZoomInButton.getSize().height - 5);
            maskZoomOutButton.setSize(maskZoomOutButton.getSize().width - 5, maskZoomOutButton.getSize().height - 5);
            maskSideBar.revalidate();
        });
        maskSideBar.add(maskZoomOutButton);
        this.add(maskSideBar, BorderLayout.EAST);

        maskPanel.addListener(transformedImagePanel);

        setVisible(true);
        pack();
        setSize(1600, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Setup the buttons for the UI
     */
    private void createButtonsForControlPanel(JPanel buttonsPanel) {

        GridBagLayout buttonsPanelLayout = new GridBagLayout();
        buttonsPanel.setLayout(buttonsPanelLayout);
        GridBagConstraints buttonPanelConstraints = new GridBagConstraints();
        buttonPanelConstraints.gridwidth = 1;
        buttonPanelConstraints.gridheight = 1;
        buttonPanelConstraints.weightx = 1;
        buttonPanelConstraints.weighty = 1;

        JButton transformButton = new JButton("transform");
        transformButton.addActionListener((e) -> {
            computeDirectTransform();
            computeReconstruction();
            repaint();
            System.out.println("transform");
        });
        buttonPanelConstraints.gridx = 1;
        buttonPanelConstraints.gridy = 0;
        buttonsPanelLayout.setConstraints(transformButton, buttonPanelConstraints);
        buttonsPanel.add(transformButton);

        JCheckBox centerCheckBox = new JCheckBox("Center", true);
        centerCheckBox.addActionListener((e) -> {
            useCenteredChunks = centerCheckBox.isSelected();
        });
        buttonPanelConstraints.gridx = 2;
        buttonPanelConstraints.gridy = 0;
        buttonsPanelLayout.setConstraints(centerCheckBox, buttonPanelConstraints);
        buttonsPanel.add(centerCheckBox);

        JButton buttonPixelOn = new JButton("ON");
        buttonPixelOn.addActionListener((e) -> {
            setTitle("ON");
            transformedImagePanel.setMode(selectionMode.ON);
            maskPanel.setMode(selectionMode.ON);
        });
        buttonPanelConstraints.gridx = 0;
        buttonPanelConstraints.gridy = 1;
        buttonsPanelLayout.setConstraints(buttonPixelOn, buttonPanelConstraints);
        buttonsPanel.add(buttonPixelOn);

        JButton buttonPixelOff = new JButton("OFF");
        buttonPixelOff.addActionListener((e) -> {
            setTitle("OFF");
            transformedImagePanel.setMode(selectionMode.OFF);
            maskPanel.setMode(selectionMode.OFF);
        });
        buttonPanelConstraints.gridx = 1;
        buttonPanelConstraints.gridy = 1;
        buttonsPanelLayout.setConstraints(buttonPixelOff, buttonPanelConstraints);
        buttonsPanel.add(buttonPixelOff);

        JButton buttonTogglePixel = new JButton("TOGGLE");
        buttonTogglePixel.addActionListener((e) -> {
            setTitle("TOGGLE");
            transformedImagePanel.setMode(selectionMode.TOGGLE);
            maskPanel.setMode(selectionMode.TOGGLE);
        });
        buttonPanelConstraints.gridx = 2;
        buttonPanelConstraints.gridy = 1;
        buttonsPanelLayout.setConstraints(buttonTogglePixel, buttonPanelConstraints);
        buttonsPanel.add(buttonTogglePixel);

        JButton zoomInButton = new JButton("z+");
        zoomInButton.addActionListener((e) -> {
            zoomIn();
            repaint();
        });
        buttonPanelConstraints.gridx = 3;
        buttonPanelConstraints.gridy = 0;
        buttonsPanelLayout.setConstraints(zoomInButton, buttonPanelConstraints);
        buttonsPanel.add(zoomInButton);

        JButton zoomOutButton = new JButton("z-");
        zoomOutButton.addActionListener((e) -> {
            zoomOut();
            repaint();
        });
        buttonPanelConstraints.gridx = 3;
        buttonPanelConstraints.gridy = 1;
        buttonsPanelLayout.setConstraints(zoomOutButton, buttonPanelConstraints);
        buttonsPanel.add(zoomOutButton);
    }

    private void zoomOut() {
        try {
            originalImagePanel.zoomOut();
            transformedImagePanel.zoomOut();
            reconstructedImagePanel.zoomOut();
        } catch (NullPointerException e) {
            System.out.println("Not all panels initiated.");
        }
        imagePanel.revalidate();
        imagePanel.repaint();
        scroll.revalidate();
    }

    private void zoomIn() {
        try {
            originalImagePanel.zoomIn();
            transformedImagePanel.zoomIn();
            reconstructedImagePanel.zoomIn();
        } catch (NullPointerException e) {
            System.out.println("Not all panels initiated.");
        }
        imagePanel.revalidate();
        imagePanel.repaint();
        scroll.revalidate();
    }

    private void computeDirectTransform(MyImage imageSource, MyImage imageDest) {
        System.out.println("Computing transform...");
        double M = imageSource.getHeight();
        double N = imageSource.getWidth();

        for (int v = 0; v < M; v++) {
            int yStart = (v / chunkSize) * chunkSize;
            for (int u = 0; u < N; u++) {
                int xStart = (u / chunkSize) * chunkSize;

                // Compute the value of pixel (u,v) of the tranform
                Complex Fuv = new Complex();

                for (int y = yStart; y < yStart + chunkSize; y++) {
                    for (int x = xStart; x < xStart + chunkSize; x++) {
                        double argument = -2 * PI * ((double) (u * x) / chunkSize + (double) (v * y) / chunkSize);
                        Complex exponentPart = new Complex(argument);
                        Fuv.increment(exponentPart.multiply(imageSource.get(y, x)));
                    }
                }
                Fuv = Fuv.divide(chunkSize * chunkSize);
                if (useCenteredChunks) {
                    imageDest.set(yStart + (v - yStart + chunkSize / 2) % chunkSize, xStart + (u - xStart + chunkSize / 2) % chunkSize, Fuv);
                } else {
                    imageDest.set(v, u, Fuv);
                }

            }
        }
        System.out.println("Computing transform done");
    }

    public void computeDirectTransform() {
        System.out.println("original to transformed");
        computeDirectTransform(originalImagePanel.getImage(), transformedImagePanel.getImage());
    }

    public void computeReconstruction() {
        System.out.println("transformed to reconstructed");
        computeReverseTransform(transformedImagePanel.getImage(), reconstructedImagePanel.getImage());
    }

    void computeReverseTransform(MyImage imageSource, MyImage imageDest) {
        double M = imageSource.getHeight();
        double N = imageSource.getWidth();

        for (int x = 0; x < N; x++) {
            int uStart = (x / chunkSize) * chunkSize;
            for (int y = 0; y < M; y++) {
                int vStart = (y / chunkSize) * chunkSize;

                // Compute the value of pixel (x,y) of the decoded image
                Complex fxy = new Complex();
                for (int u = uStart; u < uStart + chunkSize; u++) {
                    for (int v = vStart; v < vStart + chunkSize; v++) {

                        boolean pixelIsActive;
                        pixelIsActive = transformedImagePanel.isActive(v, u);

                        int centeredU = u;
                        int centeredV = v;

                        if (useCenteredChunks) {
                            centeredU = uStart + (u + chunkSize / 2) % chunkSize;
                            centeredV = vStart + (v + chunkSize / 2) % chunkSize;
                        }

                        if (pixelIsActive) {
                            double argument = 2 * PI * ((double) (centeredU * x) / chunkSize + (double) (centeredV * y) / chunkSize);
                            Complex exponentPart = new Complex(argument);
                            fxy.increment(exponentPart.multiply(imageSource.get(v, u)));
                        }
                    }
                }

                // Set the pixel on the decoded image.
                imageDest.set(y, x, fxy);
            }
        }
        labelTransformed.setText("Transformed Image: " + transformedImagePanel.getPercentageActive());
    }

    public void computeReverseTransform() {
        computeReverseTransform(transformedImagePanel.getImage(), reconstructedImagePanel.getImage());
    }

}
