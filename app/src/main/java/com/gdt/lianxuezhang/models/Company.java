package com.gdt.lianxuezhang.models;

/**
 * Created by LianxueZhang on 18/11/2015.
 */
public class Company {
    private String name;
    private int id;

    public Company() {
    }

    public Company(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
