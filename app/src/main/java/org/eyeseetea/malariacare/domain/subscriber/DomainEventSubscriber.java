package org.eyeseetea.malariacare.domain.subscriber;


public interface DomainEventSubscriber<T> {

    public void handleEvent(final T aDomainEvent);

    public Class<T> subscribedToEventType();
}
