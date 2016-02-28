
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class HtmlUtilTest {
    
    private PageData pageData;
    private PageCrawler crawler;
    private WikiPage root;
    private WikiPage testPage;
	
        
    @Before
    public void setUp() throws Exception {
        root = InMemoryPage.makeRoot("RooT");
        crawler = root.getPageCrawler();
        testPage = addPage("TestPage", "!define TEST_SYSTEM {slim}\n"
                        + "the content");
        addPage("SetUp", "setup");
        addPage("TearDown", "teardown");
        addPage("SuiteSetUp", "suiteSetUp");
        addPage("SuiteTearDown", "suiteTearDown");

        crawler.addPage(testPage, PathParser.parse("ScenarioLibrary"),
                        "scenario library 2");

        pageData = testPage.getData();
    }

    private WikiPage addPage(String pageName, String content) throws Exception {
            return crawler.addPage(root, PathParser.parse(pageName), content);
    }

    private String removeMagicNumber(String expectedResult) {
            return expectedResult.replaceAll("[-]*\\d+", "");
    }

    private String generateHtml(boolean includeSuiteSetup) {
        String testableHtml;
        try{
            testableHtml = HtmlUtil.testableHtml(pageData,
                            includeSuiteSetup);
        }catch(Exception e){
            throw new RuntimeException (e);
        }
            return removeMagicNumber(testableHtml);
    }
        
    @Test
    public void testSystemIsPresent () {
            String html = generateHtml(false);
            assertTrue(html.contains("TEST_SYSTEM"));
            html = generateHtml(true);
            assertTrue(html.contains("TEST_SYSTEM"));
    }
        
    @Test
    public void suiteSetupIsPresent () {
            String html = generateHtml(true);
            assertTrue(html.contains("SuiteSetUp"));
    }
    
    @Test
    public void suiteTearDownIsPresent () {
            String html = generateHtml(true);
            assertTrue(html.contains("suiteTearDown"));
    }
        
    @Test
    public void tearDownIsPresent () {
            String html = generateHtml(false);
            assertTrue(html.contains("teardown"));
    }
        
    @Test
    public void setUpIsPresent () {
            String html = generateHtml(false);
            assertTrue(html.contains("SetUp"));
    }
}
