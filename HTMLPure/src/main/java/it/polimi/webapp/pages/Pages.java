package it.polimi.webapp.pages;

import it.polimi.webapp.IWebExchanges;
import it.polimi.webapp.beans.SellPageArgs;
import org.thymeleaf.web.IWebExchange;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Objects;

public class Pages {

    private Pages() {
    }

    private static final String ARGS_ATTR_NAME = "$$__args__$$";

    public static Page<SellPageArgs> SELL_PAGE = new Page<>("/sell", SellPageArgs::new);

    public static <T> void forwardTo(Page<T> page, T args, ServletRequest req, ServletResponse res) throws ServletException, IOException {
        var dispatcher = Objects.requireNonNull(
                req.getRequestDispatcher(page.path()),
                "Couldn't find dispatcher for class " + page.path());
        req.setAttribute(ARGS_ATTR_NAME, args);
        dispatcher.forward(req, res);
    }

    public static <T extends Record> T getArgs(Page<T> page, IWebExchange webExchange) {
        return IWebExchanges.getAttributeOr(webExchange, ARGS_ATTR_NAME, page.fallbackArgsFactory().get());
    }
}