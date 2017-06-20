import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.SimpleShootingStrategy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static ru.yandex.qatools.ashot.util.InnerScript.*;

public class ScalingViewportPastingShootingStrategy implements ShootingStrategy {

    private static final long serialVersionUID = 1L;
    protected int scrollTimeout = 0;
    private Coords shootingArea;
	private ShootingStrategy strategy = new SimpleShootingStrategy();

    public ScalingViewportPastingShootingStrategy withScrollTimeout(int scrollTimeout) {
        this.scrollTimeout = scrollTimeout;
        return this;
    }

    @Override
    public BufferedImage getScreenshot(WebDriver wd) {
        return getScreenshot(wd, null);
    }

    @Override
    public BufferedImage getScreenshot(WebDriver wd, Set<Coords> coordsSet) {
        JavascriptExecutor js = (JavascriptExecutor) wd;
        int pageHeight = getFullHeight(wd);
        int pageWidth = getFullWidth(wd);
        int viewportHeight = getWindowHeight(wd);
        float dpi = -1;
        shootingArea = getShootingCoords(coordsSet, pageWidth, pageHeight, viewportHeight);

        BufferedImage finalImage = new BufferedImage(pageWidth, shootingArea.height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = finalImage.createGraphics();

        int scrollTimes = (int) Math.ceil(shootingArea.getHeight() / viewportHeight);
        for (int n = 0; n < scrollTimes; n++) {
            scrollVertically(js, shootingArea.y + viewportHeight * n);
            waitForScrolling();
            BufferedImage part = getShootingStrategy().getScreenshot(wd);

            if (dpi == -1) {
                dpi = part.getWidth() / pageWidth;
                finalImage = new BufferedImage(Math.round(pageWidth * dpi), Math.round(shootingArea.height * dpi), BufferedImage.TYPE_3BYTE_BGR);
                graphics = finalImage.createGraphics();
            }

            if (dpi != -1)
                graphics.drawImage(part, 0, Math.round((getCurrentScrollY(js) - shootingArea.y) * dpi), null);
            else
                graphics.drawImage(part, 0, getCurrentScrollY(js) - shootingArea.y, null);
        }

        graphics.dispose();
        return finalImage;
    }

    @Override
    public Set<Coords> prepareCoords(Set<Coords> coordsSet) {
        return coordsSet;
        //return shootingArea == null ? coordsSet : shiftCoords(coordsSet, shootingArea);
    }

    public int getFullHeight(WebDriver driver) {
        return ((Number) execute(PAGE_HEIGHT_JS, driver)).intValue();
    }

    public int getFullWidth(WebDriver driver) {
        return ((Number) execute(VIEWPORT_WIDTH_JS, driver)).intValue();
    }

    public int getWindowHeight(WebDriver driver) {
        return ((Number) execute(VIEWPORT_HEIGHT_JS, driver)).intValue();
    }

    protected int getCurrentScrollY(JavascriptExecutor js) {
        return ((Number) js.executeScript("var scrY = window.scrollY;"
        		+ "if(scrY){return scrY;} else {return 0;}")).intValue();
    }

    protected void scrollVertically(JavascriptExecutor js, int scrollY) {
        js.executeScript("scrollTo(0, arguments[0]); return [];", scrollY);
    }

    private Coords getShootingCoords(Set<Coords> coords, int pageWidth, int pageHeight, int viewPortHeight) {
        if (true || coords == null || coords.isEmpty()) {
            return new Coords(0, 0, pageWidth, pageHeight);
        }
        return extendShootingArea(Coords.unity(coords), viewPortHeight, pageHeight);
    }

    private Set<Coords> shiftCoords(Set<Coords> coordsSet, Coords shootingArea) {
        Set<Coords> shiftedCoords = new HashSet<>();
        if (coordsSet != null) {
            for (Coords coords : coordsSet) {
                coords.y -= shootingArea.y;
                shiftedCoords.add(coords);
            }
        }
        return shiftedCoords;
    }

    private Coords extendShootingArea(Coords shootingCoords, int viewportHeight, int pageHeight) {
        int halfViewport = viewportHeight / 2;
        shootingCoords.y = Math.max(shootingCoords.y - halfViewport / 2, 0);
        shootingCoords.height = Math.min(shootingCoords.height + halfViewport, pageHeight);
        return shootingCoords;
    }

    private void waitForScrolling() {
        try {
            Thread.sleep(scrollTimeout);
        } catch (InterruptedException e) {
            throw new IllegalStateException("Exception while waiting for scrolling", e);
        }
    }

	private ShootingStrategy getShootingStrategy () {
		return this.strategy;
	}
}
