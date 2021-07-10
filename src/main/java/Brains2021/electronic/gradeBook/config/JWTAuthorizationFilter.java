package Brains2021.electronic.gradeBook.config;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

	private String securityKey;

	public JWTAuthorizationFilter(String securityKey) {
		super();
		this.securityKey = securityKey;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// check if jwt token exists
		if (checkJWTToken(request)) {
			// check the validity of jwt token, return authorities/claims
			Claims claims = validateToken(request);
			//sanity check if token has authority
			if (claims.get("authorities") != null) {
				// if valid setup spring security based on token
				setUpSpringAuthentication(claims);
			} else {
				// if not clear context
				SecurityContextHolder.clearContext();
			}
			// if not valid clear context	
		} else {
			SecurityContextHolder.clearContext();// sve sto je validirano do ovog momenta, ponistava se
		}

		// invoke filter chain
		filterChain.doFilter(request, response);

	}

	private Boolean checkJWTToken(HttpServletRequest request) {
		String authorizationHeader = request.getHeader("Authorization");
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			return false;
		}
		return true;
	}

	private Claims validateToken(HttpServletRequest request) {
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		return Jwts.parser().setSigningKey(this.securityKey).parseClaimsJws(jwtToken).getBody();
	}

	private void setUpSpringAuthentication(Claims claims) {
		List<String> authorities = (List<String>) claims.get("authorities");
		UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
				authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
}
