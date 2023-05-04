package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import it.polimi.webapp.dao.ArticleDao;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.sql.SQLException;
import java.util.Objects;

public class AuctionInsertionPage extends ThymeleafServlet {
    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        ctx.setVariable("errorAuctionDataInserted", Objects.requireNonNullElse(
                webExchange.getAttributeValue("errorAuctionDataInserted"), false));

        ctx.setVariable("errorAuctionQuery", Objects.requireNonNullElse(
                webExchange.getAttributeValue("errorAuctionQuery"), false));

        try (var connection = dataSource.getConnection()){
            var result = new ArticleDao(connection).findAllArticles(
                    (Integer) webExchange.getSession().getAttributeValue("userId"));
            if(result!=null){
                ctx.setVariable("articles", result);
            }else{
                ctx.setVariable("errorQuery", true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        templateEngine.process("auctionInsertion", ctx, writer);
    }
}