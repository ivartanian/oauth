package ua.com.skywell.oauth.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * Created by super on 9/11/16.
 */
@RestController
public class Base {

    @RequestMapping("/user")
    public Principal auth(Principal principal) {
        return principal;
    }

    @RequestMapping("/api/me")
    public Principal resourse(Principal principal) {
        return principal;
    }

}
