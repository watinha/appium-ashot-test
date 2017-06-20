import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.SimpleShootingStrategy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class ScrollStrategy implements ShootingStrategy {

    private static final long serialVersionUID = 1L;

    @Override
    public BufferedImage getScreenshot(WebDriver driver) {
        return this.getScreenshot(driver, null);
    }

    @Override
    public BufferedImage getScreenshot(WebDriver driver, Set<Coords> cords) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        this.scrollVertically(js, 0);
        this.waitForScrolling();
        return null;
    }

    private void scrollVertically(JavascriptExecutor js, int scrollY) {
        js.executeScript("scrollTo(0, arguments[0]); return [];", scrollY);
    }

    @Override
    public Set<Coords> prepareCoords(Set<Coords> coordsSet) {
        return coordsSet;
    }

    private void waitForScrolling() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Exception while waiting for scrolling", e);
        }
    }

}
