package com.assabetsecurity.core;

/**
 * Created by george on 1/3/16.
 */
public class Products {
   //
      private String categoryId;
      private String productId;
      private String name;
      private String descContent;
      private String HTMLContent;

    public Products(String categoryId, String productId) {
        this.categoryId = categoryId;
        this.productId = productId;
    }

    public String getCategoryId() {
        return categoryId;
    }


    public String getProductId() {
        return productId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescContent() {
        return descContent;
    }

    public void setDescContent(String descContent) {
        this.descContent = descContent;
    }

    public String getHTMLContent() {
        return HTMLContent;
    }

    public void setHTMLContent(String HTMLContent) {
        this.HTMLContent = HTMLContent;
    }
}


