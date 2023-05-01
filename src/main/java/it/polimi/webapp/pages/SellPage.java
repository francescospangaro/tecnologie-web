package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
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

        templateEngine.process("sell", ctx, writer);
    }
}