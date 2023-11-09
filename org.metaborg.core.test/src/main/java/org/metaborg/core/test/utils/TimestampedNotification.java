package org.metaborg.core.test.utils;

import io.reactivex.rxjava3.core.Notification;

public class TimestampedNotification<T> {
    public final Notification<T> notification;
    public final long timestamp;

    public TimestampedNotification(Notification<T> notification, long timestamp) {
        this.notification = notification;
        this.timestamp = timestamp;
    }
}
