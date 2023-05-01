package it.polimi.webapp;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebApplication;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JavaxServletWebApplication;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;

/** Base servlet class which will be used by all Servlets which return a Thymeleaf templated page */
public abstract class ThymeleafServlet extends HttpServlet {

    protected boolean isDevelopmentMode;

    private ITemplateEngine templateEngine;
    private JavaxServletWebApplication application;
    private DataSource dataSource;

    @Override
    @Initializer
    @MustBeInvokedByOverriders
    public void init() throws ServletException {
        this.isDevelopmentMode = Boolean.parseBoolean(getServletContext().getInitParameter("developmentMode"));

        this.application = JavaxServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = buildTemplateEngine(this.application);

        try {
            this.dataSource = (DataSource) new InitialContext().lookup("java:/comp/env/jdbc/AsteDB");
        } catch (NamingException e) {
            throw new ServletException("Failed to get Context", e);
        }

        if (this.dataSource == null)
            throw new ServletException("Data source not found!");
    }

    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doProcess(request, response);
    }

    @Override
    protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        //this is to support request dispatcher forwarding (doesn't work with doPost if doProcess is only done in doGet)
        doProcess(request, response);
    }

    protected void doProcess(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            final IWebExchange webExchange = application.buildExchange(request, response);

            /*
             * Write the response headers
             */
            response.setContentType("text/html;charset=UTF-8");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

            /*
             * Obtain the response writer
             */
            final Writer writer = response.getWriter();

            /*
             * Execute the controller and process view template,
             * writing the results to the response writer.
             */
            process(webExchange, templateEngine, dataSource, writer);
        } catch (final Exception e) {
            try {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (final IOException ignored) {
                // Just ignore this
            }
            throw new ServletException(e);
        }
    }

    protected abstract void process(IWebExchange webExchange,
                                    ITemplateEngine templateEngine,
                                    DataSource dataSource,
                                    Writer writer)
            throws Exception;

    private ITemplateEngine buildTemplateEngine(final IWebApplication application) {
        final WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(application);

        // HTML is the default mode, but we will set it anyway for better understanding of code
        templateResolver.setTemplateMode(TemplateMode.HTML);
        // This will convert "home" to "/WEB-INF/templates/home.html"
        templateResolver.setPrefix("/WEB-INF/templates/");
        templateResolver.setSuffix(".html");

        if(!isDevelopmentMode) {
            // Set template cache TTL to 1 hour. If not set, entries would live in cache until expelled by LRU
            templateResolver.setCacheTTLMs(3600000L);
            // Cache is set to true by default. Set to false if you want templates to
            // be automatically updated when modified.
            templateResolver.setCacheable(true);
        }

        final TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        return templateEngine;
    }
}
