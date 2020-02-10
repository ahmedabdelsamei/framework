/*package com.cit.vc.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import com.cit.vc.security.CustomAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {

//	@Autowired
//	private AuthenticationEntryPoint authEntryPoint;

	@Autowired
    CustomAuthenticationProvider customAuthProvider;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		        http.csrf().disable()
		        .authorizeRequests()
		        .antMatchers("/**//*transactions/status*//**").authenticated()
		        .anyRequest().permitAll()
		        .and().httpBasic()
				.and().authenticationProvider(customAuthProvider)
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);*/
			//	.authenticationEntryPoint(authEntryPoint);
		
//		http.httpBasic().authenticationEntryPoint(authEntryPoint)
//        .and()
//        .authorizeRequests()
//      //  .antMatchers("/api/secured").authenticated()
//        .antMatchers("/**/transactions/status/**").hasRole("CLIENT")
//        .anyRequest().permitAll();
	//}

	
//	@Autowired
//	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//	//	auth.inMemoryAuthentication().withUser("interpay").password("password").roles("CLIENT");
////		auth.authenticationProvider(customAuthProvider);
//		auth.inMemoryAuthentication().withUser("externaluser").password("pass").roles("USER");
//	}
	
	/*@Autowired
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(this.customAuthProvider);
    }
	
}*/
