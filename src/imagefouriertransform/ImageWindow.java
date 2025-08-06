package imagefouriertransform;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import static java.lang.Math.PI;
import javax.swing.JButton;
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
    JLabel labelOriginal;
    JLabel labelTransformed;
    JLabel labelReconstructed;

    int width = 1900, height = 600;
    int availableWidth, availableHeight; // for each image

    double percentage; // [0; 100]

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

    public ImageWindow(MyImage imgA, MyImage imgB) {
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
        prepareComponent(labelReconstructed, 2, 0, layout);
        mainPanel.add(labelReconstructed);

        Dimension myPrefSize = new Dimension(availableWidth, 200);

        // First image
        originalImagePanel = new ImagePanel(imgA);
        originalImagePanel.setPreferredSize(myPrefSize);
        prepareComponent(originalImagePanel, 0, 1, layout);
        mainPanel.add(originalImagePanel);

        // Transformed image
        transformedImagePanel = new RestrictedImagePanel(imgA.createEmptyClone());
        transformedImagePanel.setPreferredSize(myPrefSize);
        prepareComponent(transformedImagePanel, 1, 1, layout);
        mainPanel.add(transformedImagePanel);

        // Reconstructed image
        reconstructedImagePanel = new ImagePanel(imgA.createEmptyClone());
        reconstructedImagePanel.setPreferredSize(myPrefSize);
        prepareComponent(reconstructedImagePanel, 2, 1, layout);
        mainPanel.add(reconstructedImagePanel);

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

        JButton buttonPixelOn = new JButton("ON");
        buttonPixelOn.addActionListener((e) -> {
            setTitle("ON");
            transformedImagePanel.setMode(selectionMode.ON);
        });
        buttonPanelConstraints.gridx = 0;
        buttonPanelConstraints.gridy = 1;
        buttonsPanelLayout.setConstraints(buttonPixelOn, buttonPanelConstraints);
        buttonsPanel.add(buttonPixelOn);

        JButton buttonPixelOff = new JButton("OFF");
        buttonPixelOff.addActionListener((e) -> {
            setTitle("OFF");
            transformedImagePanel.setMode(selectionMode.OFF);
        });
        buttonPanelConstraints.gridx = 1;
        buttonPanelConstraints.gridy = 1;
        buttonsPanelLayout.setConstraints(buttonPixelOff, buttonPanelConstraints);
        buttonsPanel.add(buttonPixelOff);

        JButton buttonTogglePixel = new JButton("TOGGLE");
        buttonTogglePixel.addActionListener((e) -> {
            setTitle("TOGGLE");
            transformedImagePanel.setMode(selectionMode.TOGGLE);
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
        repaint();
    }

    private void zoomIn() {
        availableWidth = getSize().width / 3;
        originalImagePanel.zoomIn();
        transformedImagePanel.zoomIn();
        reconstructedImagePanel.zoomIn();
        repaint();
    }

    private void computeDirectTransform(MyImage imageSource, MyImage imageDest) {
        System.out.println("Computing transform...");
        double M = imageSource.getHeight();
        double N = imageSource.getWidth();

        for (int v = 0; v < M; v++) {
            for (int u = 0; u < N; u++) {

                // Compute the value of pixel (u,v) of the tranform
                Complex Fuv = new Complex();
                for (int y = 0; y < M; y++) {
                    for (int x = 0; x < N; x++) {
                        double argument = -2 * PI * ((double) (u * x) / N + (double) (v * y) / M);
                        Complex exponentPart = new Complex(argument);
                        Fuv.increment(exponentPart.multiply(imageSource.get(y, x)));
                    }
                }
                Fuv = Fuv.divide(M * N);
                imageDest.set(v, u, Fuv);
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
            for (int y = 0; y < M; y++) {

                // Compute the value of pixel (x,y) of the decoded image
                Complex fxy = new Complex();
                for (int u = 0; u < N; u++) {
                    for (int v = 0; v < M; v++) {
                        double argument = 2 * PI * ((double) (u * x) / N + (double) (v * y) / M);
                        Complex exponentPart = new Complex(argument);
                        fxy.increment(exponentPart.multiply(imageSource.get(v, u)));
                    }
                }

                // Set the pixel on the decoded image.
                imageDest.set(y, x, fxy);
            }
        }
    }

    public void computeReverseTransform() {
        computeReverseTransform(transformedImagePanel.getImage(), reconstructedImagePanel.getImage());
    }

}
