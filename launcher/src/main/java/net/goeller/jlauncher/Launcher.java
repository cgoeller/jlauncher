package net.goeller.jlauncher;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
	private static Logger LOG = LoggerFactory.getLogger(Launcher.class);
	private static File appDir;
	private static File appDirTemp;
	private static File appDirLog;

	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Usage: java net.goeller.jlauncher.Launcher <launchConfigLocation>");
			System.exit(1);
		}

		String launchConfigLocation = args[0];
		LaunchConfig launchConfig = loadLaunchConfig(launchConfigLocation);

		appDir = new File(System.getProperty("user.home") + "/" + launchConfig.getName());
		appDirTemp = new File(appDir, "temp");
		appDirTemp.mkdirs();

		appDirLog = new File(appDir, "log");
		appDirLog.mkdirs();

		// download all the stuff
		URL appPackage = createUrl(launchConfig.getBaseUrl(), launchConfig.getAppPackage());
		URL jrePackage = createUrl(launchConfig.getBaseUrl(), launchConfig.getJrePackage());

		File app = download(appPackage);
		File jre = download(jrePackage);

		unpack(app);
		unpack(jre);

		try {
			FileUtils.deleteDirectory(appDirTemp);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Build Command Line and Classpath
		String java = appDir + "/jre/bin/java";

		List<String> cmds = new ArrayList<>();
		cmds.add(java);
		cmds.add(launchConfig.getMainClass());
		if (launchConfig.getParams() != null) {
			cmds.addAll(launchConfig.getParams());
		}

		LOG.info("Starting " + cmds);

		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.directory(appDir);
		File log = new File(appDirLog, "app.log");
		pb.redirectErrorStream(true);
		pb.redirectOutput(Redirect.appendTo(log));
		try {
			Process p = pb.start();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static void unpack(File file) {
		LOG.info("Unpacking " + file.getAbsolutePath());

		try {
			ZipFile zipFile = new ZipFile(file);
			zipFile.extractAll(appDir.getAbsolutePath());
		} catch (ZipException e) {
			throw new RuntimeException(e);
		}
	}

	private static File download(URL url) {
		LOG.info("Downloading " + url.toString());

		File destination = new File(appDirTemp, url.getFile());
		try {
			FileUtils.copyURLToFile(url, destination, 30000, 30000);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return destination;
	}

	private static URL createUrl(String baseUri, String pack) {
		String url = baseUri;
		if (!url.endsWith("/")) {
			url += "/";
		}
		url += pack;
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	private static LaunchConfig loadLaunchConfig(String location) {
		LaunchConfig cfg = new LaunchConfig();
		cfg.setBaseUrl("http://localhost:8080/");
		cfg.setAppPackage("app.zip");
		cfg.setJrePackage("jre.zip");
		cfg.setFullName("Test Application");
		cfg.setName("testapp");
		cfg.setMainClass("net.goeller.test");
		
		return cfg;
	}
}
