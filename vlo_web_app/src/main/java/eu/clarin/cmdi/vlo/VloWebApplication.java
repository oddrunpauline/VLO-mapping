package eu.clarin.cmdi.vlo;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.VloContextConfig;
import eu.clarin.cmdi.vlo.dao.SearchResultsDao;
import eu.clarin.cmdi.vlo.pages.BasePage;
import eu.clarin.cmdi.vlo.pages.BasePanel;
import eu.clarin.cmdi.vlo.pages.FacetedSearchPage;
import eu.clarin.cmdi.vlo.pages.ShowAllFacetValuesPage;
import static eu.clarin.cmdi.vlo.pages.ShowAllFacetValuesPage.SELECTED_FACET_PARAM;
import eu.clarin.cmdi.vlo.pages.ShowResultPage;
import javax.servlet.ServletContext;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Virtual Language Observatory web application<br><br>
 *
 * <describe VLO>
 *
 * While the application is intended to run inside a web server container,
 * running the Start class enables you to run it without outside one.
 */
public class VloWebApplication extends WebApplication {

    private final static Logger logger = LoggerFactory.getLogger(VloWebApplication.class);
    
    /**
     * Customised client request cycle<br><br>
     *
     * <intercept resquest in order to update session parameter list>
     *
     * Add behaviour to the web request handling by retrieving persistent
     * parameters to the application from from client requests, and store the in
     * the application object.
     */
    private class CustomRequestCycleListener extends AbstractRequestCycleListener {

        @Override
        public void onBeginRequest(RequestCycle cycle) {
            // first, invoke the default behavior
            super.onBeginRequest(cycle);
            // after that, get the parameters of the request itself
            IRequestParameters reqParam = cycle.getRequest().getRequestParameters();

            // from these, get the parameters represented in the URL
            // Map <String, String[]> map = this.getWebRequest().getParameterMap();
            // check if there is a theme parameter
            StringValue object = reqParam.getParameterValue("theme");

            if (object.isEmpty()) {
                // no theme choosen, keep the current one
            } else {
                final String themeName = object.toString();
                // check if the users requests a different theme
                if (themeName.matches(((VloSession) Session.get()).getCurrentTheme().name)) {
                    // current theme requested, nothing to do
                } else {
                    // different theme requested, compose it
                    VloSession.get().setCurrentTheme(new Theme(themeName));
                    // remember the theme as a vlo session page parameter
                    PageParameters params = new PageParameters();
                    params.add("theme", VloSession.get().getCurrentTheme().name);
                    VloSession.get().addVloSessionPageParameters(params);
                }
            }
        }
    }

    /**
     * Flag indicating whether or not the application object lives in a web
     * server context.
     */
    boolean inContext;

    /**
     * Method that will be invoked when the application starts.
     */
    @Override
    public void init() {

        if (inContext) {
            
            /*
             * send messages to objects that need a static reference to this web
             * application object. While this, at a one point in time, was only 
             * required in the case of the results page BookmarkablePageLink 
             * method, uniform approach might be the most prefarable one.
             */
            BasePage.setWebApp(this);
            BasePanel.setWebApp(this);

            // install theme -> compose theme
            // get the servlet's context
            ServletContext servletContext;
            servletContext = this.getServletContext();

            /*
             * Send the application context to the configuration object to
             * enable it to read an external {@literal VloConfig.xml}
             * configuration file.
             */
            VloContextConfig.switchToExternalConfig(servletContext);

            getRequestCycleListeners().add(new CustomRequestCycleListener());
        }
        
        // configure cache by applying the vlo configuration settings to it
        final int pagesInApplicationCache = VloConfig.getPagesInApplicationCache();
        logger.info("Setting Wicket in-memory cache size to {}", pagesInApplicationCache);
        this.getStoreSettings().setInmemoryCacheSize(pagesInApplicationCache);
        
        final Bytes sessionCacheSize = Bytes.kilobytes((long)VloConfig.getSessionCacheSize());
        logger.info("Setting Wicket max size per session to {}", sessionCacheSize);
        this.getStoreSettings().setMaxSizePerSession(sessionCacheSize);

        // creata an object referring to the search results
        searchResults = new SearchResultsDao();

        // mount pages on URL's for bookmarkable links
        mountPages();

        // hand over control to the application
    }

    private void mountPages() {
        // Record (query result) page. E.g. /vlo/record?docId=abc123
        // (cannot encode docId in path because it contains a slash)
        mountPage("/record", ShowResultPage.class);

        // All facet values page. E.g. /vlo/values/genre?facetMinOccurs=1
        // (min occurs not in path because it's a filter on the facet list)
        mountPage("/values/${" + SELECTED_FACET_PARAM + "}", ShowAllFacetValuesPage.class);
    }

    // remember the search results
    private SearchResultsDao searchResults;

    /**
     * Web application constructor<br><br>
     *
     * Create an application instance configured to be living inside a web
     * server container.
     */
    public VloWebApplication() {
        
        /*
         * Read the application's packaged configuration
         * 
         * Because on instantiation a web application cannot said to be living 
         * in a web server context, parameters defined in the context can only 
         * be added to the configuration later, in this case: when the {@literal
         * init()} method will be invoked.
         */
        VloConfig.readPackagedConfig();

        // let the {@literal init()} method know that there will be a context
        inContext = true;
    }

    /**
     * Web application constructor<br><br>
     *
     * Allows for the creation of an application instance that does not rely on
     * a web server context. When send the message 'false', this constructor
     * will create an object that will not look for an external configuration
     * file; it will exclusively rely on the packaged configuration. Typically,
     * the application's tests will send false to the application constructor.
     * <br><br>
     *
     * @param inContext If and only if this parameter equals true. later on, the
     * {@literal init} method will try to determine the web server's container
     * context so that, if it is defined in it, an external configuration can be
     * switched to.
     */
    public VloWebApplication(Boolean inContext) {

        // remember that the application does not live in a web server context
        this.inContext = inContext;

        searchResults = new SearchResultsDao();
    }

    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<FacetedSearchPage> getHomePage() {
        return FacetedSearchPage.class;
    }

    public SearchResultsDao getSearchResultsDao() {
        return searchResults;
    }

    @Override
    public VloSession newSession(Request request, Response response) {
        return new VloSession(request);
    }
}
