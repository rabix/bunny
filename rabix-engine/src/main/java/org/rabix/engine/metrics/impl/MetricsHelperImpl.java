package org.rabix.engine.metrics.impl;

import com.codahale.metrics.*;
import com.google.inject.Inject;
import org.rabix.engine.metrics.MetricsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Supplier;

/**
 * A helper class for working with metrics.
 */
public class MetricsHelperImpl implements MetricsHelper {

    private static final Logger logger = LoggerFactory.getLogger(MetricsHelperImpl.class);

    private final MetricRegistry metricRegistry;
    private String defaultPrefix;

    @Inject
    public MetricsHelperImpl(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        try {
            defaultPrefix = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Could not determine hostname.");
            defaultPrefix = "unknown_host";
        }
    }

    @Override
    public Timer timer(String prefix, String name) {
        return metricRegistry.timer(getName(prefix, name));
    }

    @Override
    public Timer timer(String name) {
        return timer(defaultPrefix, name);
    }

    @Override
    public Counter counter(String prefix, String name) {
        return metricRegistry.counter(getName(prefix, name));
    }

    @Override
    public Counter counter(String name) {
        return counter(defaultPrefix, name);
    }

    @Override
    public Meter meter(String prefix, String name) {
        return metricRegistry.meter(getName(prefix, name));
    }

    @Override
    public Meter meter(String name) {
        return meter(defaultPrefix, name);
    }

    @Override
    public Histogram histogram(String prefix, String name) {
        return metricRegistry.histogram(getName(prefix, name));
    }

    @Override
    public Histogram histogram(String name) {
        return histogram(defaultPrefix, name);
    }

    @Override
    public void time(Runnable runnable, String name) {
        final Timer.Context context = timer(name).time();
        try {
            runnable.run();
        } finally {
            context.stop();
        }
    }

    @Override
    public void gauge(Supplier<Integer> supplier, String name) {
        metricRegistry.register(name, (Gauge<Integer>) supplier::get);
    }

    private String getName(String prefix, String name) {
        return prefix + "." + name;
    }
}