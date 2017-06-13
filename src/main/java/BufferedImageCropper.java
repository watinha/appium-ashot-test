import java.awt.image.BufferedImage;
import java.util.Set;

import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.cropper.ImageCropper;

public class BufferedImageCropper extends ImageCropper {

    private static final long serialVersionUID = 1L;

    @Override
    protected Screenshot cropScreenshot(BufferedImage image, Set<Coords> coords) {
        Coords cropArea = Coords.unity(coords);
        int left = cropArea.x,
            top = cropArea.y,
            height = (new Double(cropArea.getHeight())).intValue(),
            width = (new Double(cropArea.getWidth())).intValue();
        if (top < 0) {
            height = height + top;
            top = 0;
        }
        if (left < 0) {
            width = width + left;
            left = 0;
        }
        if (top >= image.getHeight())
            top = image.getHeight() - 2;
        if (left >= image.getWidth())
            left = image.getWidth() - 2;
        if (top + height >= image.getHeight())
            height = image.getHeight() - top - 1;
        if (left + width >= image.getWidth())
            width = image.getWidth() - left - 1;
        System.out.println("Image: " + left + " " + top + " " + width + " " + height);
        return new Screenshot(image.getSubimage(
                left, top, width, height));
    }
}
