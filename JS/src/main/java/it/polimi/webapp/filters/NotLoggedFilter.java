package it.polimi.webapp.filters;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NotLoggedFilter extends HttpFilter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (req instanceof HttpServletRequest httpReq &&
                httpReq.getSession(false) == null &&
                res instanceof HttpServletResponse httpRes){
            httpRes.sendRedirect(getServletContext().getContextPath() + "/login");
            return;
        }
        chain.doFilter(req, res);
    }
}
