package org.hl.socialspherebackend.application.pattern.behavioral;

public interface Observable<T> {

    void addObserver(Observer<T> observer);

    void removeObserver(Observer<T> observer);

    void notifyObservers(T subject);

}
