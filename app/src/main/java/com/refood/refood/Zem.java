package com.refood.refood;

public class Zem {
    public int price;
    private int image;
    private String status;
    private String url;
    public Zem(int price, int image, String url) {
        this.price = price;
        this.image = image;
        this.status = "Buy";
        this.url = url;
    }
    public int getPrice() {
        return price;
    }
    public void setPrice(String title) {
        this.price = price;
    }
    public int getImage() {
        return image;
    }
    public void setImage(int image) {
        this.image = image;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus(){
        return status;
    }
    public String getUrl(){return url;}
}
