Java SDK for AppDynamics Monitoring Extensions
================================================

AppDynamics platform can be extended to support custom metrics by using [AppDynamics Monitoring Extensions](https://docs.appdynamics.com/display/LATEST/Extensions+and+Custom+Metrics). 
This Java SDK abstracts out common extension features and makes extension development very straightforward.

# Release Notes
Beginning 2.0.0, all the changes to the SDK are tracked in the [CHANGELOG.md](CHANGELOG.md).

# Getting Started

## Extension Prerequisites
Before developing a new extension, please check if an AppDynamics supported extension already exists on the [exchange](https://www.appdynamics.com/community/exchange/).
AppDynamics monitoring extensions extend the AppDynamics Server Infrastructure Agent (SIM) or Standalone Machine Agent functionality to report additional metrics to the AppDynamics Controller. 
Please go over the [Extension Prerequisites Guide](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) for more details on the supported configurations. 

## Importing the Dependency

### Maven
```
<dependency>
  <groupId>com.appdynamics</groupId>
  <artifactId>appd-exts-commons</artifactId>
  <version>2.2.0</version>
</dependency>
```


## Usage

### monitor.xml
Every monitoring extension should ship with a `monitor.xml`. The Machine Agent loads the 
extension jar during its start up and schedules the extension to run based on `execution-frequency-in-seconds` defined in the `monitor.xml`. 
A `monitor.xml` should also point to a `config.yml`. Below is a sample `monitor.xml` 

```
<monitor>
    <name>ExtensionStarter</name>
    <type>managed</type>
    <description>Starter template to gather data from an http source</description>
    <monitor-configuration>
    </monitor-configuration>
    <monitor-run-task>
        <execution-style>periodic</execution-style>
        <execution-frequency-in-seconds>60</execution-frequency-in-seconds>
        <name>ExtensionStarter Monitor Run Task</name>
        <display-name>ExtensionStarter Task</display-name>
        <description>ExtensionStarter Task</description>
        <type>java</type>
        <execution-timeout-in-secs>120</execution-timeout-in-secs>
        <task-arguments>
            <argument name="config-file" is-required="true" default-value="monitors/ExtensionStarterMonitor/config.yml" />
        </task-arguments>
        <java-task>
            <classpath>extensions-starter.jar</classpath>
            <impl-class>com.appdynamics.extensions.extensionstarter.ExtStarterMonitor</impl-class>
        </java-task>
    </monitor-run-task>
</monitor>
```

### config.yml
Every monitoring extension should ship with a `config.yml`. This file should define the configuration of the extension artifact.
In addition to configuration, the SDK reads the `config.yml` to load and initialize all the different features mentioned [below](#Features).
  

### metrics.xml
This is an optional file to define the metrics and their properties for a given extension artifact.

A new extension should be developed by extending the ABaseMonitor class. For eg. 

```
public class SampleMonitor extends ABaseMonitor {
 
  protected abstract String getDefaultMetricPrefix(){
      return "Custom Metrics|Sample Monitor"
  }
 
  public abstract String getMonitorName(){
      return "Sample Monitor"
  }
 
  protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider){
      //...logic to add the core logic for the SampleMonitor
  }
 
  protected abstract List<Map<>> getServers(){
      //configuration for multiple servers to be processed in parallel
 
  }
 
}
```
For more details on how to use this Java SDK to build an AppDynamics extension, please check [Extension Starter project](https://github.com/Appdynamics/extension-starter)

# Features

## Controller Info

## Auto Upload Custom Dashboards

## Derived Metrics

## Configurable HTTP Client

## Events Services

## Task Schedule


 

# Getting Help

For help related AppDynamics Extensions, please open a ticket on [AppDynamics Support Portal](http://help.appdynamics.com).

