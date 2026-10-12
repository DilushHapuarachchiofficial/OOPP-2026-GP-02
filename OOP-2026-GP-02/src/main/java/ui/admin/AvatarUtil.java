package main.java.ui.admin;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class AvatarUtil {

    /**
     * Renders avatar onto g2 within given (x, y, size, size) box.
     * If imagePath is valid and file exists, renders the cropped circular image with antialiasing.
     * Otherwise, renders the modern default silhouette avatar (blue circular background with white/soft-blue silhouette).
     */
    public static void renderAvatar(Graphics2D g2, String imagePath, int x, int y, int size) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        BufferedImage img = loadAvatarImage(imagePath);
        if (img != null) {
            Shape oldClip = g2.getClip();
            Ellipse2D.Float circle = new Ellipse2D.Float(x, y, size, size);
            g2.clip(circle);

            // Scale to fill circle preserving aspect ratio (center-cropped)
            int imgW = img.getWidth();
            int imgH = img.getHeight();
            double scale = Math.max((double) size / imgW, (double) size / imgH);
            int drawW = (int) Math.round(imgW * scale);
            int drawH = (int) Math.round(imgH * scale);
            int drawX = x + (size - drawW) / 2;
            int drawY = y + (size - drawH) / 2;

            g2.drawImage(img, drawX, drawY, drawW, drawH, null);
            g2.setClip(oldClip);

            // Subtle border around circular photo
            g2.setColor(new Color(220, 230, 242));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(x, y, size - 1, size - 1);
        } else {
            // Render default modern avatar (matching screenshot 1)
            // Vibrant modern blue background
            g2.setColor(new Color(66, 133, 244));
            g2.fillOval(x, y, size, size);

            // Soft white/light-blue silhouette
            g2.setColor(new Color(245, 249, 255));

            // Head circle
            int headR = (int) Math.round(size * 0.38);
            int headX = x + (size - headR) / 2;
            int headY = y + (int) Math.round(size * 0.18);
            g2.fillOval(headX, headY, headR, headR);

            // Body shoulders (clipped inside avatar circle)
            Shape oldClip = g2.getClip();
            Ellipse2D.Float circle = new Ellipse2D.Float(x, y, size, size);
            g2.clip(circle);

            int bodyW = (int) Math.round(size * 0.74);
            int bodyH = (int) Math.round(size * 0.56);
            int bodyX = x + (size - bodyW) / 2;
            int bodyY = y + (int) Math.round(size * 0.58);
            g2.fillArc(bodyX, bodyY, bodyW, bodyH, 0, 180);

            g2.setClip(oldClip);
        }
    }

    private static BufferedImage loadAvatarImage(String imagePath) {
        if (imagePath == null) return null;
        String trimmed = imagePath.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("default_avatar.png")) {
            return null;
        }

        try {
            // Check direct file path
            File file = new File(trimmed);
            if (file.exists() && file.isFile()) {
                return ImageIO.read(file);
            }

            // Check inside project resources
            File resFile = new File("src/main/resources/images/profiles/" + trimmed);
            if (resFile.exists() && resFile.isFile()) {
                return ImageIO.read(resFile);
            }
        } catch (Exception e) {
            // Ignore error and fall back to default avatar
        }
        return null;
    }
}
