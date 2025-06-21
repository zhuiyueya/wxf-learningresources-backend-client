package chasemoon.top.wxflearningresourcesbackendclient.entity.enums;

public enum FileStatus {
    UNREVIEWED(0),
    APPROVED(1),
    REJECTED(2);

    private final int value;

    FileStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
} 