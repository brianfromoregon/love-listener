package com.lovingishard.lovelistener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 */
public class PoorManPubsub {
    public static class CanBeUpdated<T> {
        private List<Consumer<? super T>> subscribers = new ArrayList<>();

        public void update(T value) {
            subscribers.forEach(c -> c.accept(value));
        }

        public void forEach(Consumer<? super T> action) {
            subscribers.add(action);
        }

        public <R> CanBeUpdated<R> map(Function<? super T, ? extends R> mapper) {
            CanBeUpdated<R> ret = new CanBeUpdated<>();
            forEach(t -> ret.update(mapper.apply(t)));
            return ret;
        }

        public CanBeUpdated<T> mergeWith(CanBeUpdated<? extends T> other) {
            CanBeUpdated<T> ret = new CanBeUpdated<>();
            forEach(val -> ret.update(val));
            other.forEach(val -> ret.update(val));
            return ret;
        }

//    CanBeUpdated<T> filter(Predicate<? super T> predicate);
//    <R> CanBeUpdated<R> flatMap(Function<? super T, ? extends CanBeUpdated<? extends R>> mapper);
    }

    // Alternate style of reacting to inputs
    public static abstract class Node<T> {
        public Node(CanBeUpdated<T> upstream) {
            upstream.forEach(this::react);
        }

        protected abstract void react(T value);
    }

}
