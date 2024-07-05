package org.dev.Operation.Condition;
import lombok.Getter;
import lombok.Setter;
import org.dev.Enum.ReadingCondition;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

@Getter
@Setter
public abstract class Condition implements Serializable {
    protected ReadingCondition chosenReadingCondition;
    protected transient BufferedImage mainImage;
    protected Rectangle mainImageBoundingBox;
    protected boolean not;
    protected boolean required;

    public Condition(ReadingCondition chosenReadingCondition, BufferedImage mainImage, Rectangle mainImageBoundingBox,
                     boolean not, boolean required) {
        this.chosenReadingCondition = chosenReadingCondition;
        this.mainImage = mainImage;
        this.mainImageBoundingBox = mainImageBoundingBox;
        this.not = not;
        this.required = required;
    }
    public BufferedImage getMainDisplayImage() {
        return mainImage;
    }
    public abstract boolean checkCondition();
}
