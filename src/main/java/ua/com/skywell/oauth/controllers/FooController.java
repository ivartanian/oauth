package ua.com.skywell.oauth.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.com.skywell.oauth.dto.FooDto;

import java.security.Principal;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

/**
 * Created by viv on 02.09.2016.
 */
@RestController
public class FooController {

//    @PreAuthorize("#oauth2.hasScope('read')")
    @RequestMapping(method = RequestMethod.GET, value = "/api/foos/{id}")
    public FooDto findById(@PathVariable long id) {
        return new FooDto(id, randomAlphabetic(4));
    }

//    @PreAuthorize("#oauth2.hasScope('write')")
    @RequestMapping(method = RequestMethod.POST, value = "/api/foos")
    public FooDto create(@RequestBody final FooDto foo) {
        foo.setId(Long.parseLong(randomNumeric(2)));
        return foo;
    }
}