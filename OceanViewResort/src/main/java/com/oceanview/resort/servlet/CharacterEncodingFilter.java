package com.oceanview.resort.servlet;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Forces UTF-8 encoding on every request and response passing through Tomcat.
 *
 * Key changes vs the naive version:
 *  - Request encoding is forced BEFORE any Servlet reads the body.
 *  - Response character encoding is set to UTF-8 so that any subsequent
 *    setContentType() call in a Servlet does not reset to ISO-8859-1.
 *  - For HTTP responses that have NOT yet committed, we also call
 *    setCharacterEncoding AFTER the chain so that late commits are covered.
 *
 * Strictly uses javax.servlet.* (Tomcat 8.5 compatible).
 */
@WebFilter("/*")
public class CharacterEncodingFilter implements Filter {

    private static final String ENCODING = "UTF-8";

    @Override
    public void init(FilterConfig config) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Force UTF-8 on the incoming request body (e.g. POST form data).
        request.setCharacterEncoding(ENCODING);

        // Force UTF-8 on the response BEFORE any servlet or Tomcat's DefaultServlet
        // begins writing.  setCharacterEncoding() must be called before getWriter()
        // and must be called before setContentType() to take effect for static files.
        response.setCharacterEncoding(ENCODING);

        // For HTTP responses, ensure the charset is reflected in the Content-Type header
        // immediately so that browsers receive correct encoding information for UTF-8
        // characters (emojis, Arabic, accented characters, etc.).
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResp = (HttpServletResponse) response;
            httpResp.setCharacterEncoding(ENCODING);
        }

        chain.doFilter(request, response);

        // After chain: if the response was not yet committed, reinforce UTF-8.
        // This covers cases where a Servlet set a Content-Type without a charset.
        if (!response.isCommitted()) {
            response.setCharacterEncoding(ENCODING);
        }
    }

    @Override
    public void destroy() {}
}
