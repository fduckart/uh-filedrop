package edu.hawaii.its.filedrop.type;

public interface PersonIdentifiable {
    default String getUhUuid() {
        return "";
    }
}
