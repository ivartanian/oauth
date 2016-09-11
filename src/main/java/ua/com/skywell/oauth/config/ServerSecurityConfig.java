package ua.com.skywell.oauth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.RequestContextFilter;
import ua.com.skywell.oauth.custom.UserInfoTokenServices;

import javax.servlet.Filter;
import java.util.Arrays;

/**
 * Created by viv on 02.09.2016.
 */
@Configuration
@EnableWebSecurity(debug = true)
@EnableOAuth2Client
@Order(6)
public class ServerSecurityConfig extends WebSecurityConfigurerAdapter {

    private final OAuth2ClientContext oauth2ClientContext;
    private final OAuth2ClientContextFilter oAuth2ClientContextFilter;

    @Autowired
    public ServerSecurityConfig(OAuth2ClientContext oauth2ClientContext, OAuth2ClientContextFilter oAuth2ClientContextFilter) {
        this.oauth2ClientContext = oauth2ClientContext;
        this.oAuth2ClientContextFilter = oAuth2ClientContextFilter;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.parentAuthenticationManager(authenticationManagerBean());/*inMemoryAuthentication()
                .withUser("user").password("user").roles("USER");*/
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**")
                .authorizeRequests().antMatchers("/", "/login**", "/webjars/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/"))
                .and()
                .logout().logoutSuccessUrl("/").permitAll()
                .and()
                .csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .addFilterBefore(ssoGitHubFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(ssoFacebookFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(ssoGoogleFilter(), BasicAuthenticationFilter.class)
                .addFilterBefore(requestContextFilter(), OAuth2ClientAuthenticationProcessingFilter.class)
                .addFilterBefore(oAuth2ClientContextFilter, LogoutFilter.class);
    }

    private Filter ssoFacebookFilter() {
        OAuth2ClientAuthenticationProcessingFilter facebookFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/facebook");
        facebookFilter.setRestTemplate(restTemplateFacebook());
        facebookFilter.setTokenServices(new UserInfoTokenServices("https://graph.facebook.com/me?fields=name,email,first_name,last_name", facebook().getClientId()));
        return facebookFilter;
    }

    private Filter ssoGitHubFilter() {
        OAuth2ClientAuthenticationProcessingFilter githubFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/github");
        githubFilter.setRestTemplate(restTemplateGitHub());
        githubFilter.setTokenServices(new UserInfoTokenServices("https://api.github.com/user", github().getClientId()));
        return githubFilter;
    }

    private Filter ssoGoogleFilter() {
        OAuth2ClientAuthenticationProcessingFilter githubFilter = new OAuth2ClientAuthenticationProcessingFilter("/login/google");
        githubFilter.setRestTemplate(restTemplateGoogle());
        githubFilter.setTokenServices(new UserInfoTokenServices("https://www.googleapis.com/oauth2/v1/userinfo", github().getClientId()));
        return githubFilter;
    }

    @Bean
    public OAuth2RestTemplate restTemplateFacebook() {
        return new OAuth2RestTemplate(facebook(), oauth2ClientContext);
    }

    @Bean
    public OAuth2RestTemplate restTemplateGitHub() {
        return new OAuth2RestTemplate(github(), oauth2ClientContext);
    }

    @Bean
    public OAuth2RestTemplate restTemplateGoogle() {
        return new OAuth2RestTemplate(google(), oauth2ClientContext);
    }

    @Bean
    public RequestContextFilter requestContextFilter() {
        return new RequestContextFilter();
    }

    @Bean
    public AuthorizationCodeResourceDetails facebook() {
        AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        details.setClientId("696436577172062");
        details.setClientSecret("700900186591bc61cb595b57ea86aefb");
        details.setAccessTokenUri("https://graph.facebook.com/oauth/access_token");
        details.setUserAuthorizationUri("https://www.facebook.com/dialog/oauth");
        details.setTokenName("oauth_token");
        details.setAuthenticationScheme(AuthenticationScheme.query);
        details.setClientAuthenticationScheme(AuthenticationScheme.form);
        details.setUseCurrentUri(true);
        details.setScope(Arrays.asList("email", "public_profile"));
        return details;
    }

    @Bean
    public AuthorizationCodeResourceDetails github() {
        AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        details.setClientId("0a9043c4a9d5e43bf6c1");
        details.setClientSecret("ff28221946d2ca7abfac7dd5cef5565461ee6775");
        details.setAccessTokenUri("https://github.com/login/oauth/access_token");
        details.setUserAuthorizationUri("https://github.com/login/oauth/authorize");
        details.setTokenName("oauth_token");
        details.setAuthenticationScheme(AuthenticationScheme.query);
        details.setClientAuthenticationScheme(AuthenticationScheme.form);
        details.setUseCurrentUri(true);
        return details;
    }

    @Bean
    public AuthorizationCodeResourceDetails google() {
        AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
        details.setClientId("946905659608-oshjnltjokbmgrhjgla3t56l07jbkt6d.apps.googleusercontent.com");
        details.setClientSecret("XvH2xojEDhYd2jmEF0YbYnOg");
        details.setAccessTokenUri("https://accounts.google.com/o/oauth2/token");
        details.setUserAuthorizationUri("https://accounts.google.com/o/oauth2/auth");
        details.setTokenName("oauth_token");
        details.setAuthenticationScheme(AuthenticationScheme.query);
        details.setClientAuthenticationScheme(AuthenticationScheme.form);
        details.setPreEstablishedRedirectUri("http://localhost:8080");
        details.setScope(Arrays.asList("https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/userinfo.profile"));
        return details;
    }
}