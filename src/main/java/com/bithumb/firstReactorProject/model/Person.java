package com.bithumb.firstReactorProject.model;

public class Person {
    String name;
    String email;
    String password;

    public Person(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getUserName() {
        return this.name;
    }

    public void setUserName(String name) {
        this.name = name;
    }

}
