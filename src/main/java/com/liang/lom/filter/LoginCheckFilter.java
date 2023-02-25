package com.liang.lom.filter;

import com.alibaba.fastjson.JSON;
import com.liang.lom.common.BaseContext;
import com.liang.lom.common.R;
import com.liang.lom.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    // 路径匹配器,支持通配符的写法
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 获取本次请求的uri
        String requestURI = request.getRequestURI();

        log.info("拦截到的url:{}", requestURI);
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg",
                "/user/login",
        };
        // 判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        // 如果不需要处理直接放行
        if (check) {
            log.info("本次请求不需要处理{}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }
        // 4-1判断登陆状态,如果已经登录,则直接放行(后台管理)
        if (request.getSession().getAttribute(Constant.EMPLOYEE) != null) {
            BaseContext.setCurrentId((Long) request.getSession().getAttribute(Constant.EMPLOYEE));
            filterChain.doFilter(request, response);
            return;
        }

        // 4-2判断登陆状态,如果已经登录,则直接放行(移动端)
        if (request.getSession().getAttribute(Constant.USER) != null) {
            BaseContext.setCurrentId((Long) request.getSession().getAttribute(Constant.USER));
            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户未登录");
        // 未登录则返回未登录的结果,通过输出流的方式向客户端响应数据
        response.getWriter().write(JSON.toJSONString(R.error(Constant.NOTLOGIN)));
        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     *
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                return true;
            }
        }
        return false;
    }
}
