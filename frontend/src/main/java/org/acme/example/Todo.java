package org.acme.example;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Todo {

    public String id;

    public String title;

    public boolean completed;

    public long created;

    @JsonIgnore
    public String user;
}
