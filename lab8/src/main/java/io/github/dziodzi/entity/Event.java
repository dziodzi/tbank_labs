package io.github.dziodzi.entity;

import lombok.Data;

@Data
public class Event {
    private int id;
    private String title;
    private boolean isFree;
    private String price;
    private Double parsedPrice;
}