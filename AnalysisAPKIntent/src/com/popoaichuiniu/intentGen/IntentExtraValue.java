package com.popoaichuiniu.intentGen;

import java.util.Objects;

public class IntentExtraValue {
    String key;
    String type;
    String value;

    public IntentExtraValue(String key, String type, String value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }

    public IntentExtraValue(IntentExtraKey intentExtraKey) {
        this.key = intentExtraKey.key;
        this.type = intentExtraKey.type;
        this.value = intentExtraKey.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntentExtraValue that = (IntentExtraValue) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(type, that.type) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(key, type, value);
    }


}