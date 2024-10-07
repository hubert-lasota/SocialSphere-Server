package org.hl.socialspherebackend.application.common;

public interface Observer<T> {

    void update(T subject);

}
