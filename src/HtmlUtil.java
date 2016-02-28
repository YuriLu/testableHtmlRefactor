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
                        includePage(suiteSetup, mode);
                    }
                }
                WikiPage setup = PageCrawlerImpl.getInheritedPage("SetUp", wikiPage);
                if (setup != null) {
                    includePage(setup, mode);
                }
            }
            buffer.append(pageData.getContent()).append("\n");
            if (pageData.hasAttribute("Test")) {
                WikiPage teardown = PageCrawlerImpl.getInheritedPage("TearDown", wikiPage);
                String mode = "teardown";
                if (teardown != null) {
                    includePage(teardown, mode);
                }
                if (includeSuiteSetup) {
                    WikiPage suiteTeardown = PageCrawlerImpl.getInheritedPage(SuiteResponder.SUITE_TEARDOWN_NAME, wikiPage);
                    if (suiteTeardown != null) {
                        includePage(suiteTeardown, mode);
                    }
                }
            }
            pageData.setContent(buffer.toString());
            return pageData.getHtml();
        }

        private void includePage(WikiPage page, String mode) throws Exception {
            WikiPagePath pagePath = crawler.getFullPath(page);
            String pagePathName = PathParser.render(pagePath);
            buffer.append(String.format("!include -%s .%s\n", mode , pagePathName));
        }
    }
}
