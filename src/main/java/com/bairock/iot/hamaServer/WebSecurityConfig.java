package com.bairock.iot.hamaServer;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private DataSource dataSource;

//	@Override
//	protected void configure(HttpSecurity http) throws Exception {
//		http.authorizeRequests().antMatchers("/login", "/user/page/register", "/user/register", "/css/**", "/img/**", "/js/**", "/webjars/**").permitAll().anyRequest().authenticated().and()
//				.formLogin().loginPage("/login").defaultSuccessUrl("/loginSuccess", true).permitAll().and().logout()
//				.permitAll();
//	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/user/page/register", "/user/register", "/css/**", "/img/**", "/js/**", "/webjars/**").permitAll().anyRequest().authenticated().and()
				.formLogin().loginPage("/login").defaultSuccessUrl("/loginSuccess", true).permitAll().and().logout()
				.permitAll();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//		auth.jdbcAuthentication().dataSource(dataSource)
//				.usersByUsernameQuery("select name, psd from User where name=?")
//				.passwordEncoder(passwordEncoder());
		auth.jdbcAuthentication().dataSource(dataSource)
				.usersByUsernameQuery("select name, psd, true from User where name=?")
				.authoritiesByUsernameQuery("select name, 'ROLE_USER' from User where name=?")
				.passwordEncoder(passwordEncoder());
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
