package chasemoon.top.wxflearningresourcesbackendclient.entity.enums;

public enum DeleteFlag {
    NORMAL(0),
    DELETED(1);

    private final int value;

    DeleteFlag(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
} 