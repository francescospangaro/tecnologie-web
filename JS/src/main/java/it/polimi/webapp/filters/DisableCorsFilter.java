package it.polimi.webapp.filters;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DisableCorsFilter extends HttpFilter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (res instanceof HttpServletResponse httpRes) {
            httpRes.setHeader("Access-Control-Allow-Origin", "*");
            // httpRes.setHeader("Access-Control-Request-Method", "GET, POST, PUT, UPDATE, DELETE, OPTIONS");
        }

        chain.doFilter(req, res);
    }
}
