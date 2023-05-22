package it.polimi.webapp.pages;

import it.polimi.webapp.HttpServlets;
import it.polimi.webapp.IWebExchanges;
import it.polimi.webapp.ThymeleafServlet;
import it.polimi.webapp.beans.SellPageArgs;
import it.polimi.webapp.dao.ArticleDao;
import it.polimi.webapp.dao.AuctionDao;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Objects;

public class SellPage extends ThymeleafServlet {

    private static final String ARGS_ATTR_NAME = "args";

    public static void forwardWith(ServletRequest req, ServletResponse res, SellPageArgs args) throws ServletException, IOException {
        var dispatcher = HttpServlets.getDispatcherFor(req.getServletContext(), SellPage.class);
        req.setAttribute(ARGS_ATTR_NAME, args);
        dispatcher.forward(req, res);
    }

    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        var session = IWebExchanges.requireSession(webExchange);
        var args = IWebExchanges.getAttributeOr(webExchange, ARGS_ATTR_NAME, new SellPageArgs());
        ctx.setVariable(ARGS_ATTR_NAME, args);

        try (var connection = dataSource.getConnection()) {
            var closedAuction = new AuctionDao(connection).findAuctions(session.id(), true);
            if (closedAuction != null) {
                ctx.setVariable("closedAuction", closedAuction);
            } else {
                ctx.setVariable("errorClosedQuery", true);
            }

            var openAuction = new AuctionDao(connection).findAuctions(session.id(), false);
            if (openAuction != null) {
                ctx.setVariable("openAuction", openAuction);
            } else {
                ctx.setVariable("errorOpenQuery", true);
            }

            var result = new ArticleDao(connection).findAllArticles(session.id());
            if (result != null) {
                ctx.setVariable("articles", result);
            } else {
                ctx.setVariable("errorArticlesQuery", true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        templateEngine.process("sell", ctx, writer);
    }
}