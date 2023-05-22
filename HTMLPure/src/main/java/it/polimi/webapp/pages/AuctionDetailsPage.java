package it.polimi.webapp.pages;

import it.polimi.webapp.IWebExchanges;
import it.polimi.webapp.ThymeleafServlet;
import it.polimi.webapp.dao.AuctionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.sql.SQLException;

public class AuctionDetailsPage extends ThymeleafServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuctionDetailsPage.class);

    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
        var session = IWebExchanges.requireSession(webExchange);

        var auctionId = IWebExchanges.getAttributeOr(webExchange, "id", (Integer) null);
        if (auctionId == null) {
            ctx.setVariable("errorQuery", true);
        } else {
            try (var connection = dataSource.getConnection()) {
                var result = new AuctionDao(connection).findAuctionByIds(session.id(), auctionId);
                if (result != null) {
                    ctx.setVariable("auction", result);
                } else {
                    ctx.setVariable("errorQuery", true);
                }
            } catch (SQLException e) {
                LOGGER.error("Failed to findAuctionByIds({}, {})", session.id(), auctionId, e);
                ctx.setVariable("errorQuery", true);
            }
        }

        templateEngine.process("auctionDetails", ctx, writer);
    }
}