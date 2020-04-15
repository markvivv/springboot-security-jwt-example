package examples.spring.project.security.jwt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private Logger logger = LogManager.getLogger(getClass());

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
		logger.warn("Jwt authentication failed: {}", authException);

		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Jwt authentication failed. " + authException.getMessage());

	}

}
