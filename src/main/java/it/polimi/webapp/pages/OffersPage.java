package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import it.polimi.webapp.beans.OpenAuction;
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

        Integer auctionId = null;
        try {
            auctionId = Integer.parseInt(webExchange.getRequest().getParameterValue("id"));
            ctx.setVariable("errorQuery", false);
        } catch (NumberFormatException e) {
            System.out.println(webExchange.getRequest().getParameterValue("id"));
            ctx.setVariable("errorQuery", true);
        }

        if (auctionId != null) {
            try (var connection = dataSource.getConnection()) {
                var result = new AuctionDao(connection).findAuctionByAuctionId(auctionId);
                ctx.setVariable("openAuction", result);
                ctx.setVariable("errorQuery", false);
            } catch (SQLException e) {
                ctx.setVariable("errorQuery", true);
            }
        } else {
            ctx.setVariable("openAuction", new OpenAuction());
        }

        templateEngine.process("offers", ctx, writer);
    }
}