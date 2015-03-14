package net.goeller.jlauncher;

public class Launcher {
	public static void main(String[] args) {

		if (args.length != 1) {
			System.out.println("Usage: java net.goeller.jlauncher.Launcher <launchConfigLocation>");
			System.exit(1);
		}
		
		String launchConfigLocation = args[0];
		LaunchConfig launchConfig = loadLaunchConfig(launchConfigLocation);
		
		// download all stuff
		
	}
	
	private static LaunchConfig loadLaunchConfig(String location) {
		return null;
	}
}
