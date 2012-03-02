package com.stackmob.sdk.testobjects;

import com.stackmob.sdk.api.StackMob;
import com.stackmob.sdk.api.StackMobModel;
import com.stackmob.sdk.api.StackMobUser;

public class Book extends StackMobModel {

    private String title;
    private String publisher;
    private Author author;
    private StackMobUser owner;


    public Book(StackMob stackmob) {
        super(stackmob, Book.class);
    }
    public Book(String title, String publisher, Author author, StackMob stackmob) {
        this(stackmob);
        setTitle(title);
        setPublisher(publisher);
        setAuthor(author);
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublisher() {
        return publisher;
    }
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public StackMobUser getOwner() {
        return owner;
    }

    public void setOwner(StackMobUser owner) {
        this.owner = owner;
    }
}
