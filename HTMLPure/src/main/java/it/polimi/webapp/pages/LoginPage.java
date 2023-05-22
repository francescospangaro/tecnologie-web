package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import javax.sql.DataSource;
import java.io.Writer;
import java.util.Objects;

public class LoginPage extends ThymeleafServlet {
    @Override
    protected void process(IWebExchange webExchange,
                           ITemplateEngine templateEngine,
                           DataSource dataSource,
                           Writer writer) {

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        ctx.setVariable("errorCred", Objects.requireNonNullElse(webExchange.getAttributeValue("errorCred"), false));
        ctx.setVariable("loginUsername", webExchange.getAttributeValue("loginUsername"));
        ctx.setVariable("errorNotFound", Objects.requireNonNullElse(webExchange.getAttributeValue("errorNotFound"), false));

        templateEngine.process("login", ctx, writer);
    }
}
