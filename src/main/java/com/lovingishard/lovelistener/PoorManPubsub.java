package com.lovingishard.lovelistener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 */
public class PoorManPubsub {

    public interface Stream<T> {
        void forEach(Consumer<? super T> action);

        <R> Stream<R> map(Function<? super T, ? extends R> mapper);

        Stream<T> mergeWith(Stream<? extends T> other);
    }

    public static class CanBeUpdated<T> implements Stream<T> {
        private List<Consumer<? super T>> subscribers = new ArrayList<>();

        public void update(T value) {
            subscribers.forEach(c -> c.accept(value));
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            subscribers.add(action);
        }

        @Override
        public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
            CanBeUpdated<R> ret = new CanBeUpdated<>();
            forEach(t -> ret.update(mapper.apply(t)));
            return ret;
        }

        @Override
        public Stream<T> mergeWith(Stream<? extends T> other) {
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
