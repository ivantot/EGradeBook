package Brains2021.electronic.gradeBook.config;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

	private String securityKey;

	private final Logger logger = (Logger) LoggerFactory.getLogger(this.getClass());

	public JWTAuthorizationFilter(String securityKey) {
		super();
		this.securityKey = securityKey;
	}

	/**
	 * filter that provides token validation
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		logger.info("~~TOKEN AUTHORIZATION FILTER~~ Attempting to validate token.");
		// check if jwt token exists
		if (checkJWTToken(request)) {
			// check the validity of jwt token, return authorities/claims
			Claims claims = validateToken(request);
			//sanity check if token has authority
			logger.info("~~TOKEN AUTHORIZATION FILTER~~ Checking claims.");
			if (claims.get("authorities") != null) {
				logger.info(
						"~~TOKEN AUTHORIZATION FILTER~~ Authorities are present. Asigning security key and parsing claims.");
				// if valid setup spring security based on token
				setUpSpringAuthentication(claims);
			} else {
				logger.warn("~~TOKEN AUTHORIZATION FILTER~~ No authorities provided, clearing security context.");
				// if not clear context
				SecurityContextHolder.clearContext();
			}
			// if not valid clear context	
		} else {
			logger.warn("~~TOKEN AUTHORIZATION FILTER~~ Token invalid, clearing security context.");
			SecurityContextHolder.clearContext();// sve sto je validirano do ovog momenta, ponistava se
		}
		// invoke filter chain
		filterChain.doFilter(request, response);

	}

	private Boolean checkJWTToken(HttpServletRequest request) {
		logger.info("~~TOKEN CHECK~~ Method for reading authorities.");
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			logger.info("~~TOKEN CHECK~~ No authorities.");
			return false;
		}
		logger.info("~~TOKEN CHECK~~ Authorities present.");
		return true;
	}

	private Claims validateToken(HttpServletRequest request) {
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		return Jwts.parser().setSigningKey(this.securityKey).parseClaimsJws(jwtToken).getBody();
	}

	private void setUpSpringAuthentication(Claims claims) {
		logger.info("~~SET UP SPRING AUTHENTICATION~~ Translate to java object to be handled by spring.");
		@SuppressWarnings("unchecked")
		List<String> authorities = (List<String>) claims.get("authorities");
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
				authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
}
