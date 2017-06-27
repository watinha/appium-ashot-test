import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.CoordsProvider;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;
import ru.yandex.qatools.ashot.shooting.ScalingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.ViewportPastingDecorator;

public class Main {
    public static void main (String[] args) throws Exception {
        DesiredCapabilities capabilities = new DesiredCapabilities();

        capabilities.setCapability("platformName", "Android");
        capabilities.setCapability("deviceName", "Android Emulator");
        capabilities.setCapability("browserName", "Chrome");

        //capabilities.setCapability("platformName", "iOS");
        //capabilities.setCapability("deviceName", "iPhone SE");
        //capabilities.setCapability("browserName", "Safari");
        //capabilities.setCapability("platformVersion", "10.3");
        //capabilities.setCapability("automationName", "XCUITest");

        WebDriver driver = new RemoteWebDriver(new URL("http://192.168.92.1:4723/wd/hub/"),
                                            capabilities);

        Main m = new Main(driver);
        //m.getScreenshot("http://esporte.uol.com.br/tenis", "1-uol");
        //m.getScreenshot("http://globoesporte.com/tenis", "2-globo");
        //m.getScreenshot("http://amazon.com", "3-amazon");
        //m.getScreenshot("http://google.com", "4-google");
        //m.getScreenshot("http://ebay.com", "5-ebay");
        //m.getScreenshot("http://twitter.com", "6-twitter");
        m.getScreenshot("http://facebook.com", "facebook");
        m.getSegments("http://facebook.com");
        driver.quit();
    }

    private WebDriver driver;
    public WebDriver getDriver(){ return this.driver; }
    public void setDriver(WebDriver driver){ this.driver = driver; }
    public Main (WebDriver driver) { this.setDriver(driver); }

    public int[] orderBySize (WebDriver driver, int size) {
        int sizes[] = new int[size],
            index[] = new int[size],
            smallest;
        WebElement target;
        System.out.println("ordering");
        for (int i = 0; i < size; i++) {
            target = (WebElement) ((JavascriptExecutor) driver).executeScript(
                    "return window.elements[" + i + "];");
            sizes[i] = target.getSize().getHeight() *
                       target.getSize().getWidth();

        }
        for (int i = 0; i < size; i++) {
            smallest = -1;
            for (int j = (size - 1); j >= 0; j--) {
                if ((smallest == -1 || smallest > sizes[j]) && sizes[j] != -1) {
                    smallest = sizes[j];
                    index[i] = j;
                }
            }
            sizes[index[i]] = -1;
            System.out.print(index[i] + " ");
        }
        System.out.println("done.ordering");
        return index;
    }

    public void getSegments (String url) throws IOException {
        AShot ashot = new AShot().imageCropper(new BufferedImageCropper(3));
        ShootingStrategy strategy = new ScalingViewportPastingShootingStrategy().withScrollTimeout(100);
        CoordsProvider provider = new WebDriverCoordsProvider();
        WebElement target;
        Screenshot screenshot;
        int size,
            orderedIndex[];
        driver.get(url);
        size = Integer.parseInt(((JavascriptExecutor) driver).executeScript(
            "window.elements = document.querySelectorAll('*'); return window.elements.length;").toString());
        orderedIndex = this.orderBySize(driver, size);

        for (int i = 0; i < size; i++) {
            target = (WebElement) ((JavascriptExecutor) driver).executeScript(
                    "return window.elements[" + orderedIndex[i] + "];");
            System.out.println(i + " " + target.getTagName());
            if (provider.ofElement(driver, target).isEmpty() || target.getTagName().equalsIgnoreCase("script") ||
                                                                target.getTagName().equalsIgnoreCase("style")) {
                System.out.println("Empty...");
            } else {
                screenshot = ashot.coordsProvider(provider)
                                  .shootingStrategy(strategy)
                                  .takeScreenshot(driver, target);
                ImageIO.write(screenshot.getImage(), "PNG", new File("data/" + i + ".png"));
            }
            ((JavascriptExecutor) driver).executeScript(
                    "window.elements[" + orderedIndex[i] + "].style.opacity = 0;");
        }
    }

    public void getScreenshot(String url, String name) throws IOException {
        ShootingStrategy strategy = new ScalingViewportPastingShootingStrategy().withScrollTimeout(100);
        File screenshot = new File("data/" + name + "-ashot.png");
        driver.get(url);
        Screenshot ashot_screenshot = new AShot()
            .coordsProvider(new WebDriverCoordsProvider())
            .shootingStrategy(strategy)
            .takeScreenshot(driver);
        BufferedImage ashot_image = ashot_screenshot.getImage();
        ImageIO.write(ashot_image, "PNG", screenshot);

        File screenshot2 = new File("data/" + name + "-standard.png");
        File temp = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(temp, screenshot2);
        temp.delete();
    }
}
