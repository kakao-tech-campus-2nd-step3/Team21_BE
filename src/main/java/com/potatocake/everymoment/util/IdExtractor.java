package com.potatocake.everymoment.util;

@FunctionalInterface
public interface IdExtractor<T> {

    Long extractId(T item);

}
