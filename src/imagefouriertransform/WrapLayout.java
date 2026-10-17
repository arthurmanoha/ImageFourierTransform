package imagefouriertransform;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;

/**
 *
 * @author arthu
 */
public class WrapLayout extends FlowLayout {

    public WrapLayout() {
        super(FlowLayout.LEADING, 8, 8);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        synchronized (target.getTreeLock()) {
            int width = target.getWidth();

            if (width <= 0) {
                width = Integer.MAX_VALUE;
            } else {
                Insets insets = target.getInsets();
                width -= insets.left + insets.right
                        + getHgap() * 2;
            }

            int rowWidth = 0;
            int rowHeight = 0;
            int totalHeight = getVgap();
            int maxWidth = 0;

            for (Component c : target.getComponents()) {
                if (!c.isVisible()) {
                    continue;
                }

                Dimension d = c.getPreferredSize();

                if (rowWidth > 0 && rowWidth + getHgap() + d.width > width) {
                    maxWidth = Math.max(maxWidth, rowWidth);
                    totalHeight += rowHeight + getVgap();
                    rowWidth = 0;
                    rowHeight = 0;
                }

                if (rowWidth > 0) {
                    rowWidth += getHgap();
                }

                rowWidth += d.width;
                rowHeight = Math.max(rowHeight, d.height);
            }

            if (rowWidth > 0) {
                maxWidth = Math.max(maxWidth, rowWidth);
                totalHeight += rowHeight + getVgap();
            }

            Insets insets = target.getInsets();

            return new Dimension(maxWidth + insets.left + insets.right + getHgap() * 2,
                    totalHeight + insets.top + insets.bottom);
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        return preferredLayoutSize(target);
    }
}
