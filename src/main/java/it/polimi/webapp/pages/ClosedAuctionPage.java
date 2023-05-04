package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import it.polimi.webapp.dao.AuctionDao;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.sql.SQLException;

public class ClosedAuctionPage extends ThymeleafServlet {
    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        try (var connection = dataSource.getConnection()){
            var result = new AuctionDao(connection).findAuctions(
                    (Integer) webExchange.getSession().getAttributeValue("userId"), true);
            if(result!=null){
                ctx.setVariable("closedAuction", result);
            }else{
                ctx.setVariable("errorQuery", true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        templateEngine.process("closedAuction", ctx, writer);
    }
}