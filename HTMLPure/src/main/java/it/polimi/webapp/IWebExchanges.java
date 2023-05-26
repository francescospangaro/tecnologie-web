package it.polimi.webapp;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.thymeleaf.web.IWebExchange;

public class IWebExchanges {

    private IWebExchanges() {
    }

    @Contract("_, _, !null -> !null; _, _, _ -> _")
    public static @Nullable Integer getAttributeOr(IWebExchange webExchange, String attributeName, @Nullable Integer fallback) {
        var v = webExchange.getAttributeValue(attributeName);
        if(v == null)
            return fallback;

        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Record> T getAttributeOr(IWebExchange webExchange, String attributeName, T fallback) {
        var val = webExchange.getAttributeValue(attributeName);
        if(val == null)
            return fallback;

        return fallback.getClass().isInstance(val) ? (T) val : fallback;
    }

    public static UserSession requireSession(IWebExchange webExchange) {
        var session = getSession(webExchange);
        if(session == null)
            throw new IllegalStateException("UserSession accessed a protected page without logging in");
        return session;
    }

    public static @Nullable UserSession getSession(IWebExchange webExchange) {
        if(!webExchange.hasSession())
            return null;

        var session = webExchange.getSession().getAttributeValue("user");
        if(!(session instanceof UserSession u))
            return null;

        return u;
    }
}