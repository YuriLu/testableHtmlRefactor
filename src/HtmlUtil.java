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
        private String content;
        private WikiPage wikiPage;
        private PageCrawler crawler;
        
        public TestableHtmlMaker(PageData pageData, boolean includeSuiteSetup){
            content = "";
            wikiPage = pageData.getWikiPage();
            crawler = wikiPage.getPageCrawler();
            this.pageData = pageData;
            this.includeSuiteSetup = includeSuiteSetup;
        }
        
        public String invoke() throws Exception{
            if (isTestPage()){
                surroundPageWithSetupsAndTeardowns();
            }
            return pageData.getHtml();
        }

        private void surroundPageWithSetupsAndTeardowns() throws Exception {
            content += includeSetups();
            content += pageData.getContent() + "\n";
            content += includeTeardowns();
            pageData.setContent(content);
        }

        private boolean isTestPage() throws Exception {
            return pageData.hasAttribute("Test");
        }

        private String includeTeardowns() throws Exception {
            String teardowns = "";
            teardowns += includeIfInherited("TearDown", "teardown");
            if (includeSuiteSetup)
                teardowns += includeIfInherited(SuiteResponder.SUITE_TEARDOWN_NAME, "teardown");
            return teardowns;
        }

        private String includeSetups() throws Exception {
            String setups = "";
            if (includeSuiteSetup)
                setups += includeIfInherited(SuiteResponder.SUITE_SETUP_NAME, "setup");
            setups += includeIfInherited("SetUp", "setup");
            return setups;
        }

        private String includeIfInherited(String pageName, String mode) throws Exception {
            WikiPage page = PageCrawlerImpl.getInheritedPage(pageName, wikiPage);
            if (page != null)
                return includePage(page, mode);
            return "";
        }

        private String includePage(WikiPage page, String mode) throws Exception {
            WikiPagePath pagePath = crawler.getFullPath(page);
            String pagePathName = PathParser.render(pagePath);
            return String.format("!include -%s .%s\n", mode , pagePathName);
        }
    }
}
