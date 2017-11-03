//   Copyright 2012,2013 Vaughn Vernon
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package org.eyeseetea.malariacare.domain.subscriber;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DomainEventPublisher {
    private static final DomainEventPublisher instance = new DomainEventPublisher();

    private List subscribers = new ArrayList();

    public static DomainEventPublisher instance() {
        return instance;
    }

    public <T> void publish(final T aDomainEvent) {
        if (subscribers.size() > 0) {
            Class<?> eventType = aDomainEvent.getClass();

            List<DomainEventSubscriber<T>> allSubscribers = this.subscribers;

            for (DomainEventSubscriber<T> subscriber : allSubscribers) {
                Class<?> subscribedToType = subscriber.subscribedToEventType();

                if (eventType == subscribedToType) {
                    subscriber.handleEvent(aDomainEvent);
                }
            }
        }
    }

    public <T> void subscribe(DomainEventSubscriber<T> subscriber) {
        this.subscribers.add(subscriber);
    }

    public <T> void unSubscribe(DomainEventSubscriber<T> subscriber) {
        subscribers.remove(subscriber);
    }
}