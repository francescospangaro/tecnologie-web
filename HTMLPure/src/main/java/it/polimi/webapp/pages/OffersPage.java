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

public class OffersPage extends ThymeleafServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(OffersPage.class);

    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
        var args = Pages.getArgs(Pages.OFFERS_PAGE, webExchange);
        ctx.setVariable("offersArgs", args);

        Integer auctionId = IWebExchanges.getAttributeOr(webExchange, "id", args.auctionId());
        if (auctionId == null) {
            ctx.setVariable("errorQuery", true);
        } else {
            try (var connection = dataSource.getConnection()) {
                var result = new AuctionDao(connection).findOpenAuctionById(auctionId);
                if (result != null)
                    ctx.setVariable("openAuction", result);
                else {
                    ctx.setVariable("errorQuery", true);
                }
            } catch (SQLException e) {
                LOGGER.error("Failed to findOpenAuctionById({})", auctionId, e);
                ctx.setVariable("errorQuery", true);
            }
        }

        templateEngine.process("offers", ctx, writer);
    }
}