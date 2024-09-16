package imagefouriertransform;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import static java.lang.Math.PI;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author arthu
 */
public class ImageWindow extends JFrame {

    JPanel mainPanel;
    JPanel buttonsPanel;
    ImagePanel imagePanelLeft;
    ImagePanel imagePanelMiddle;
    ImagePanel imagePanelRight;
    ImagePanel transformedPanelLeft;
    ImagePanel transformedPanelMiddle;
    ImagePanel transformedPanelRight;
    JLabel labelFirst;
    JLabel labelMiddle;
    JLabel labelSecond;

    int width = 1900, height = 1050;
    int availableWidth, availableHeight; // for each image

    double percentage; // [0; 100]

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

        buttonsPanel = new JPanel();

        this.setSize(new Dimension(10, 10));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        labelFirst = new JLabel("First Image");
        prepareComponent(labelFirst, 0, 0, layout);
        mainPanel.add(labelFirst);

        labelMiddle = new JLabel("Middle Image");
        prepareComponent(labelMiddle, 1, 0, layout);
        mainPanel.add(labelMiddle, constraints);

        labelSecond = new JLabel("Second Image");
        prepareComponent(labelSecond, 2, 0, layout);
        mainPanel.add(labelSecond);

        Dimension myPrefSize = new Dimension(availableWidth, 200);

        // First image
        imagePanelLeft = new ImagePanel(imgA);
        imagePanelLeft.setPreferredSize(myPrefSize);
        prepareComponent(imagePanelLeft, 0, 1, layout);
        mainPanel.add(imagePanelLeft);

        // First Fourier transform
        transformedPanelLeft = new ImagePanel(imgA.createEmptyClone(), true);
        transformedPanelLeft.setPreferredSize(myPrefSize);
        prepareComponent(transformedPanelLeft, 0, 2, layout);
        mainPanel.add(transformedPanelLeft);

        // Second image
        imagePanelMiddle = new ImagePanel(imgA.createEmptyClone());
        imagePanelMiddle.setPreferredSize(myPrefSize);
        prepareComponent(imagePanelMiddle, 1, 1, layout);
        mainPanel.add(imagePanelMiddle);

        // Second Fourier transform
        transformedPanelMiddle = new ImagePanel(imgA.createEmptyClone(), true);
        transformedPanelMiddle.setPreferredSize(myPrefSize);
        prepareComponent(transformedPanelMiddle, 1, 2, layout);
        mainPanel.add(transformedPanelMiddle);

        // Third image
        imagePanelRight = new ImagePanel(imgB);
        imagePanelRight.setPreferredSize(myPrefSize);
        prepareComponent(imagePanelRight, 2, 1, layout);
        mainPanel.add(imagePanelRight);

        // Third Fourier transform
        transformedPanelRight = new ImagePanel(imgB.createEmptyClone(), true);
        transformedPanelRight.setPreferredSize(myPrefSize);
        prepareComponent(transformedPanelRight, 2, 2, layout);
        mainPanel.add(transformedPanelRight);

        /////////////////////////////////////////
        // Text field and slider for mix value //
        ////////////////////////////////////////
        JTextField mixValueField = new JTextField(percentage + "");
        JSlider slider = new JSlider(0, 100, 0);

        mixValueField.setPreferredSize(new Dimension(100, 30));
        mixValueField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                percentage = Double.valueOf(e.getActionCommand());
                slider.setValue((int) percentage);
                computeMixImage(percentage);
                computeReverseTransform();
                repaint();
            }

        });
        GridBagConstraints textFieldConstraints = new GridBagConstraints();
        textFieldConstraints.gridx = 1;
        textFieldConstraints.gridy = 3;
        layout.setConstraints(mixValueField, textFieldConstraints);
        mainPanel.add(mixValueField);

        // Slider for mix value
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int sliderValue = slider.getValue();
                percentage = (double) sliderValue;
                mixValueField.setText("" + percentage);
                computeMixImage(percentage);
                computeReverseTransform();
                repaint();
            }
        });
        GridBagConstraints sliderConstraints = new GridBagConstraints();
        sliderConstraints.gridx = 2;
        sliderConstraints.gridy = 3;
        layout.setConstraints(slider, sliderConstraints);
        mainPanel.add(slider);

        ////////////////////////////
        JButton zoomPlusButton = new JButton("Z+");
        zoomPlusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomIn();
            }
        });
        buttonsPanel.add(zoomPlusButton);
        JButton zoomMinusButton = new JButton("Z-");
        zoomMinusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomOut();
            }
        });
        buttonsPanel.add(zoomMinusButton);

        JButton transformButton = new JButton("transform");
        transformButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                computeDirectTransform();
                repaint();
            }
        });
        buttonsPanel.add(transformButton);

        JButton transformBackButton = new JButton("transform back");
        transformBackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                computeReverseTransform();
                repaint();
            }
        });
        buttonsPanel.add(transformBackButton);

        GridBagConstraints buttonPanelConstraints = new GridBagConstraints();
        buttonPanelConstraints.gridx = 0;
        buttonPanelConstraints.gridy = 4;
        buttonPanelConstraints.gridwidth = 3;
        layout.setConstraints(buttonsPanel, buttonPanelConstraints);
        mainPanel.add(buttonsPanel);

        availableWidth = this.getSize().width;
        availableHeight = this.getSize().height;
        imagePanelLeft.setPreferredSize(new Dimension(availableWidth, availableHeight));
        imagePanelMiddle.setPreferredSize(new Dimension(availableWidth, availableHeight));
        imagePanelRight.setPreferredSize(new Dimension(availableWidth, availableHeight));
        transformedPanelLeft.setPreferredSize(new Dimension(availableWidth, availableHeight));
        transformedPanelMiddle.setPreferredSize(new Dimension(availableWidth, availableHeight));
        transformedPanelRight.setPreferredSize(new Dimension(availableWidth, availableHeight));
        setPreferredSize(new Dimension(width, height));
        setVisible(true);
        repaint();
        pack();
        zoomIn();
        zoomOut();
        revalidate();
    }

    private void zoomOut() {
        availableWidth = getSize().width / 3;
        imagePanelLeft.zoomOut();
        imagePanelMiddle.zoomOut();
        imagePanelRight.zoomOut();
        transformedPanelLeft.zoomOut();
        transformedPanelMiddle.zoomOut();
        transformedPanelRight.zoomOut();
        repaint();
    }

    private void zoomIn() {
        availableWidth = getSize().width / 3;
        imagePanelLeft.zoomIn();
        imagePanelMiddle.zoomIn();
        imagePanelRight.zoomIn();
        transformedPanelLeft.zoomIn();
        transformedPanelMiddle.zoomIn();
        transformedPanelRight.zoomIn();
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
        computeDirectTransform(imagePanelLeft.getImage(), transformedPanelLeft.getImage());
        computeDirectTransform(imagePanelRight.getImage(), transformedPanelRight.getImage());
        computeMixImage(percentage);
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
        computeReverseTransform(transformedPanelMiddle.getImage(), imagePanelMiddle.getImage());
    }

    /**
     * Compute a weighted sum of two images.
     *
     * @param percentage [0, 100]; 0: only image A, 100: only image B.
     */
    private void computeMixImage(double percentage) {
        System.out.println("mix " + percentage);
        MyImage imgA = transformedPanelLeft.getImage().multiply(1 - percentage / 100);
        MyImage imgB = transformedPanelRight.getImage().multiply(percentage / 100);
        MyImage imgMiddle = new MyImage(imgA, imgB);

        transformedPanelMiddle.setImage(imgMiddle);
        labelFirst.setText("First Image");
        labelMiddle.setText("Middle Image");
        labelSecond.setText("Second Image");
    }
}
