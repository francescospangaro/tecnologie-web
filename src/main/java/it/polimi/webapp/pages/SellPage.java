package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import it.polimi.webapp.dao.AuctionDao;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Objects;

public class SellPage extends ThymeleafServlet {
    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        ctx.setVariable("goodInsertion", Objects.requireNonNullElse(webExchange.getAttributeValue("goodInsertion"), false));
        ctx.setVariable("goodAuctionInsertion", Objects.requireNonNullElse(webExchange.getAttributeValue("goodInsertion"), false));

        try (var connection = dataSource.getConnection()) {
            var closedAuction = new AuctionDao(connection).findAuctions(
                    (Integer) webExchange.getSession().getAttributeValue("userId"), true);
            if (closedAuction != null) {
                ctx.setVariable("closedAuction", closedAuction);
            } else {
                ctx.setVariable("errorClosedQuery", true);
            }

            var openAuction = new AuctionDao(connection).findAuctions(
                    (Integer) webExchange.getSession().getAttributeValue("userId"), false);
            if (openAuction != null) {
                ctx.setVariable("openAuction", openAuction);
            } else {
                ctx.setVariable("errorOpenQuery", true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        templateEngine.process("sell", ctx, writer);
    }
}