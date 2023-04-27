package it.polimi.webapp.pages;

import it.polimi.webapp.ThymeleafServlet;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;

import java.io.Writer;
import java.util.Calendar;

public class HomePage extends ThymeleafServlet {
    @Override
    protected void process(IWebExchange webExchange, ITemplateEngine templateEngine, Writer writer) {
        webExchange.getSession().setAttributeValue("user", "John");

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
        ctx.setVariable("today", Calendar.getInstance());
        templateEngine.process("home", ctx, writer);
    }
}
