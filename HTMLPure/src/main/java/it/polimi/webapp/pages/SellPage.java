package it.polimi.webapp.pages;

import it.polimi.webapp.IWebExchanges;
import it.polimi.webapp.ThymeleafServlet;
import it.polimi.webapp.dao.ArticleDao;
import it.polimi.webapp.dao.AuctionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.sql.SQLException;

public class SellPage extends ThymeleafServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(SellPage.class);

    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        var session = IWebExchanges.requireSession(webExchange);
        var args = Pages.getArgs(Pages.SELL_PAGE, webExchange);
        ctx.setVariable("sellArgs", args);

        try (var connection = dataSource.getConnection()) {
            try {
                var closedAuction = new AuctionDao(connection).findAuctions(session.id(), true);
                if (closedAuction != null) {
                    ctx.setVariable("closedAuction", closedAuction);
                } else {
                    ctx.setVariable("errorClosedQuery", true);
                }
            } catch (SQLException e) {
                LOGGER.error("Failed to execute query findAuctions({}, closed: true)", session.id(), e);
                ctx.setVariable("errorClosedQuery", true);
            }

            try {
                var openAuction = new AuctionDao(connection).findAuctions(session.id(), false);
                if (openAuction != null) {
                    ctx.setVariable("openAuction", openAuction);
                } else {
                    ctx.setVariable("errorOpenQuery", true);
                }
            } catch (SQLException e) {
                LOGGER.error("Failed to execute query findAuctions({}, closed: false)", session.id(), e);
                ctx.setVariable("errorOpenQuery", true);
            }

            try {
                var result = new ArticleDao(connection).findAllArticles(session.id());
                if (result != null) {
                    ctx.setVariable("articles", result);
                } else {
                    ctx.setVariable("errorArticlesQuery", true);
                }
            } catch (SQLException e) {
                LOGGER.error("Failed to execute query findAllArticles({})", session.id(), e);
                ctx.setVariable("errorArticlesQuery", true);
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to execute queries in SellPage", e);
            ctx.setVariable("errorClosedQuery", true);
            ctx.setVariable("errorOpenQuery", true);
            ctx.setVariable("errorArticlesQuery", true);
        }

        templateEngine.process("sell", ctx, writer);
    }
}