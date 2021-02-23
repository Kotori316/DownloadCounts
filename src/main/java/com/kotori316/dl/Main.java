package com.kotori316.dl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class Main {
    public static final String BASE_URI = "https://www.curseforge.com/minecraft/mc-mods/";

    public static void main(String[] args) {
        var time = ZonedDateTime.now();
        if (System.getenv("OS") != null && System.getenv("OS").contains("Win") && System.getProperty("webdriver.chrome.driver") == null) {
            System.setProperty("webdriver.chrome.driver", "C:\\ProgramWorks\\utils\\chromedriver.exe");
        }
        var projects = List.of("additional-enchanted-miner", "largefluidtank", "infchest", "largefluidtank-fabric", 
                               "scalable-cats-force", "planting-dirt-for-saplings", "limit-mob-spawn");
        var counts = getDownloadCounts(projects);
        System.out.println(counts);
        Output.appendAll(time, counts);
    }

    static Map<String, Integer> getDownloadCounts(List<String> projects) {
        var map = new HashMap<String, Integer>();
        var options = new ChromeOptions();
        options.addArguments("--no-sandbox", "--window-size=1920,1080", "--disable-gpu", "--disable-dev-shm-usage");
        var driver = new ChromeDriver(options);

        for (String project : projects) {
            var uri = BASE_URI + project;
            driver.get(uri);
            System.out.printf("Accessed to %s\n", uri);
            try {
                Thread.sleep(3000L);
                var screenshot = driver.getScreenshotAs(OutputType.FILE).toPath();
                Files.copy(screenshot, Path.of(project + "-screen.png"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            var downloadElement = driver.findElement(By.xpath("/html/body/div[1]/main/div[1]/div[2]/section/aside/div/div/div[1]/div[2]/div[4]/span[2]"));
            var countText = downloadElement.getText();
            map.put(project, Integer.parseInt(countText.replace(",", "")));
        }
        driver.quit();
        return map;
    }
}
