package Authentication;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class RoundedButton extends JButton {
    private int arcWidth;
    private int arcHeight;

    public RoundedButton(String text) {
        super(text);
        this.arcWidth = 30;
        this.arcHeight = 30;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
    }

    public RoundedButton(String text, int arcWidth, int arcHeight) {
        super(text);
        this.arcWidth = arcWidth;
        this.arcHeight = arcHeight;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        if (getModel().isPressed()) {
            g2.setColor(new Color(90, 170, 50)); // Darker green when pressed
        } else if (getModel().isRollover()) {
            g2.setColor(new Color(140, 220, 100)); // Lighter green on hover
        } else {
            g2.setColor(getBackground());
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);

        // Draw border
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);

        // Draw text
        super.paintComponent(g);
    }
}
