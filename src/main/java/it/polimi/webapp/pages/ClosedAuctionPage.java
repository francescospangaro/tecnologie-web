package it.polimi.webapp.pages;

import it.polimi.webapp.AuctionList;
import it.polimi.webapp.ThymeleafServlet;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;

public class ClosedAuctionPage extends ThymeleafServlet {
    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        try (var connection = dataSource.getConnection();
             var query = connection.prepareStatement("""
                     SELECT asta.idAsta, articolo.codArticolo, articolo.nome, articolo.descrizione, articolo.immagine, articolo.prezzo
                     FROM articolo, asta, astearticoli, utente
                     WHERE articolo.codArticolo=astearticoli.articolo_codArticolo
                     AND asta.idAsta=astearticoli.asta_idAsta
                     AND articolo.utente_idUtente=?
                     AND asta.chiusa=true""")) {
            query.setInt(1, (Integer) webExchange.getSession().getAttributeValue("userId"));
            try (var temp = query.executeQuery()) {
                List<AuctionList> result = new AuctionList().toAuctionList(temp);
                if (result.size() > 0) {
                    ctx.setVariable("closedAuction", result);
                } else {
                    ctx.setVariable("errorQuery", true);
                    return;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        templateEngine.process("closedAuction", ctx, writer);
    }
}