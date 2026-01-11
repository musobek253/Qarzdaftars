package uz.muso.debtbook.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import uz.muso.debtbook.repository.UserRepository;

import java.io.IOException;

@Component
public class AccessKeyFilter implements Filter {

    private final UserRepository userRepo;

    public AccessKeyFilter(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        if (path.startsWith("/api/auth") || path.startsWith("/api/telegram")) {
            chain.doFilter(req, res);
            return;
        }

        // Allow static files (everything NOT starting with /api)
        if (!path.startsWith("/api")) {
            chain.doFilter(req, res);
            return;
        }

        String key = request.getHeader("X-ACCESS-KEY");
        if (key == null || userRepo.findByAccessKey(key).isEmpty()) {
            response.setStatus(401);
            response.getWriter().write("Unauthorized");
            return;
        }

        chain.doFilter(req, res);
    }
}
