package com.appdynamics.extensions.conf.modules;

import com.appdynamics.extensions.api.ApiException;
import com.appdynamics.extensions.api.ControllerApiService;
import com.appdynamics.extensions.conf.controller.ControllerInfo;
import com.appdynamics.extensions.conf.controller.ControllerInfoValidator;
import com.appdynamics.extensions.dashboard.CustomDashboardTemplateGenerator;
import com.appdynamics.extensions.dashboard.CustomDashboardUploader;
import com.appdynamics.extensions.dashboard.CustomDashboardUtils;
import com.appdynamics.extensions.dashboard.DashboardConstants;
import com.appdynamics.extensions.http.Http4ClientBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.google.common.base.Strings;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CustomDashboardModule {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(CustomDashboardModule.class);
    private static final int DEFAULT_PERIODIC_DASHBOARD_CHECK_IN_SECONDS= 300;

    private boolean initialized;
    private String dashboardName;
    private boolean overwrite;
    private String dashboardTemplate;
    private CustomDashboardUploader dashboardUploader;
    private long timeDelayInMilliSeconds;
    private CloseableHttpClient client;
    private volatile AtomicLong lastRecordedTime = new AtomicLong();


    public void initCustomDashboard(Map<String, ?> config, String metricPrefix, String monitorName,
                                    ControllerInfo controllerInfo) {
        initialized = false;
        Map customDashboardConfig = (Map) config.get("customDashboard");
        if (isCustomDashboardEnabled(customDashboardConfig)) {
            String dashboardTemplate1 = getDashboardTemplate(metricPrefix, customDashboardConfig,controllerInfo);
            if(isValidDashboardTemplate(dashboardTemplate1)){
                dashboardTemplate = dashboardTemplate1;
                dashboardName = CustomDashboardUtils.getDashboardName(customDashboardConfig, monitorName);
                overwrite = CustomDashboardUtils.getOverwrite(customDashboardConfig);
                timeDelayInMilliSeconds = getTimeDelay(customDashboardConfig) * 1000;
                dashboardUploader = new CustomDashboardUploader(new ControllerApiService(controllerInfo));
                client = Http4ClientBuilder.getBuilder(CustomDashboardUtils.getHttpProperties(controllerInfo, config)).build();
                initialized = true;
            }
        } else {
            logger.info("Custom Dashboard is not initialized in config.yml.");
        }
    }

    public void uploadDashboard(){
        if(initialized){
            long currentTime = System.currentTimeMillis();
            if(hasTimeElapsed(currentTime,lastRecordedTime.get(),timeDelayInMilliSeconds)){
                try{
                    logger.debug("Attempting to upload dashboard: {}", dashboardName);
                    dashboardUploader.checkAndUpload(client, dashboardName, dashboardTemplate,
                            httpProperties, overwrite);
                    lastRecordedTime.set(currentTime);
                    long endTime = System.currentTimeMillis();
                    logger.debug("Time to complete customDashboardModule  :" + (endTime - currentTime) + " ms");
                } catch (ApiException e) {
                    logger.error("Unable to establish connection, not uploading dashboard.", e);
                } catch (IOException e) {
                    logger.error("Error while uploading dashboard", e);
                }
            }
        }
        else {
            logger.debug("Auto upload of custom dashboard is disabled.");
        }
    }

    private boolean hasTimeElapsed(long curr,long prev, long threshold){
        return (curr - prev > threshold) ? true : false;
    }

    private boolean isCustomDashboardEnabled(Map customDashboardConfig) {
        return customDashboardConfig != null && !customDashboardConfig.isEmpty() && (Boolean)customDashboardConfig.get(DashboardConstants.ENABLED);
    }

    private int getTimeDelay(Map customDashboardConfig) {
        Integer num = (Integer) customDashboardConfig.get("periodicDashboardCheckInSeconds");
        if (num != null) {
            return num;
        }
        return DEFAULT_PERIODIC_DASHBOARD_CHECK_IN_SECONDS;
    }

    private String getDashboardTemplate(String metricPrefix, Map customDashboardConfig, ControllerInfo controllerInfo) {
        ControllerInfoValidator validator = new ControllerInfoValidator();
        if (validator.isValidatedAndResolved(controllerInfo)) {
            String dashboardMetricPrefix = CustomDashboardUtils.buildMetricPrefixForDashboard(metricPrefix);
            CustomDashboardTemplateGenerator templateGenerator = new CustomDashboardTemplateGenerator(customDashboardConfig, controllerInfo,
                    dashboardMetricPrefix, dashboardName);
            return templateGenerator.getDashboardTemplate();
        }
        return null;
    }

    private Boolean isValidDashboardTemplate(String dashboardTemplate) {
        if (!Strings.isNullOrEmpty(dashboardTemplate)) {
            logger.debug("Dashboard values resolved. Ready for uploader");
            return true;
        }
        logger.debug("Dashboard is not initialized, skipping upload.");
        return false;
    }

}