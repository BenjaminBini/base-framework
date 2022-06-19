package io.bini.base.specifiction;

import lombok.Data;

@Data
public class SearchCriteria {
    private String key;
    private String operation;
    private Object value;

    public SearchCriteria(String key, String operation, Object value) {
        this.operation = operation;
        this.value = value;
        // Foreign key management (one level deep)
        if (key != null && key.contains("Id")) {
            String[] splitKey = key.split("Id");
            this.key = splitKey[0] + ".id";
        } else {
            this.key = key;
        }
    }
}
