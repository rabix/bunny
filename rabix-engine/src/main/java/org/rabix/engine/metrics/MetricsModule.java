package org.rabix.engine.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.commons.configuration.Configuration;
import org.rabix.engine.metrics.impl.MetricsHelperImpl;

import java.util.concurrent.TimeUnit;

public class MetricsModule extends AbstractModule {

    public static final String METRICS_KEY_PREFIX = "metrics.";
    public static final String METRICS_ENABLE_KEY = METRICS_KEY_PREFIX + "enable";
    public static final String METRICS_REPORTER_KEY = METRICS_KEY_PREFIX + "reporter";
    public static final String METRICS_REPORT_PERIOD_KEY = METRICS_KEY_PREFIX + "reporter.period";
    public static final String METRICS_REPORT_PERIOD_UNIT_KEY = METRICS_REPORT_PERIOD_KEY + "unit";

    private final Configuration configuration;

    public MetricsModule(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {}

    @Provides
    @Singleton
    public MetricsHelper getMetricsHelper() {
        MetricRegistry metricRegistry = new MetricRegistry();
        resolveAndStartReporter(metricRegistry);

        return new MetricsHelperImpl(metricRegistry);
    }

    private void resolveAndStartReporter(MetricRegistry metricsRegistry) {
        boolean metricsEnabled = configuration.getBoolean(METRICS_ENABLE_KEY, false);
        if (!metricsEnabled) {
            return;
        }

        String reportingType = configuration.getString(METRICS_REPORTER_KEY, "jmx").toLowerCase();
        switch (reportingType) {
            case "jmx":
                JmxReporter.forRegistry(metricsRegistry).build().start();
                break;
            case "console":
                int period = configuration.getInt(METRICS_REPORT_PERIOD_KEY, 15);
                TimeUnit unit = TimeUnit.valueOf(configuration.getString(METRICS_REPORT_PERIOD_UNIT_KEY, "seconds").toUpperCase());

                ConsoleReporter.forRegistry(metricsRegistry).build().start(period, unit);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported metrics reporter specified: %s. Use jmx or console.", reportingType));
        }
    }
}
