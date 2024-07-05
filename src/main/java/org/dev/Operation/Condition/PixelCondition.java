package org.dev.Operation.Condition;

import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ReadingCondition;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.*;
import java.util.Base64;

@Getter
@Setter
public class PixelCondition extends Condition {

    private transient BufferedImage displayImage;
    private boolean globalSearch;

    public PixelCondition(ReadingCondition chosenReadingCondition, BufferedImage mainImage,
                          Rectangle mainImageBoundingBox, boolean not, boolean required,
                          BufferedImage displayImage, boolean globalSearch) {
        super(chosenReadingCondition, mainImage, mainImageBoundingBox, not, required);
        this.displayImage = displayImage;
        this.globalSearch = globalSearch;
    }

    @Override
    public BufferedImage getMainDisplayImage() { return displayImage; }

    @Override
    public boolean checkCondition() {
        try {
            boolean pass = (globalSearch) ? checkPixelFromCurrentScreen(mainImage)
                    : checkPixelFromBoundingBox(mainImageBoundingBox, mainImage);
            if (not)
                return !pass;
            return pass;
        } catch (Exception e) {
            System.out.println("Fail checking pixel condition");
        }
        return false;
    }

    private boolean checkPixelFromBoundingBox(Rectangle boundingBox, BufferedImage img2) throws AWTException {
        BufferedImage img1 = new Robot().createScreenCapture(boundingBox);
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight())
            return false;
        DataBuffer db1 = img1.getRaster().getDataBuffer();
        DataBuffer db2 = img2.getRaster().getDataBuffer();
        int size1 = db1.getSize();
        int size2 = db2.getSize();
        if (size1 != size2)
            return false;
        for (int i = 0; i < size1; i++)
            if (db1.getElem(i) != db2.getElem(i))
                return false;
        return true;
    }

    private boolean checkPixelFromCurrentScreen(BufferedImage img2) throws AWTException, IOException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage currentScreen = new Robot().createScreenCapture(new Rectangle(0, 0, screenSize.width-1, screenSize.height-1));

        int heightDif = currentScreen.getHeight() - img2.getHeight();
        int widthDif = currentScreen.getWidth() - img2.getWidth();
        for (int y = 0; y < heightDif; y++)
            for (int x = 0; x < widthDif; x++)
                if (isSubImage(currentScreen, img2, x, y)) {
                    System.out.println("Found " + x + " " + y);
                    return true;
                }
        return false;
    }
    private boolean isSubImage(BufferedImage img1, BufferedImage img2, int startX, int startY) {
        int height = img2.getHeight();
        int width = img2.getWidth();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                if (img1.getRGB(startX + x, startY + y) != img2.getRGB(x, y))
                    return false;
                else
                    System.out.println((startX + x) + " " + (startY + y));
        return true;
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        // Serialize mainImage
        if (mainImage != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(mainImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            out.writeObject(Base64.getEncoder().encodeToString(imageBytes));
        } else {
            out.writeObject(null);
        }

        // Serialize displayImage
        if (displayImage != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(displayImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            out.writeObject(Base64.getEncoder().encodeToString(imageBytes));
        } else {
            out.writeObject(null);
        }
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Deserialize mainImage
        String mainImageString = (String) in.readObject();
        if (mainImageString != null) {
            byte[] imageBytes = Base64.getDecoder().decode(mainImageString);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            mainImage = ImageIO.read(bais);
        }

        String displayImageString = (String) in.readObject();
        if (displayImageString != null) {
            byte[] imageBytes = Base64.getDecoder().decode(displayImageString);
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            displayImage = ImageIO.read(bais);
        }
    }
}
