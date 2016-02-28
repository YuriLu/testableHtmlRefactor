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
            if (pageData.hasAttribute("Test"))
                includeSetups();
            buffer.append(pageData.getContent()).append("\n");
            if (pageData.hasAttribute("Test"))
                includeTeardowns();
            pageData.setContent(buffer.toString());
            return pageData.getHtml();
        }

        private void includeTeardowns() throws Exception {
            includeIfInherited("TearDown", "teardown");
            if (includeSuiteSetup)
                includeIfInherited(SuiteResponder.SUITE_TEARDOWN_NAME, "teardown");
        }

        private void includeSetups() throws Exception {
            if (includeSuiteSetup)
                includeIfInherited(SuiteResponder.SUITE_SETUP_NAME, "setup");
            includeIfInherited("SetUp", "setup");
        }

        private void includeIfInherited(String pageName, String mode) throws Exception {
            WikiPage page = PageCrawlerImpl.getInheritedPage(pageName, wikiPage);
            if (page != null)
                buffer.append(includePage(page, mode));
        }

        private String includePage(WikiPage page, String mode) throws Exception {
            WikiPagePath pagePath = crawler.getFullPath(page);
            String pagePathName = PathParser.render(pagePath);
            return String.format("!include -%s .%s\n", mode , pagePathName);
        }
    }
}
