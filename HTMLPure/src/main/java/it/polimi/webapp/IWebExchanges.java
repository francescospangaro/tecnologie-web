package it.polimi.webapp;

import it.polimi.webapp.beans.User;
import org.jetbrains.annotations.Nullable;
import org.thymeleaf.web.IWebExchange;

public class IWebExchanges {

    private IWebExchanges() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Record> T getAttributeOr(IWebExchange webExchange, String attributeName, T fallback) {
        var val = webExchange.getAttributeValue(attributeName);
        if(val == null)
            return fallback;

        return fallback.getClass().isInstance(val) ? (T) val : fallback;
    }

    public static User requireSession(IWebExchange webExchange) {
        var session = getSession(webExchange);
        if(session == null)
            throw new IllegalStateException("User accessed a protected page without logging in");
        return session;
    }

    public static @Nullable User getSession(IWebExchange webExchange) {
        if(!webExchange.hasSession())
            return null;

        var session = webExchange.getSession().getAttributeValue("user");
        if(!(session instanceof User u))
            return null;

        return u;
    }
}