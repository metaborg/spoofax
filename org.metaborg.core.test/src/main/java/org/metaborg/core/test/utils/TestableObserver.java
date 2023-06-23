package org.metaborg.core.test.utils;

import java.util.Iterator;
import java.util.Queue;

import com.google.common.collect.Queues;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Notification;
import io.reactivex.rxjava3.disposables.Disposable;

public class TestableObserver<T> implements ITestableObserver<T> {
    private Queue<TimestampedNotification<T>> notifications = Queues.newArrayDeque();


    @Override public void onComplete() {
        notifications
            .add(new TimestampedNotification<T>(Notification.<T>createOnComplete(), System.currentTimeMillis()));
    }

    @Override public void onError(Throwable e) {
        notifications.add(new TimestampedNotification<T>(Notification.<T>createOnError(e), System.currentTimeMillis()));
    }

    @Override public void onSubscribe(@NonNull Disposable d) {

    }

    @Override public void onNext(T t) {
        notifications.add(new TimestampedNotification<T>(Notification.createOnNext(t), System.currentTimeMillis()));
    }


    @Override public TimestampedNotification<T> peek() {
        return notifications.peek();
    }

    @Override public TimestampedNotification<T> poll() {
        return notifications.poll();
    }

    @Override public int size() {
        return notifications.size();
    }

    @Override public void clear() {
        notifications.clear();
    }


    @Override public Iterator<TimestampedNotification<T>> iterator() {
        return notifications.iterator();
    }
}
