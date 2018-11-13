package com.popoaichuiniu.intentGen;

import java.util.Objects;

public class IntentExtraKey {
    String key;
    String type;
    String value;

    public IntentExtraKey(IntentExtraValue intentExtraValue) {

        this.key = intentExtraValue.key;
        this.type = intentExtraValue.type;
        this.value = intentExtraValue.value;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntentExtraKey that = (IntentExtraKey) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key, type);
    }

    public IntentExtraKey(String key, String type, String value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" + "key='" + key + '\'' +
                ", type='" + type + '\'' +
                ", value='" + value + '\'' + "}";
    }

}
