package com.appdynamics.extensions.workbench;


import com.appdynamics.extensions.util.PathResolver;
import com.appdynamics.extensions.conf.monitorxml.Monitor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by abey.tom on 3/16/16.
 */
public class WorkbenchServerLauncher {
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static void main(String[] args) throws IOException, InterruptedException {
        File installDir = PathResolver.resolveDirectory(WorkbenchServerLauncher.class);
        if (installDir != null && installDir.exists()) {
            Monitor monitor = Monitor.from(installDir);
            List<File> files = loadAllJars(installDir, monitor);
            if (files != null && !files.isEmpty()) {
                String property = System.getProperty("java.home");
                File java = new File(property, "bin" + File.separator + getJavaExecutable());
                String cp = asClassPath(files);
                if (java.exists()) {
                    List<String> workbenchSysProps = getWorkbenchSysProps();
                    ProcessBuilder builder = new ProcessBuilder();
                    builder.inheritIO();
                    List<String> commands = new ArrayList<>();
                    commands.add(java.getAbsolutePath());
                    commands.addAll(workbenchSysProps);
                    commands.add("-cp");
                    commands.add(cp);
                    commands.add(WorkBenchServer.class.getName());
                    String[] command = commands.toArray(new String[commands.size()]);

                    String[] merged = new String[command.length + args.length];
                    System.arraycopy(command, 0, merged, 0, command.length);
                    System.arraycopy(args, 0, merged, command.length, args.length);
                    info("The launch command is %s", Arrays.toString(merged));
                    builder.command(merged);
                    Process process = builder.start();
                    process.waitFor();
                } else {
                    error("Cannot locate java, exiting the program");
                }
            } else {
                error("Cannot find the jars needed for the classpath");
            }
        } else {
            error("Cannot locate the install directory");
        }
    }

    private static List<String> getWorkbenchSysProps() {
        Properties properties = System.getProperties();
        List<String> sysProps = new ArrayList<>();
        for (Object o : properties.keySet()) {
            String key = (String) o;
            if (key.startsWith("workbench.")) {
                String value = (String) properties.get(o);
                sysProps.add("-D" + key + "=" + value);
            }
        }
        return sysProps;
    }

    private static String getJavaExecutable() {
        if (isWin()) {
            return "java.exe";
        }
        return "java";
    }

    private static String asClassPath(List<File> files) {
        String seperator;
        if (isWin()) {
            seperator = ";";
        } else {
            seperator = ":";
        }
        StringBuilder sb = new StringBuilder();
        for (File file : files) {
            sb.append(file.getAbsolutePath()).append(seperator);
        }
        if (sb.length() > 1) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private static boolean isWin() {
        return OS.indexOf("win") >= 0;
    }


    private static List<File> loadAllJars(File extensionDir, Monitor monitor) {
        File file = new File(extensionDir.getParentFile().getParentFile(), "machineagent.jar");
        if (file.exists()) {
            List<File> urls = new ArrayList<File>();
            urls.add(file);
            String classpath = monitor.getMonitorRunTask().getJavaTask().getClasspath();
            if (classpath != null) {
                String[] split = classpath.split(";");
                for (String path : split) {
                    File jar = new File(extensionDir, path);
                    if (jar.exists()) {
                        urls.add(jar);
                    } else {
                        File jar1 = new File(path);
                        if (jar1.exists()) {
                            urls.add(jar1);
                        } else {
                            error("The path %s doesnt exist. Attempted to resolve %s and %s"
                                    , path, jar.getAbsolutePath(), jar1.getAbsolutePath());

                        }
                    }
                }
            } else {
                error("The <classpath> element cannot be null in monitor.xml");
                return null;
            }
            return urls;
        } else {
            String msg = String.format("Cannot located the [%s] from [%s]. This jar can be run only from inside the <MachineAgent>/monitors/<MonitorName> durectory"
                    , file.getName(), file.getAbsolutePath());
            throw new RuntimeException(msg);
        }
    }

    private static void loadAllJars1(File extensionDir, Monitor monitor) {
        File file = new File(extensionDir.getParentFile().getParentFile(), "machineagent.jar");
        if (file.exists()) {
            List<URL> urls = new ArrayList<URL>();
            addToList(file, urls);
            String classpath = monitor.getMonitorRunTask().getJavaTask().getClasspath();
            if (classpath != null) {
                String[] split = classpath.split(";");
                for (String path : split) {
                    File jar = new File(extensionDir, path);
                    if (jar.exists()) {
                        addToList(jar, urls);
                    } else {
                        File jar1 = new File(path);
                        if (!jar1.exists()) {
                            error("The path %s doesnt exist. Attempted to resolve %s and %s"
                                    , path, jar.getAbsolutePath(), jar1.getAbsolutePath());
                        } else {
                            addToList(jar1, urls);
                        }

                    }
                }
            } else {
                error("The <classpath> element cannot be null in monitor.xml");
                return;
            }
            URLClassLoader urlCl = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), System.class.getClassLoader());
            Thread.currentThread().setContextClassLoader(urlCl);
        } else {
            String msg = String.format("Cannot located the [%s] from [%s]. This jar can be run only from inside the <MachineAgent>/monitors/<MonitorName> durectory"
                    , file.getName(), file.getAbsolutePath());
            throw new RuntimeException(msg);
        }
    }

    private static void info(String format, String... vars) {
        System.out.println("[INFO] " + String.format(format, vars));
    }

    private static void error(String format, String... vars) {
        System.err.println("[ERROR] " + String.format(format, vars));
    }

    private static void addToList(File file, List<URL> urls) {
        URL url = getUrl(file);
        if (url != null) {
            urls.add(url);
            info("Adding the jar [%s] to the classpath", url.toString());
        }
    }

    private static URL getUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            error("The file cannot be converted to URL " + file.getAbsolutePath());
            e.printStackTrace();
            return null;
        }
    }


}
