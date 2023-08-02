package org.noear.solon.cloud.metrics.integration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;

import org.noear.solon.Solon;
import org.noear.solon.cloud.CloudManager;
import org.noear.solon.cloud.metrics.annotation.MeterGauge;
import org.noear.solon.cloud.metrics.annotation.MeterSummary;
import org.noear.solon.cloud.metrics.annotation.MeterCounter;
import org.noear.solon.cloud.metrics.annotation.MeterTimer;
import org.noear.solon.cloud.metrics.Interceptor.MeterGaugeInterceptor;
import org.noear.solon.cloud.metrics.Interceptor.MeterSummaryInterceptor;
import org.noear.solon.cloud.metrics.Interceptor.MeterCounterInterceptor;
import org.noear.solon.cloud.metrics.Interceptor.MeterTimerInterceptor;

import org.noear.solon.core.AopContext;
import org.noear.solon.core.Plugin;


/**
 * @author bai
 * @since 2.4
 */
public class XPluginImpl implements Plugin {
    @Override
    public void start(AopContext context) {
        context.beanInterceptorAdd(MeterCounter.class, new MeterCounterInterceptor());
        context.beanInterceptorAdd(MeterGauge.class, new MeterGaugeInterceptor());
        context.beanInterceptorAdd(MeterSummary.class, new MeterSummaryInterceptor());
        context.beanInterceptorAdd(MeterTimer.class, new MeterTimerInterceptor());

        context.wrapAndPut(MeterRegistry.class, Metrics.globalRegistry);

        context.subBeansOfType(MeterRegistry.class, bean -> {
            if (bean != Metrics.globalRegistry) {
                Metrics.addRegistry(bean);
            }
        });

        Metrics.globalRegistry.config()
                .commonTags(
                        "solon.app.name", Solon.cfg().appName(),
                        "solon.app.group", Solon.cfg().appGroup(),
                        "solon.app.nameSpace", Solon.cfg().appNamespace());

        CloudManager.register(new CloudMetricServiceImpl());
    }
}
