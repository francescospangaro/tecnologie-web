package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import it.polimi.webapp.dao.AuctionDao;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.sql.SQLException;

public class OffersPage extends ThymeleafServlet {
    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        ctx.setVariable("errorMaxOffer", webExchange.getAttributeValue("errorMaxOffer"));
        ctx.setVariable("errorLowPrice", webExchange.getAttributeValue("errorLowPrice"));
        ctx.setVariable("errorQuery", webExchange.getAttributeValue("errorQuery"));
        ctx.setVariable("offerPlaceholder", webExchange.getAttributeValue("offerPlaceholder"));

        Integer auctionId = null;
        try {
            auctionId = Integer.parseInt(webExchange.getRequest().getParameterValue("id"));
        } catch (NumberFormatException e) {
            System.out.println("Magia1");
            ctx.setVariable("errorQuery", true);
        }

        if (auctionId != null) {
            try (var connection = dataSource.getConnection()) {
                var result = new AuctionDao(connection).findOpenAuctionById(auctionId);
                if (result != null)
                    ctx.setVariable("openAuction", result);
                else {
                    System.out.println("Magia2");
                    ctx.setVariable("errorQuery", true);
                }
            } catch (SQLException e) {
                System.out.println("Magia3");
                e.printStackTrace();
                ctx.setVariable("errorQuery", true);
            }
        }
        templateEngine.process("offers", ctx, writer);
    }
}