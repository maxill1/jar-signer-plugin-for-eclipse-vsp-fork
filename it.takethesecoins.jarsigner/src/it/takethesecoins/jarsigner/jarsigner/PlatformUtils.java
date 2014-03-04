package it.takethesecoins.jarsigner.jarsigner;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Utility to check Platform/SKD related info
 * @author lmazzilli
 *
 */
public class PlatformUtils {

	private static String OS = System.getProperty("os.name").toLowerCase();

	public static boolean isWindows() {
 
		return (OS.indexOf("win") >= 0);
 
	}
 
	public static boolean isMac() {
 
		return (OS.indexOf("mac") >= 0);
 
	}
 
	public static boolean isUnix() {
 
		return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
 
	}
 
	public static boolean isSolaris() {
 
		return (OS.indexOf("sunos") >= 0);
 
	}
	
	/**
	 * Absolute path of default VM
	 * @return
	 */
	public static String getDefaultJREPath() {
		File miJRE = JavaRuntime.getDefaultVMInstall().getInstallLocation();
		String jre = miJRE.toString();
		String jrePath =  jre + File.separatorChar + "bin" + File.separatorChar;
		return jrePath;
	}

	/**
	 * Absolute path of current running VM
	 * @return
	 */
	public static String getRunningJREPath() {
		String jreRunning = System.getProperty("java.home");
		return jreRunning;
	}

	public static boolean currentVmAndDefaultEquals() {
		String jreDef = JavaRuntime.getDefaultVMInstall().getInstallLocation().toString();
		String jreRunning = new File(getRunningJREPath()).getParent();
		return jreDef.equals(jreRunning);
	}

	/**
	 * Check if current JRE is from JDK
	 * @return
	 */
	public static boolean isRunningJDK() {
		String running = getRunningJREPath();
		String parent = new File(running).getParent();
		if(parent.toLowerCase().indexOf("jdk")>-1 && existsExecutable(parent)){
			return true;
		}
		return false;
	}
	
	/**
	 * Check if exists a jarsigner executable in that folder
	 * @param parent
	 * @return
	 */
	private static boolean existsExecutable(String parent) {
		String path = parent;
		if(!path.endsWith(String.valueOf(File.separatorChar))){
			path+=File.separatorChar;
		}
		
		if(!path.endsWith("bin"+File.separatorChar)){
			path = path+"bin"+File.separatorChar;
		}
		
		return new File(path+getExecutable()).exists();
	}

	/**
	 * jarsigner executable name
	 * @return
	 */
	public static String getExecutable() {
		String executable = "jarsigner";
		//windows fix (maybe useless)
		if(PlatformUtils.isWindows()){
			executable+=".exe";
		}
		return executable;
	}
	
	
	public static File[] findSuitableJDK(){
		
		String basePath = "/usr/java/"; //TODO untested
		if(isWindows()){
			basePath = "C:\\Program Files (x86)\\Java";
		}
		
		File[] folders = new File(basePath).listFiles(new FileFilter() {
			
			public boolean accept(File pathname) {
				
				boolean firstCheck = pathname.isDirectory() && pathname.getName().toLowerCase().contains("jdk");
				if(firstCheck){
					return existsExecutable(pathname.getAbsolutePath());
				}
				
				return false;
			}
		});
		
		return folders;
	}

	/**
	 * ex c:\Program Files\Java\jdk.x.x.xxx\jre\bin\
	 * @param absolutePath
	 * @return
	 */
	public static String getJRE_bin_fromJDKPath(String absolutePath) {
		return absolutePath+File.separatorChar+"jre"+File.separatorChar+"bin"+File.separatorChar;
	}
	
	/**
	 * ex c:\Program Files\Java\jdk.x.x.xxx\bin\
	 * @param absolutePath
	 * @return
	 */
	public static String getJDK_bin_fromJDKPath(String absolutePath) {
		return absolutePath+File.separatorChar+"bin"+File.separatorChar;
	}

	/**
	 * Add escapes and fixes common binary names issues
	 * @param customCode
	 * @return
	 */
	public static String[] fixBinaryPath(String exec, String... arguments) {

		List<String> args = new ArrayList<String>();
		if(exec.endsWith(".bat")){
		
			if(exec.contains("\\")){
				exec = exec.replace("\\", "/");
			}

			args.add("cmd");
			args.add("/c");
			args.add("start");
			args.add("jasigner custom code");
			args.add(exec);
		}else{
			args.add(exec);
		}
		
		if(arguments!=null){
			args.addAll(Arrays.asList(arguments));
		}
		
		String[] array = new String[args.size()];
		for (int i = 0; i < args.size(); i++) {
			array[i] = args.get(i);
		}
		return array;
	}
}
