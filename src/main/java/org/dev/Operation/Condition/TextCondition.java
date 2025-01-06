package org.dev.Operation.Condition;

import lombok.Getter;
import lombok.Setter;
import net.sourceforge.tess4j.TesseractException;
import org.dev.AppScene;
import org.dev.Enum.LogLevel;
import org.dev.Enum.ReadingCondition;
import org.dev.Operation.ImageSerialization;
import org.dev.Menu.ConditionTextMenuController;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Set;

@Getter @Setter
public class TextCondition extends Condition {

    private Set<String> savedText;
    private double currentTextScale;
    private transient final String className = this.getClass().getSimpleName();

    @Serial
    private static final long serialVersionUID = 1L;

    public TextCondition(ReadingCondition chosenReadingCondition, BufferedImage mainImage, Rectangle mainImageBoundingBox,
                         boolean not, boolean required, BufferedImage displayImage, double textScale, Set<String> texts) {
        super(chosenReadingCondition, mainImage, mainImageBoundingBox, not, required);
        this.displayImage = displayImage;
        currentTextScale = textScale;
        savedText = texts;
    }

    @Override
    public String getExpectedResult() { return savedText.toString(); }

    @Override
    public String getActualResult() { return readResult; }

    @Override
    public ImageCheckResult checkCondition() {
        try {
            ImageCheckResult checkResult = readTextFromCurrentScreen(mainImageBoundingBox, getCurrentTextScale());
            readResult = checkResult.getReadResult();
            if (not)
                checkResult.setPass(!checkCondition().isPass());
            return checkResult;
        } catch (Exception e) {
            AppScene.addLog(LogLevel.ERROR, className, "Fail - checking text condition");
            return null;
        }
    }

    private ImageCheckResult readTextFromCurrentScreen(Rectangle boundingBox, double scale) throws AWTException, TesseractException {
        BufferedImage seenImage = ConditionTextMenuController.captureCurrentScreen(boundingBox);
        if (scale != 1.00)
            seenImage = new ConditionTextMenuController().getScaledImage(seenImage, scale);
        String readText = ConditionTextMenuController.readTextFromImage(seenImage).replace("\n", "");
        BufferedImage seenImageWithEdges = createImageWithEdges(seenImage, getImageWithEdges(boundingBox, mainImage));
        return new ImageCheckResult(readText, seenImageWithEdges, savedText.contains(readResult));
    }

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        ImageSerialization.serializeBufferedImageWriteObject(out, mainImage);
        ImageSerialization.serializeBufferedImageWriteObject(out, displayImage);
        AppScene.addLog(LogLevel.TRACE, className, "Serialized main image and display image");
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String mainImageString = (String) in.readObject();
        mainImage = ImageSerialization.deserializeBufferedImageReadObject(in, mainImageString, true);
        String displayImageString = (String) in.readObject();
        displayImage = ImageSerialization.deserializeBufferedImageReadObject(in, displayImageString, true);
        AppScene.addLog(LogLevel.TRACE, className, "Deserialized main image and display image");
    }
}
