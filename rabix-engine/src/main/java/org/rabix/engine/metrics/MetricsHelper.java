package org.rabix.engine.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

import java.util.function.Supplier;

public interface MetricsHelper {

    Timer timer(String prefix, String name);

    Timer timer(String name);

    Counter counter(String prefix, String name);

    Counter counter(String name);

    Meter meter(String prefix, String name);

    Meter meter(String name);

    Histogram histogram(String prefix, String name);

    Histogram histogram(String name);

    void time(Runnable runnable, String name);

    void gauge(Supplier<Integer> supplier, String name);
}
