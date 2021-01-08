package spullara.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.StreamSupport.stream;

public class Limiter {

    public static <T> Stream<T> limit(Stream<T> s, Predicate<T> limit) {
        Iterator<T> iterator = s.sequential().iterator();
        Iterator<T> limitedIterator = new Iterator<T>() {
            T next = null;

            @Override
            public boolean hasNext() {
                if (next == null && iterator.hasNext()) {
                    next = iterator.next();
                    if (next == null) throw new NullPointerException();
                }
                return next != null && limit.test(next);
            }

            @Override
            public T next() {
                if (next != null || hasNext()) {
                    T result = next;
                    next = null;
                    return result;
                }
                throw new NoSuchElementException();
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(limitedIterator, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    public static <T> Stream<T> substream(Stream<T> s, Predicate<T> skip) {
        Iterator<T> iterator = s.sequential().iterator();
        Iterator<T> substreamIterator = new Iterator<T>() {
            T next = null;

            @Override
            public boolean hasNext() {
                if (next == null && iterator.hasNext()) {
                    do {
                        if (iterator.hasNext()) {
                            next = iterator.next();
                            if (next == null) throw new NullPointerException();
                        } else {
                            return false;
                        }
                    } while (skip.test(next));
                }
                return next != null;
            }

            @Override
            public T next() {
                if (next != null || hasNext()) {
                    T result = next;
                    next = null;
                    return result;
                }
                throw new NoSuchElementException();
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(substreamIterator, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }

    public static <T> Stream<T> substream(Stream<T> s, Predicate<T> skip, Predicate<T> limit) {
        Iterator<T> iterator = s.sequential().iterator();
        Iterator<T> substreamIterator = new Iterator<T>() {
            boolean skipped = false;
            T next = null;

            @Override
            public boolean hasNext() {
                if (!skipped && next == null && iterator.hasNext()) {
                    do {
                        if (iterator.hasNext()) {
                            next = iterator.next();
                            if (next == null) throw new NullPointerException();
                        } else {
                            return false;
                        }
                    } while (skip.test(next));
                    skipped = true;
                }
                if (next == null && iterator.hasNext()) {
                    next = iterator.next();
                    if (next == null) throw new NullPointerException();
                }
                return next != null && limit.test(next);
            }

            @Override
            public T next() {
                if (next != null || hasNext()) {
                    T result = next;
                    next = null;
                    return result;
                }
                throw new NoSuchElementException();
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(substreamIterator, Spliterator.ORDERED | Spliterator.NONNULL), false);
    }
}
