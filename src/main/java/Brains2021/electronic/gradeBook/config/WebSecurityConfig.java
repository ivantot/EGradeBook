package Brains2021.electronic.gradeBook.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${spring.security.secret-key}")
	private String securityKey;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()// u produkciji bismo ovo podesili
				.addFilterAfter(new JWTAuthorizationFilter(this.securityKey),
						UsernamePasswordAuthenticationFilter.class)
				.authorizeRequests().antMatchers(HttpMethod.POST, "/login").permitAll().anyRequest().authenticated();
		// loginu je dozvoljne pristup za sve korisnike, ostali samo autentifikovani
	}

}
