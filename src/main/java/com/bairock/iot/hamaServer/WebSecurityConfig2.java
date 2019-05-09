package com.bairock.iot.hamaServer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.bairock.iot.hamaServer.service.MyCustomUserService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig2 extends WebSecurityConfigurerAdapter{

	@Autowired
	private MyCustomUserService myCustomUserService;

//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http.authorizeRequests().antMatchers("/login", "/user/page/register", "/user/register", "/css/**", "/img/**", "/js/**", "/webjars/**").permitAll().anyRequest().authenticated().and()
//				.formLogin().loginPage("/login").defaultSuccessUrl("/loginSuccess", true).permitAll().and().logout()
//				.permitAll();
//	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/user/register/**", "/group/client/**", "/deviceImg/**", "/download/**", "/deviceMsg/**", "/hamaServer-websocket/**", "/css/**", "/img/**", "/js/**", "/webjars/**", "/devImg/**").permitAll()
			.antMatchers(HttpMethod.POST, "/group/client/**").permitAll()
			.anyRequest().authenticated().and()
				.formLogin().loginPage("/login").defaultSuccessUrl("/loginSuccess", true).permitAll().and().logout()
				.permitAll()
				.and().rememberMe().tokenValiditySeconds(2419200).key("hamaKey")
				.and().csrf().disable();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(myCustomUserService); 
	}
	
//	@Override
//	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth.inMemoryAuthentication().passwordEncoder(passwordEncoder()).withUser("admin")
//		.password(passwordEncoder().encode("a123")).roles("USER", "ADMIN");
//	}

	// 忽略静态资源的拦截
	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/static/**");
	}
}
