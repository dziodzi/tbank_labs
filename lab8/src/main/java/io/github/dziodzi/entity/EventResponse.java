package io.github.dziodzi.entity;

import lombok.Data;

import java.util.List;

@Data
public class EventResponse {
    private int count;
    private String next;
    private String previous;
    private List<Event> results;
}
