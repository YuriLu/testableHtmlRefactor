import fitnesse.responders.run.SuiteResponder;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class HtmlUtil {
    public static String testableHtml (PageData pageData, boolean includeSuiteSetup) throws Exception {
       return new TestableHtmlMaker(pageData, includeSuiteSetup).invoke();
    } 
    
    private static class TestableHtmlMaker{
        private PageData pageData;
        private boolean includeSuiteSetup;
        private StringBuffer buffer;
        private WikiPage wikiPage;
        private PageCrawler crawler;
        
        public TestableHtmlMaker(PageData pageData, boolean includeSuiteSetup){
            buffer = new StringBuffer();
            wikiPage = pageData.getWikiPage();
            crawler = wikiPage.getPageCrawler();
            this.pageData = pageData;
            this.includeSuiteSetup = includeSuiteSetup;
        }
        
        public String invoke() throws Exception{
            if (pageData.hasAttribute("Test")) {
                String mode = "setup";
                if (includeSuiteSetup) {
                    WikiPage suiteSetup = PageCrawlerImpl.getInheritedPage(SuiteResponder.SUITE_SETUP_NAME, wikiPage);
                    if (suiteSetup != null) {
                        WikiPagePath pagePath = crawler.getFullPath(suiteSetup);
                        String pagePathName = PathParser.render(pagePath);
                        buffer.append("!include -" + mode + " .").append(pagePathName).append("\n");
                    }
                }
                WikiPage setup = PageCrawlerImpl.getInheritedPage("SetUp", wikiPage);
                if (setup != null) {
                    WikiPagePath setupPath = crawler.getFullPath(setup);
                    String setupPathName = PathParser.render(setupPath);
                    buffer.append("!include -" + mode + " .").append(setupPathName).append("\n");
                }
            }
            buffer.append(pageData.getContent()).append("\n");
            if (pageData.hasAttribute("Test")) {
                WikiPage teardown = PageCrawlerImpl.getInheritedPage("TearDown", wikiPage);
                String mode = "teardown";
                if (teardown != null) {
                    WikiPagePath tearDownPath = crawler.getFullPath(teardown);
                    String tearDownPathName = PathParser.render(tearDownPath);
                    buffer.append("!include -" + mode + " .").append(tearDownPathName).append("\n");
                }
                if (includeSuiteSetup) {
                    WikiPage suiteTeardown = PageCrawlerImpl.getInheritedPage(SuiteResponder.SUITE_TEARDOWN_NAME, wikiPage);
                    if (suiteTeardown != null) {
                        WikiPagePath pagePath = crawler.getFullPath(suiteTeardown);
                        String pagePathName = PathParser.render(pagePath);
                        buffer.append("!include -" + mode + " .").append(pagePathName).append("\n");
                    }
                }
            }
            pageData.setContent(buffer.toString());
            return pageData.getHtml();
        }
    }
}
