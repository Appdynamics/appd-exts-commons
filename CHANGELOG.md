# AppDynamics Extensions SDK CHANGELOG

## 2.0.0 - Nov 02, 2017
1. Modularized the MonitorConfiguration class.
2. Added DerivedMetricsCalculation feature.
3. Added Metric data structure and Transformers(alias, delta, multiplier, convert, rounding).
4. Added feature to make extension job runs independent.
5. Increased the unit test coverage.

## 2.0.1 - Nov 28, 2017
1. Fixed a bug for delta metrics when the metric value is null
2. Added more test cases.

## 2.0.2 - Jan 23, 2018
1. Added a catch statement in the TaskExecutionServiceProvider to catch a throwable(Errors that occur while executing tasks) and print errors as a log statement.

## 2.0.3  - Feb 02, 2018
1. Provided a method in ABaseMonitor that can be used to set FileWatchListener callback in the setConfigYml() method of MonitorConfiguration when it is called in the initialize() method of ABaseMonitor.
2. Added changes from the commons version 1.6.6.1, related to CSRF token for accessing controller APIs in the CustomDashboardUploader.

## 2.0.3.1 - Feb 27, 2018
1. Added args(task arguments from monitor.xml) as an argument to the initializeMoreStuff() method.
2. Added Copyright header to all the files.
3. Removed jsr dependency from pom.xml
