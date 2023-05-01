package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.sql.PreparedStatement;
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

        try (var connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT codArticolo, nome, descrizione FROM articolo WHERE utente_idUtente = ?");
        ) {
            preparedStatement.setInt(1, (Integer) webExchange.getSession().getAttributeValue("userId"));
            try (var result = preparedStatement.executeQuery()){
                // TODO: need to check what happens if no articles are found
                //  and maybe send an error message
                ctx.setVariable("articles", Objects.requireNonNull(result));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        templateEngine.process("auctionInsertion", ctx, writer);
    }
}