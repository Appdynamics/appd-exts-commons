# AppDynamics Extensions SDK CHANGELOG

## 2.1.1 - Oct 10, 2018
1. Removed displayName from AMonitorJob.java to avoid conflict with DashBoard changes

## 2.1.1 - Sep 7th, 2018
1. Removed getTaskCount()in ABaseMonitor.java and added getServers() to hold the list of servers from config.yml in ABaseMonitor.java and TaskExecutionServiceprovider.java.
2. In AMonitorJob.java, added flag for displayNameCheckEnabled and checkDisplayName().
3. In MetricWriteHelper.java, added a new metric "Metrics Uploaded".

## 2.1.0 - Apr 5, 2018
1. Extracted Configuration and Context from the MonitorConfiguration and dissolved it.
2. onConfigReload(File file) will now have access to configuration and context after the refactoring.
3. Bumped the version to 2.1.1

## 2.0.4 - Mar 13, 2018
1. Implemented mutual SSL Authentication

## 2.0.3.1 - Feb 27, 2018 (UNRELEASED --> MERGED with v2.0.4)
1. Added args(task arguments from monitor.xml) as an argument to the initializeMoreStuff() method.
2. Added Copyright header to all the files.
3. Removed jsr dependency from pom.xml.
4. Packaged LICENSE.txt and NOTICE.txt.
5. Made the names for passwordEncrypted and encryptionKey consistent.
6. Refactored dashboard module to be consistent with CloseableHttpClientUsage(except the uploadFile() method).
7. Fixed the bug caused by casting ThreadPoolExecutor to ScheduledThreadPoolExecutor.

## 2.0.3  - Feb 02, 2018 (UNRELEASED --> MERGED with v2.0.4)
1. Provided a method in ABaseMonitor that can be used to set FileWatchListener callback in the setConfigYml() method of MonitorConfiguration when it is called in the initialize() method of ABaseMonitor.
2. Added changes from the commons version 1.6.6.1, related to CSRF token for accessing controller APIs in the CustomDashboardUploader.

## 2.0.2 - Jan 23, 2018
1. Added a catch statement in the TaskExecutionServiceProvider to catch a throwable(Errors that occur while executing tasks) and print errors as a log statement.

## 2.0.1 - Nov 28, 2017
1. Fixed a bug for delta metrics when the metric value is null
2. Added more test cases.

## 2.0.0 - Nov 02, 2017
1. Modularized the MonitorConfiguration class.
2. Added DerivedMetricsCalculation feature.
3. Added Metric data structure and Transformers(alias, delta, multiplier, convert, rounding).
4. Added feature to make extension job runs independent.
5. Increased the unit test coverage.






