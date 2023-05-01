package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.util.Objects;

public class ArticleInsertionPage extends ThymeleafServlet {
    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        ctx.setVariable("errorDataInserted", Objects.requireNonNullElse(webExchange.getAttributeValue("errorDataInserted"), false));
        ctx.setVariable("errorQuery", Objects.requireNonNullElse(webExchange.getAttributeValue("errorQuery"), false));

        templateEngine.process("articleInsertion", ctx, writer);
    }
}