package cn.edu.ouc.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录验证过滤器：拦截受限资源，未登录用户跳转至登录页
 */
@WebFilter("/*") // 拦截所有请求
public class LoginFilter implements Filter {

    // 无需登录即可访问的路径（白名单）
    private static final String[] ALLOWED_PATHS = {
            "/login.jsp", "/login", "/logout",
            "/fileList", "/media", "/rank",
            "/bootstrap/", "/js/"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 初始化操作：无特殊需求
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // 1. 获取当前请求路径（去除上下文路径）
        String contextPath = req.getContextPath();
        String requestURI = req.getRequestURI();
        String path = requestURI.substring(contextPath.length());

        // 2. 检查是否在白名单中
        boolean isAllowed = false;
        for (String allowedPath : ALLOWED_PATHS) {
            if (path.startsWith(allowedPath)) {
                isAllowed = true;
                break;
            }
        }

        if (isAllowed) {
            // 白名单路径，直接放行
            chain.doFilter(request, response);
        } else {
            // 非白名单路径，检查是否已登录
            Object loginUser = req.getSession().getAttribute("loginUser");
            if (loginUser != null) {
                // 已登录，放行
                chain.doFilter(request, response);
            } else {
                // 未登录，重定向至登录页
                resp.sendRedirect(contextPath + "/login.jsp");
            }
        }
    }

    @Override
    public void destroy() {
        // 销毁操作：无特殊需求
    }
}