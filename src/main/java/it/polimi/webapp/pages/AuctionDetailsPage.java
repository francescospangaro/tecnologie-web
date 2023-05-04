package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import it.polimi.webapp.dao.AuctionDao;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.sql.SQLException;

public class AuctionDetailsPage extends ThymeleafServlet {
    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        int auctionId = -1;
        try {
            auctionId = Integer.parseInt(webExchange.getRequest().getParameterValue("id"));
        } catch (NumberFormatException ex) {
            ctx.setVariable("errorQuery", true);
        }

        if(auctionId != -1) {
            try (var connection = dataSource.getConnection()) {
                var result = new AuctionDao(connection).findAuctionById(
                        (Integer) webExchange.getSession().getAttributeValue("userId"), auctionId);
                if (result != null) {
                    ctx.setVariable("auction", result);
                } else {
                    ctx.setVariable("errorQuery", true);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        templateEngine.process("auctionDetails", ctx, writer);
    }
}