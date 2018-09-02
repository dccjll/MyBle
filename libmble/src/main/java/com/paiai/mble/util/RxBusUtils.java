package com.paiai.mble.util;

import android.support.annotation.IntDef;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@SuppressWarnings("all")
public class RxBusUtils {

    private static final String TAG = RxBusUtils.class.getSimpleName();

    public static final int MAIN            = 0x01;
    public static final int IO              = 0x02;
    public static final int NEWTHREAD       = 0x03;
    public static final int COMPUTATION     = 0x04;
    public static final int TRAMPOLINE      = 0x05;
    public static final int DEFAULT         = 0x06;

    @IntDef({MAIN, IO, NEWTHREAD, COMPUTATION, TRAMPOLINE, DEFAULT})
    private @interface ThreadMode {}

    private final Subject<Object> subject = PublishSubject.create().toSerialized();
    private Map<Object, List<Disposable>> disposableMap = new HashMap<>();
    private int subscribeCount = 0; // 订阅的数量

    private RxBusUtils() {}
    private static class RxBusHolder {
        private static final RxBusUtils INSTANCE = new RxBusUtils();
    }
    private static RxBusUtils get() {
        return RxBusHolder.INSTANCE;
    }

    private static class Event {
        private Object event;
        private Object subscriber;
        private String target;

        private Event(Object event, Object subscriber, String target) {
            this.event = event;
            this.subscriber = subscriber;
            this.target = target;
        }

        @Override
        public String toString() {
            return "Event{" +
                "event=" + event +
                ", subscriber=" + subscriber +
                ", target='" + target + '\'' +
                '}';
        }
    }

    public interface EventHandle<T> {
        void accept(T t);
    }

    private <T> Observable<T> toObservable(Class<T> eventType) {
        return subject.ofType(eventType);
    }

    private <T> void subscribe(final Object subscriber, final Class<T> type, @ThreadMode final int threadMode,
                               final String target, final EventHandle<T> eventHandle) {
        if (subscriber == null) {
            throw new IllegalArgumentException("param subscriber cannot empty");
        }

        Observable<T> observable = toObservable(Event.class)
            .observeOn(AndroidSchedulers.mainThread())
            .onErrorReturn(new Function<Throwable, Event>() {
                @Override
                public Event apply(Throwable throwable) throws Exception {
                    throwable.printStackTrace();
                    return null;
                }
            })
            .subscribeOn(Schedulers.io())
            .filter(new Predicate<Event>() {
                @Override
                public boolean test(Event event) throws Exception {
                    return type.isInstance(event.event);
                }
            })
            .filter(new Predicate<Event>() {
                @Override
                public boolean test(Event event) throws Exception {
                    return event.subscriber == null || subscriber == event.subscriber;
                }
            })
            .filter(new Predicate<Event>() {
                @Override
                public boolean test(Event event) throws Exception {
                    return event.target == null || target == null || target.equals(event.target);
                }
            })
            .map(new Function<Event, T>() {
                @Override
                public T apply(Event event) throws Exception {
                    return (T) event.event;
                }
            });

        switch (threadMode) {
            case MAIN:
                observable = observable.observeOn(AndroidSchedulers.mainThread());
                break;
            case IO:
                observable = observable.observeOn(Schedulers.io());
                break;
            case NEWTHREAD:
                observable = observable.observeOn(Schedulers.newThread());
                break;
            case COMPUTATION:
                observable = observable.observeOn(Schedulers.computation());
                break;
            case TRAMPOLINE:
                observable = observable.observeOn(Schedulers.trampoline());
                break;
            case DEFAULT:
                break;
        }

        Consumer<T> consumer = new Consumer<T>() {
            @Override
            public void accept(T t) throws Exception {
                if (eventHandle != null) {
                    eventHandle.accept(t);
                }
            }
        };

        List<Disposable> disposableList = new ArrayList<>();
        if (disposableMap.containsKey(subscriber)) {
            disposableList = disposableMap.get(subscriber);
        }
        disposableList.add(observable.subscribe(consumer));
        synchronized (this) {
            disposableMap.put(subscriber, disposableList);
        }
        subscribeCount ++;
        Log.d(TAG, "接受一个订阅，subscriber = " + subscriber + ", Count = " + subscribeCount);
    }

    /**
     * 订阅消息
     * @param subscriber 订阅者
     * @param type 接受的消息类型
     * @param eventHandle 消息处理函数
     */
    public static <T> void subscribe(Object subscriber, Class<T> type, EventHandle<T> eventHandle) {
        get().subscribe(subscriber, type, MAIN, null, eventHandle);
    }

    /**
     * 订阅消息
     * @param subscriber 订阅者
     * @param type 消息类型
     * @param eventHandle 消息处理函数
     * @param target 消息发布的类型
     */
    public static <T> void subscribe(Object subscriber, Class<T> type, EventHandle<T> eventHandle, String target) {
        get().subscribe(subscriber, type, MAIN, target, eventHandle);
    }

    /**
     * 取消某个订阅者下的所有订阅
     * @param subscriber 订阅者
     */
    public synchronized static void unSubscribe(Object subscriber) {
        if (get().disposableMap.containsKey(subscriber)) {
            for (Disposable disposable : get().disposableMap.get(subscriber)) {
                if (disposable != null && !disposable.isDisposed()) {
                    disposable.dispose();
                }
            }
            get().subscribeCount -= get().disposableMap.get(subscriber).size();
            get().disposableMap.remove(subscriber);
            Log.d(TAG, "取消一组订阅， subscriber is " + subscriber + ", Count = " + get().subscribeCount);
        }
    }

    /**
     * 发布消息
     * @param event 需要发布的消息
     */
    public static void post(Object event) {
        get().subject.onNext(new Event(event, null, null));
    }

    /**
     * 发布消息
     * @param event 需要发布的消息
     * @param target 消息发布的类型
     */
    public static void post(Object event, String target) {
        get().subject.onNext(new Event(event, null, target));
    }

    /**
     * 发布消息
     * @param event 需要发布的消息
     * @param subscriber 订阅者
     */
    public static void post(Object event, Object subscriber) {
        get().subject.onNext(new Event(event, subscriber, null));
    }

    /**
     * 发布消息
     * @param event 需要发布的消息
     * @param subscriber 订阅者
     * @param target 消息发布的类型
     */
    public static void post(Object event, Object subscriber, String target) {
        get().subject.onNext(new Event(event, subscriber, target));
    }


    /**
     * 延迟发布消息
     */
    public static void postDelayed(final Object event, long delayMillis) {
        Observable.timer(delayMillis, TimeUnit.MILLISECONDS)
            .subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) {
                    post(event);
                }
            });
    }

    public static void postDelayed(final Object event, final String target, long delayMillis) {
        Observable.timer(delayMillis, TimeUnit.MILLISECONDS)
            .subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) {
                    post(event, target);
                }
            });
    }

    public static void postDelayed(final Object event, final Object subscriber, long delayMillis) {
        Observable.timer(delayMillis, TimeUnit.MILLISECONDS)
            .subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) {
                    post(event, subscriber);
                }
            });
    }

    public static void postDelayed(final Object event, final Object subscriber, final String target, long delayMillis) {
        Observable.timer(delayMillis, TimeUnit.MILLISECONDS)
            .subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) {
                    post(event, subscriber, target);
                }
            });
    }


    public interface Runnable {
        void run();
    }

    public static void runDelayed(long delayMillis, final Runnable runnable) {
        Observable.timer(delayMillis, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Long>() {
                @Override
                public void accept(Long aLong) {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
    }

    public static void runOnMainThread(final Runnable runnable) {
        Observable.just(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Integer>() {
                @Override
                public void accept(Integer integer) {
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            });
    }

}
