package edu.hawaii.its.filedrop.type;

public interface OfficeIdentifiable {
    default Integer getOfficeId() {
        return 0;
    }
}
