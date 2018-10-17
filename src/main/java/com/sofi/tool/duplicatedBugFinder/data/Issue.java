package com.sofi.tool.duplicatedBugFinder.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Issue {
    private final String key;
    private final String description;
    private final String summary;
    private final List<String> components;

    public Issue(String key, String description) {
        this.key = key;
        this.description = description;
        this.summary = null;
        this.components = new ArrayList<>();
    }

    @Override
    public String toString() {
        return key;
    }
}
