package ua.com.skywell.oauth.dto;

import java.io.Serializable;

/**
 * Created by viv on 02.09.2016.
 */
public class FooDto implements Serializable{
    private long id;
    private String name;

    public FooDto(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
