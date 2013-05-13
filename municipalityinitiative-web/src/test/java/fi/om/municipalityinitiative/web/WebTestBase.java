package fi.om.municipalityinitiative.web;

import com.google.common.collect.Lists;
import fi.om.municipalityinitiative.StartJetty;
import fi.om.municipalityinitiative.conf.PropertyNames;
import fi.om.municipalityinitiative.conf.WebTestConfiguration;
import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.service.MailSendingEmailService;
import fi.om.municipalityinitiative.util.Locales;
import fi.om.municipalityinitiative.util.Maybe;
import fi.om.municipalityinitiative.util.TestUtil;
import fi.om.municipalityinitiative.validation.NotTooFastSubmitValidator;
import mockit.Mocked;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.inject.Inject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={WebTestConfiguration.class})
public abstract class WebTestBase {

    protected static final int PORT = 8445; // NOTE: must match port in test.properties/baseUrl

    @Mocked
    MailSendingEmailService emailService;

    @Resource
    protected TestHelper testHelper;

    @Resource
    protected MessageSource messageSource;

    protected Urls urls;

    protected WebDriver driver;

    @Inject
    protected Environment env;

    private static Server jettyServer;

    @BeforeClass
    public static synchronized void initialize() {
        if (jettyServer == null) {
            jettyServer = StartJetty.startService(PORT, "test");
            try {
                while (!jettyServer.isStarted()) {
                    Thread.sleep(250);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Before
    public void init() {
        if (urls == null) {
            Urls.initUrls(env.getRequiredProperty(PropertyNames.baseURL));
            urls = Urls.FI;
        }

        String driverType = env.getProperty("test.web-driver", "default");
        System.out.println("*** driverType = " + driverType);

        formatDriver(driverType);

        if (urls == null) {
            Urls.initUrls("https://localhost:" + PORT);
            urls = Urls.FI;
        }
        NotTooFastSubmitValidator.disable(); // Disable fast-submit validation at ui-tests
        testHelper.dbCleanup();
    }

    protected final void overrideDriverToFirefox(boolean firefox) {
        formatDriver(firefox ? "ff" : "default");

    }
    private void formatDriver(String driverType) {
        switch (driverType) {
            case "ie":
                driver = new InternetExplorerDriver();
                driver.get(urls.frontpage());
                driver.navigate().to("javascript:document.getElementById('overridelink').click()"); // to skip security certificate problem page
                break;
            case "ff":
                driver = new FirefoxDriver();
                break;
            case "default":
            default:
                driver = new HtmlUnitDriver(true);
                break;
        }

        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS); // default is 0!!!
        driver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS); // default is 0!!!
    }

    @After
    public void teardown() {
        driver.quit();
    }
    
    
    //@AfterClass
    public static void destroy() {
        try {
            jettyServer.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helpers
    protected String getMessage(String code) {
        return getMessage(code, null);
    }
    
    protected String getMessage(String code, Object arg) {
        Object[] args = {arg};
        String text = messageSource.getMessage(code, args, Locales.LOCALE_FI);
        text = text.replace('\u00A0', ' '); //replace non breaking space with normal space, because it would be rendered to it
        text = text.trim();
        return text;
    }
    
    protected void open(String href) {
        driver.get(href);
    }

    protected void assertTextByTag(String tag, String text) {
        List<WebElement> elements = driver.findElements(By.tagName(tag));
        for (WebElement element : elements) {
            assertNotNull(element);
            if (text.equals(element.getText().trim())) {
                return;
            }
        }
        System.out.println("--- assertTextByTag --------------- " + tag + ": " + text);
        for (WebElement element : elements) {
            System.out.println("*** '" + element.getText().trim() + "'");
        }
        fail(tag + " tag with text " + text + " not found");
    }
    
    protected void assertMsgContainedByClass(String className, String messageKey) {
        String text = getMessage(messageKey);
        assertTextContainedByClass(className, text);
    }
    
    protected void assertTextContainedByClass(String className, String text) {
        System.out.println("--- assertTextContainedByClass --------------- " + className + ": " + text);
        List<String> elementTexts = Lists.newArrayList();
        List<WebElement> elements = driver.findElements(By.className(className));
        for (WebElement element : elements) {
            assertNotNull(element); 
            String elementText = element.getText().trim();
            elementTexts.add(elementText);
            if (elementText.contains(text)) {
                return;
            }
        }
        System.out.println("--- assertTextContainedByClass --------------- " + className + ": " + text);
        for (WebElement element : elements) {
            System.out.println("*** '" + element.getText().trim() + "'");
        }
        fail(className + " class with text " + text + " not found. Texts found: " + TestUtil.listValues(elementTexts));
    }
    
    protected void assertTextContainedByXPath(String xpathExpression, String text) {

        List<String> elementTexts = Lists.newArrayList();
        List<WebElement> elements = driver.findElements(By.xpath(xpathExpression));
        for (WebElement element : elements) {
            assertNotNull(element); 
            String elementText = element.getText().trim();
            elementTexts.add(elementText);
            if (elementText.contains(text)) {
                return;
            }
        }
        System.out.println("--- assertTextContainedByXPath --------------- " + xpathExpression + ": " + text);
        for (WebElement element : elements) {
            System.out.println("*** '" + element.getText().trim() + "'");
        }
        fail(xpathExpression + " xpath with text " + text + " not found. Texts found: " + TestUtil.listValues(elementTexts));

    }

    protected String pageTitle() {
        return driver.getTitle();
    }
    protected void assertTitle(String text) {
        String title = driver.getTitle();

        System.out.println("--- assertTitle --------------- : " + text);
        System.out.println("*** '" + title.trim() + "'");
        assertThat(title, is(text));
    }

    protected void inputText(String fieldName, String text) {
        findElementWhenClickable(By.name(fieldName)).sendKeys(text);
    }

    protected void inputTextByCSS(String css, String text) {
        findElementWhenClickable(By.cssSelector(css)).sendKeys(text);
    }

    protected void clickByName(String name) {
        findElementWhenClickable(By.name(name)).click();
    }

    protected void clickById(String id) {
        findElementWhenClickable(By.id(id)).click();
    }

    protected void clickLinkContaining(String text) {
        findElementWhenClickable(By.partialLinkText(text)).click();
    }

    protected WebElement findElementWhenClickable(By by) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }
    
    protected WebElement getElemContaining(String containing, String tagName) {

        Maybe<WebElement> maybeElement = getOptionalElemContaining(containing, tagName);
        
        if (maybeElement.isNotPresent()) {
            throw new NullPointerException("Tag not found with text: " + containing);
        }
        
        return maybeElement.get();
    }
    
    protected Maybe<WebElement> getOptionalElemContaining(String containing, String tagName) {
        
     List<WebElement> htmlElements = driver.findElements(By.tagName(tagName));
            
        // wait.until(ExpectedConditions.elementToBeClickable(By.name(name)));
       
        for (WebElement e : htmlElements) {
          if (e.getText().contains(containing)) {
            return Maybe.of(e);
          }
        }
        
        return Maybe.absent();
    }


    protected void loginAsOmUser() {
        open(urls.login(""));
        inputText("u", "admin");
        inputText("p", "admin");
        clickByName("Login");
    }

    protected void loginAsAuthorForLastTestHelperCreatedInitiative() {
        open(urls.loginAuthor(TestHelper.PREVIOUS_TEST_MANAGEMENT_HASH));
        clickByName("Login");
    }

    protected void logout() {
        open(urls.logout());
    }

    protected void assert404() {
        assertThat(getElement(By.tagName("h1")).getText(), is(getMessage("error.404.title")));
    }

    protected WebElement getElement(By by) {
        try {
            return driver.findElement(by);
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException("\nPage " + driver.getCurrentUrl() + "\nTitle: " + driver.getTitle()+"\nCause: " +e.getMessage(), e);
        }
    }
}
