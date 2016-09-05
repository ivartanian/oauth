package ua.com.skywell.oauth.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

/**
 * Created by viv on 07.07.2016.
 */
@Component
@DependsOn(value = "dataSourceInitializer")
public class PopulateBean implements InitializingBean {

    @Autowired
    private DataSource dataSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        ClientRegistrationService clientRegistrationService = new JdbcClientDetailsService(dataSource);

        BaseClientDetails clientDetails1 = new BaseClientDetails("sampleClientId", "", "read,write", "implicit", "");
        clientDetails1.setRefreshTokenValiditySeconds(60 * 60 * 12);
        clientRegistrationService.addClientDetails(clientDetails1);

        BaseClientDetails clientDetails2 = new BaseClientDetails("clientIdPassword", "", "read", "client_credentials,password,authorization_code,refresh_token", "ROLE_USER");
        clientDetails2.setClientSecret("secret");
        clientRegistrationService.addClientDetails(clientDetails2);
    }

}
