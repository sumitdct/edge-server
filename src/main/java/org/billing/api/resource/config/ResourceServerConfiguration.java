package org.billing.api.resource.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
@Configuration
@EnableResourceServer
/*@Import(PublicJWTConfig.class)*/
public class ResourceServerConfiguration extends  ResourceServerConfigurerAdapter 
{
	/*@Autowired
    TokenStore tokenStore;

    @Autowired
    JwtAccessTokenConverter tokenConverter;*/


    @Autowired
	private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.resourceId("nba-gateway")/*.tokenStore(tokenStore)*/;
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http
		.cors().and()
		.csrf().disable()
				.authorizeRequests()

				/** Permit all Request Calls **/
				.antMatchers("/api/core/*").permitAll()
				.antMatchers("/public/**").permitAll()

				/** Authenticated Request Calls **/
				.antMatchers("/api/secure/**").authenticated()
				.antMatchers("/api/core/secured/**").authenticated()

					.and()
        		.authorizeRequests()
					.anyRequest().authenticated()
				.and().httpBasic().authenticationEntryPoint(customAuthenticationEntryPoint);
	}
}
