package com.pratmodi.main.dto;

import lombok.NonNull;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;


public class Item {

    private AtomicInteger i = new AtomicInteger();
    @NonNull
    private String title;
    @NonNull
    private String description;
    @NonNull
    private BigDecimal price;
    @NonNull
    private String url;
    @NonNull
    private String hideThisPosting;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHideThisPosting() {
        return hideThisPosting;
    }

    public void setHideThisPosting(String hideThisPosting) {
        this.hideThisPosting = hideThisPosting;
    }

    public Item() {
    }

    public AtomicInteger getI() {
        return i;
    }

    public void setI(AtomicInteger i) {
        this.i = i;
    }

    public Item(String title, String description, BigDecimal price, String url, String hideThisPosting) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.url = url;
        this.hideThisPosting = hideThisPosting;
    }

    @Override
    public String toString() {
        return "Item{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", url='" + url + '\'' +
                ", hideThisPosting='" + hideThisPosting + '\'' +
                '}';
    }
}
