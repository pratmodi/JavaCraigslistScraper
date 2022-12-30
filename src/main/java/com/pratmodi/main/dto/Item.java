package com.pratmodi.main.dto;

import java.math.BigDecimal;

public class Item {
    private String title;
    private String description;
    private BigDecimal price;
    private String url;
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
}
