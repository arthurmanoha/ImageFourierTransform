package imagefouriertransform;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import static java.lang.Math.PI;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author arthu
 */
public class ImageWindow extends JFrame {

    JPanel mainPanel;
    JPanel buttonsPanel;
    ImagePanel originalImagePanel;
    RestrictedImagePanel transformedImagePanel;
    ImagePanel reconstructedImagePanel;
    RestrictedImagePanel maskPanel;
    JLabel labelOriginal;
    JLabel labelTransformed;
    JLabel labelReconstructed;
    JLabel labelTransformed2;

    int width = 1220, height = 1050;
    int availableWidth, availableHeight; // for each image

    int chunkSize = 8;
    boolean useCenteredChunks = true;
    double percentage; // [0; 100]

//    boolean centerTransform = false;
    // The different ways we can interact with the pixels of the transformed image
    enum selectionMode {
        ON, OFF, TOGGLE
    }

    private void prepareComponent(JComponent comp, int col, int row, GridBagLayout layout) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = col;
        constraints.gridy = row;
        layout.setConstraints(comp, constraints);
        availableWidth = getSize().width / 3;
        percentage = 0;
    }

    public ImageWindow(MyImage imgA) {
        super();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                availableWidth = getSize().width / 3;
                repaint();
            }
        });

        mainPanel = new JPanel();

        this.setContentPane(mainPanel);
        setVisible(true);

        GridBagLayout layout = new GridBagLayout();
        mainPanel.setLayout(layout);
        GridBagConstraints constraints = new GridBagConstraints();

        this.setSize(new Dimension(10, 10));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        labelOriginal = new JLabel("Original Image");
        prepareComponent(labelOriginal, 0, 0, layout);
        mainPanel.add(labelOriginal);

        labelTransformed = new JLabel("Transformed Image");
        prepareComponent(labelTransformed, 1, 0, layout);
        mainPanel.add(labelTransformed, constraints);

        labelReconstructed = new JLabel("Reconstructed Image");
        prepareComponent(labelReconstructed, 0, 2, layout);
        mainPanel.add(labelReconstructed);

        labelTransformed2 = new JLabel("Mask");
        prepareComponent(labelTransformed2, 1, 2, layout);
        mainPanel.add(labelTransformed2);

        Dimension myPrefSize = new Dimension(availableWidth, 200);

        // First image
        originalImagePanel = new ImagePanel(imgA);
        originalImagePanel.setPreferredSize(myPrefSize);
        prepareComponent(originalImagePanel, 0, 1, layout);
        mainPanel.add(originalImagePanel);

        // Transformed image
        transformedImagePanel = new RestrictedImagePanel(imgA.createEmptyClone(), chunkSize);
        transformedImagePanel.setPreferredSize(myPrefSize);
        prepareComponent(transformedImagePanel, 1, 1, layout);
        mainPanel.add(transformedImagePanel);

        // Reconstructed image
        reconstructedImagePanel = new ImagePanel(imgA.createEmptyClone());
        reconstructedImagePanel.setPreferredSize(myPrefSize);
        prepareComponent(reconstructedImagePanel, 0, 3, layout);
        mainPanel.add(reconstructedImagePanel);

        // Reconstructed image
        maskPanel = new RestrictedImagePanel(chunkSize);
        maskPanel.setPreferredSize(myPrefSize);
        prepareComponent(maskPanel, 1, 3, layout);
        maskPanel.zoom *= 4;
        maskPanel.toggleDisplayRows();
        mainPanel.add(maskPanel);

        maskPanel.addListener(transformedImagePanel);

        createButtonsPanel();

        GridBagConstraints mainPanelConstraints = new GridBagConstraints();
        mainPanelConstraints.gridx = 0;
        mainPanelConstraints.gridy = 4;
        mainPanelConstraints.gridwidth = 3;
        layout.setConstraints(buttonsPanel, mainPanelConstraints);
        mainPanel.add(buttonsPanel);

        availableWidth = this.getSize().width;
        availableHeight = this.getSize().height;
        originalImagePanel.setPreferredSize(new Dimension(availableWidth, availableHeight));
        transformedImagePanel.setPreferredSize(new Dimension(availableWidth, availableHeight));
        reconstructedImagePanel.setPreferredSize(new Dimension(availableWidth, availableHeight));
        maskPanel.setPreferredSize(new Dimension(availableWidth, availableHeight));
        setPreferredSize(new Dimension(width, height));
        setVisible(true);
        repaint();
        pack();
        zoomIn();
        zoomOut();
        revalidate();
    }

    /**
     * Setup the buttons for the UI
     */
    private void createButtonsPanel() {

        buttonsPanel = new JPanel();
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
    }

    private void zoomOut() {
        availableWidth = getSize().width / 3;
        originalImagePanel.zoomOut();
        transformedImagePanel.zoomOut();
        reconstructedImagePanel.zoomOut();
        maskPanel.zoomOut();
        repaint();
    }

    private void zoomIn() {
        availableWidth = getSize().width / 3;
        originalImagePanel.zoomIn();
        transformedImagePanel.zoomIn();
        reconstructedImagePanel.zoomIn();
        maskPanel.zoomIn();
        repaint();
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
