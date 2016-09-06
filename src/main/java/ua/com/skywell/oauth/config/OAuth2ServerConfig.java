package ua.com.skywell.oauth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.ApprovalStoreUserApprovalHandler;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

/**
 * Created by viv on 02.09.2016.
 */
@Configuration
public class OAuth2ServerConfig {

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

        private final TokenStore tokenStore;
        private final DataSource dataSource;
        private final AuthenticationManager authenticationManager;

        @Autowired
        public AuthorizationServerConfiguration(TokenStore tokenStore, DataSource dataSource,
                                                @Qualifier("authenticationManagerBean") AuthenticationManager authenticationManager) {
            this.tokenStore = tokenStore;
            this.dataSource = dataSource;
            this.authenticationManager = authenticationManager;
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            oauthServer
                    .tokenKeyAccess("permitAll()")
                    .checkTokenAccess("isAuthenticated()");
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.jdbc(dataSource);
//                    .withClient("sampleClientId")
//                    .authorizedGrantTypes("implicit")
//                    .scopes("read", "write")
//                    .accessTokenValiditySeconds(60 * 60)
//                    .refreshTokenValiditySeconds(60 * 60 * 12)
//                    .authorities("ROLE_USER")
//                    .and()
//                    .withClient("clientIdPassword")
//                    .secret("secret")
//                    .authorizedGrantTypes("client_credentials", "password", "authorization_code", "refresh_token")
//                    .scopes("read")
//                    .authorities("ROLE_USER");
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints
                    .authorizationCodeServices(authorizationCodeServices())
                    .approvalStore(approvalStore())
                    .tokenStore(tokenStore)
                    .accessTokenConverter(accessTokenConverter())
                    .reuseRefreshTokens(true)
                    .userApprovalHandler(userApprovalHandler())
                    .authenticationManager(authenticationManager)
                    .setClientDetailsService(clientDetailsService());
        }

        @Bean
        public ApprovalStore approvalStore() {
            return new JdbcApprovalStore(dataSource);
        }

        @Bean
        public AuthorizationCodeServices authorizationCodeServices() {
            return new JdbcAuthorizationCodeServices(dataSource);
        }

        @Bean
        public AccessTokenConverter accessTokenConverter() {
            return new DefaultAccessTokenConverter();
        }

        @Bean
        public ClientDetailsService clientDetailsService() {
            return new JdbcClientDetailsService(dataSource);
        }

        @Bean
        public OAuth2RequestFactory requestFactory() {
            return new DefaultOAuth2RequestFactory(clientDetailsService());
        }

        @Bean
        public ApprovalStoreUserApprovalHandler userApprovalHandler() {
            ApprovalStoreUserApprovalHandler approvalStoreUserApprovalHandler = new ApprovalStoreUserApprovalHandler();
            approvalStoreUserApprovalHandler.setClientDetailsService(clientDetailsService());
            approvalStoreUserApprovalHandler.setApprovalStore(approvalStore());
            approvalStoreUserApprovalHandler.setRequestFactory(requestFactory());
            approvalStoreUserApprovalHandler.setApprovalExpiryInSeconds(-1);
            return approvalStoreUserApprovalHandler;
        }

    }

//    @Configuration
//    @EnableResourceServer
//    protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
//
//        private final TokenStore tokenStore;
//
//        @Autowired
//        public ResourceServerConfiguration(TokenStore tokenStore) {
//            this.tokenStore = tokenStore;
//        }
//
//        @Override
//        public void configure(ResourceServerSecurityConfigurer resources) {
//            resources.tokenStore(tokenStore).tokenServices(tokenService());
//        }
//
//        @Override
//        public void configure(HttpSecurity http) throws Exception {
//            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).and().authorizeRequests()
//                    .anyRequest().authenticated();
//        }
//
//        @Primary
//        @Bean
//        public RemoteTokenServices tokenService() {
//            RemoteTokenServices tokenService = new RemoteTokenServices();
//            tokenService.setCheckTokenEndpointUrl("http://localhost:8080/oauth/oauth/check_token");
//            tokenService.setClientId("emb");
//            tokenService.setClientSecret("secret");
//            return tokenService;
//        }
//
//    }

    @Configuration
    @PropertySource({"classpath:persistence.properties"})
    protected static class Stuff {

        private final Resource schemaScript;
        private final Environment environment;

        @Autowired
        public Stuff(@Value("classpath:schema.sql") Resource schemaScript, Environment environment) {
            this.schemaScript = schemaScript;
            this.environment = environment;
        }

        @Bean
        public TokenStore tokenStore() {
            return new JdbcTokenStore(dataSource());
        }

        @Bean
        public DataSourceInitializer dataSourceInitializer() {
            DataSourceInitializer initializer = new DataSourceInitializer();
            initializer.setDataSource(dataSource());
            initializer.setDatabasePopulator(databasePopulator());
            return initializer;
        }

        private DatabasePopulator databasePopulator() {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(schemaScript);
            return populator;
        }

        @Bean
        public DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(environment.getProperty("jdbc.driverClassName"));
            dataSource.setUrl(environment.getProperty("jdbc.url"));
            dataSource.setUsername(environment.getProperty("jdbc.user"));
            dataSource.setPassword(environment.getProperty("jdbc.pass"));
            return dataSource;
        }

    }

}