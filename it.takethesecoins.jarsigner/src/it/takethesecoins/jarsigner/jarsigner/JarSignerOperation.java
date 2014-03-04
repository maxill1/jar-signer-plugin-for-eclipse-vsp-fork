package it.takethesecoins.jarsigner.jarsigner;

import it.takethesecoins.jarsigner.Activator;
import it.takethesecoins.jarsigner.preferences.PreferenceConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.jarpackager.JarFileExportOperation;
import org.eclipse.jdt.internal.ui.jarpackager.JarPackagerMessages;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;


public class JarSignerOperation extends JarFileExportOperation implements IJarExportRunnable 
{
	protected MessageMultiStatus fStatus;
	protected JarPackageData fJarPackage;
	protected JarPackageData fJarPackages[];
	protected Shell fParentShell;
	
	/**
	 * The absolute path of the keystore
	 */
	protected String keystorePath = getPreferences().getString(PreferenceConstants.KEYSTORE);
	/**
	 * The absolute path of the jre/jdk
	 */
	protected String jdkPath =  PlatformUtils.getDefaultJREPath();

	public JarSignerOperation(JarPackageData[] jarPackages, Shell shell) {
		super(jarPackages, shell);
		fStatus = new MessageMultiStatus(JavaPlugin.getPluginId(), 0, "", null);
		fJarPackages = jarPackages;
		fParentShell = shell;
	}

	/**
	 * Exports the resources as specified by the JAR package.
	 *
	 * @param       progressMonitor the progress monitor that displays the progress
	 * @see #getStatus()
	 */
	protected void execute(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException 
	{
		int count= fJarPackages.length;
		progressMonitor.beginTask("", count); //$NON-NLS-1$
		try {
			for (int i= 0; i < count; i++) {
				SubProgressMonitor subProgressMonitor= new SubProgressMonitor(progressMonitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK);
				fJarPackage = fJarPackages[i];
				if(fJarPackage != null)
					singleRun(subProgressMonitor);
			}
		} finally {
			progressMonitor.done();
		}
	}

	protected void singleRun(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException 
	{
		try {
    		
			int totalWork = fJarPackages.length;
			progressMonitor.beginTask("", totalWork); //$NON-NLS-1$
			
			
			boolean hasJarsignerDefined = hasJasignerArgs();
			boolean hasBatch = hasCustomBatch();
			
			if (hasJarsignerDefined && preconditionsOK()){
				sign();
				
				if(hasBatch){
					executeCustomBatch();
				}
			}
			
			if(!hasJarsignerDefined && hasBatch){
				executeCustomBatch();
			}

		} finally {
			progressMonitor.done();
		}
	}

	private boolean hasJasignerArgs() {
		return exists(keystorePath);
	}

	private boolean exists(String arg) {
		return arg!=null && arg.trim().length()>0;
	}

	/**
	 * Checks if it has custom code to launch
	 * @return
	 */
	private boolean hasCustomBatch() {
		IPreferenceStore store = getPreferences();
		String customCode = store.getString(PreferenceConstants.CUSTOMBATCH);
		if (customCode == null || customCode.trim().length() == 0) {
			return false;
		}
		return true;
	}

	private void executeCustomBatch() {
		IPreferenceStore store = getPreferences();
		String customCode = store.getString(PreferenceConstants.CUSTOMBATCH);
		if (customCode == null || customCode.trim().length() == 0) {
			return;
		}
		
		String destination = fJarPackage.getJarLocation().toOSString();
		
		String[] args = PlatformUtils.fixBinaryPath(customCode, destination);
		try {
			Runtime.getRuntime().exec(args);
//			MessageDialog.openConfirm(fParentShell, "Custom batch", "executed correctly "+Arrays.toString(args));
		} catch (IOException e) {
			addError("Couldn't execute '"+customCode+"' with args '"+destination+"' ", e);
		}
	}

	/**
	 * Check if a jdk is installed, etc..
	 * @return
	 */
	private boolean preconditionsOK(){
		
		if(!checkJDK()){
			return false;
		}
		
		if(!checkExecutable()){
			return false;
		}
		
		if(!checkKeystore()){
			return false;
		}
		
		//TODO more
		return true;
	}
	
	/**
	 * check if a JDK is installed
	 * @return
	 */
	private boolean checkJDK() {
		try {
			//verifica se è una JDK
			if(!PlatformUtils.isRunningJDK()){
				
				File[] suitableJDKs = PlatformUtils.findSuitableJDK();
				
				String msg = "Eclipse is running with JRE ("+PlatformUtils.getRunningJREPath()+"). \n";
				
				if(suitableJDKs!=null && suitableJDKs.length>0){
					String firstJDK = PlatformUtils.getJRE_bin_fromJDKPath(suitableJDKs[0].getAbsolutePath()); 
					msg+="Please add -vm\n "+firstJDK+" to your eclipse.ini.";
				}else{
					msg+="Please install a Java JDK and add -vm\n C:/Program Files (x86)/Java/jdk1.x.xxx/jre/bin/javaw.exe to your eclipse.ini.";
				}
				
				throw new Exception(msg);
			}
			
			//Verifica se la jre in utilizzo è quella di default
			if(!PlatformUtils.currentVmAndDefaultEquals()){
				throw new Exception("No JDK path found or running eclipse with different JDK's JRE (running: "+PlatformUtils.getRunningJREPath()+" default: "+jdkPath+" )");
			}
			//controllo se esiste la path
			if(!new File(jdkPath).exists()){
				throw new Exception("No JDK path found or running eclipse with different JDK's JRE ("+jdkPath+")");
			}
		}catch (Exception e) {
			addError("Error: to use jarsigner binary directly a JDK is required. \n"
					+ " Current JRE is not a JDK or you are running a JRE outside JDK main folder. \n"
					+ "You can alternatively launch a bat or sh by usign 'custom bin' in settings. \n", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Check if jarsigner bin exists
	 * @return
	 */
	private boolean checkExecutable(){
		try {
			//controllo se esiste il bin jarsinger
			if(!new File(jdkPath+PlatformUtils.getExecutable()).exists()){
				throw new Exception("No jarsigner executable found ("+PlatformUtils.getExecutable()+")");
			}
		}catch (Exception e) {
			addError("Error: missing required executable in "+jdkPath+" \n"+PlatformUtils.getExecutable()+"\n", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Check if the keystore file exists
	 * @return
	 */
	private boolean checkKeystore() {

		try {
			//JAR creation failed. See details for additional information.
			//controllo se esiste il bin keystore
			if(!new File(keystorePath).exists()){
				throw new Exception("No keystore file found ("+keystorePath+")");
			}
		}catch (Exception e) {
			addError("Error: cannot find keystore file: \n"+keystorePath+"\n", e);
			return false;
		}
		return true;
	}

	protected void sign() {

		//    	C:\Program Files (x86)\Java\jdk1.6.0_23\bin\jarsigner  -keystore X:\Sistema\xxx.p12 -storepass xxxx -storetype pkcs12 D:\Test\xxxx.jar test
		IPreferenceStore store = getPreferences();
		
		boolean verify = isVerify();

		ArrayList args = getArgs(store);
		
		String executable = PlatformUtils.getExecutable();

//		execute(executable, args);
		String sign = executeLegacy(jdkPath+executable, args, false); //sempre false per il primo passaggio (firma)
		String verif = "";
		if(verify){
			verif = executeLegacy(jdkPath+executable, args, verify); //in funzione della PreferenceStore
		}
		
		if(verify){
			if(sign.equals("") && verif.equals("")){
				MessageDialog.openConfirm(fParentShell, "JAR Exported", "JAR signed and verified sucessfully");
			}else if(!sign.equals("")){
				addError("JarSigner Sign error: \n", new Exception(sign));
			}else if(!verif.equals("")){
				addError("JarSigner Verify error: \n", new Exception(verif));
			}else{
				addError("JarSigner Error: \n", new Exception(sign+"\n "+verif));
			}
		}else{
			if(sign.equals("")){
				MessageDialog.openConfirm(fParentShell, "JAR Exported", "JAR signed sucessfully");
			}else{
				addError("JarSigner Error: \n", new Exception(sign));
			}
		}
	}


	/**
	 * Check if the operation should also verify the jar signing
	 * @return
	 */
	private boolean isVerify() {
		return getPreferences().getString(PreferenceConstants.VERIFY).equals("true");
	}

	private IPreferenceStore getPreferences() {
		return Activator.getDefault().getPreferenceStore();
	}


//	private void execute(String executableOLDTorem, ArrayList args) {
//
////		if(!executable.startsWith( "\"") && !executable.endsWith( "\"")){
////			executable = "\""+executable+"\"";
////		}
//		
//		//        
//		String executable =  "jarsigner";
//		if(PlatformUtils.isWindows()){
//			executable+=".exe";
//		}
//		//path della jre
//		String jdkPath =  PlatformUtils.getDefaultJREPath();
//		
//		String command = jdkPath+ executable;
//		for (Iterator iterator = args.iterator(); iterator.hasNext();) {
//			String option = (String) iterator.next();
//			command+= " "+option;
//		}
//		
//		ProcessBuilder probuilder = new ProcessBuilder(command); //executable, args.get(0), args.get(1), args.get(2), args.get(3), args.get(4), args.get(5));
//		
//		//You can set up your work directory
//		probuilder.directory(new File(jdkPath));
//
//		Process process = null;
//		
//		String error = "";
//		try {
//			
//			process = probuilder.start();
//
//			//Read out dir output
//			InputStream is = process.getInputStream();
//			InputStreamReader isr = new InputStreamReader(is);
//			BufferedReader br = new BufferedReader(isr);
//			String line;
//			System.out.printf("Output of running %s is:\n", command);
//			while ((line = br.readLine()) != null) {
//				System.out.println(line);
//				if(line.equals("Usage: jarsigner [options] jar-file alias") || error.length()>0){
//					error+=line+"\n";
//				}
//			}
//			
//		} catch (Exception e) {
//			addError("Error signing jar",e);
//			e.printStackTrace();
//		}
//		
//		//Wait to get exit value
//		try {
//			if(process!=null){
//				int exitValue = process.waitFor();
//				MessageDialog.openConfirm(fParentShell, "JAR Exported", "JAR signed sucessfully");
//				System.out.println("\n\nExit Value is " + exitValue);
//			}
//		} catch (InterruptedException e) {
//			addError("Error signing jar",e);
//			e.printStackTrace();
//		}
//		
//		if(error.length()>0){
//			addError("Error signing jar", new Exception(error));
//		}
//		
//		System.out.println(probuilder.command());
//	}

	/**
	 * @param executable
	 * @param args
	 * @param verify 
	 */
	private String executeLegacy(String executable, ArrayList originalArgs, boolean verify) {
		
		List args = new ArrayList();
		args.add(executable); //comando eseguibile
		if(!verify){
			//aggiunta di tutti i param tranne -verify
			for (Iterator iterator = originalArgs .iterator(); iterator.hasNext();) {
				String param = (String) iterator.next();
				if(!param.equals("-verify")){
					args.add(param);
				}
			}
		}else{
			//se c'è bene, altrimenti ciccia
			args.addAll(originalArgs);
		}
		
		try {
			
			String[] arrayArgs = new String[args.size()];
			for (int i = 0; i < arrayArgs.length; i++) {
				String param = args.get(i).toString();
				arrayArgs[i] = param;
			}
			Process miP = Runtime.getRuntime().exec(arrayArgs);

			InputStream in = miP.getInputStream();
			InputStream err = miP.getErrorStream();

			StreamCoupler sce = new StreamCoupler(in, System.out);
			StreamCoupler sci =	new StreamCoupler(err, System.err);

			sce.start();
			sci.start();

			int res = miP.waitFor();
			if (res != 0) {
//				addError(sci.getLog()+"\n " +sce.getLog(), null);    			
				return sci.getLog()+"\n " +sce.getLog();
			}
			
			if(verify && sci.getLog().toLowerCase().contains("jar is unsigned")){
				return sci.getLog()+"\n " +sce.getLog();
			}

		} catch (Exception e) {
			return e.getMessage();
//			addError("JarFileExportOperation_errorSavingDescription: \n"+executable, e);
		} catch (Error e) {
//			addError("JarFileExportOperation_errorSavingDescription: \n"+executable, e);
			return e.getMessage();
		}
		
		return "";
	}

	private ArrayList getArgs(IPreferenceStore store) {

		ArrayList args = new ArrayList();

		//eseguibile

		//        File miJRE = JavaRuntime.getDefaultVMInstall().getInstallLocation();
		//        String jre = miJRE.toString();
		//        String executable =  jre + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "jarsigner";
		////        if(PlatformUtils.isWindows()){
		////        	executable+=".exe";
		////        }
		////        executable = "\""+executable+"\"";
		//        args.add(executable);

		//verify
		String verify = store.getString(PreferenceConstants.VERIFY);
		if (verify.equals("") == false) {
			verify = "-verify"; 
			args.add(verify);
		}

		//Store type
		String val = store.getString(PreferenceConstants.STORETYPE);
		if (val.equals("") == false) {
			String option = "-storetype"; //BUG fixed era storepass...
			args.add(option);
			args.add(val);
		}

		//keystore
		args.add("-keystore");
		args.add(store.getString(PreferenceConstants.KEYSTORE));

		//password
		val = store.getString(PreferenceConstants.STOREPASS);
		if (val.equals("") == false) {
			String option = "-storepass"; 
			args.add(option);
			args.add(val);
		}

		//      String options =  storetype + keystore + storepass; //BUG FIX  riposizionato storetype prima di keystore

		String destination = fJarPackage.getJarLocation().toOSString();
		args.add(destination);

		//Alias
		String alias = store.getString(PreferenceConstants.ALIAS);
		args.add(alias);


		//trim degli spazi
		ArrayList trimmedArgs = new ArrayList();
		for (Iterator iterator = args.iterator(); iterator.hasNext();) {
			String string = (String) iterator.next();
			trimmedArgs.add(string.trim());
		}

		return trimmedArgs;
	}

	protected void addToStatus(CoreException ex) {
		IStatus status= ex.getStatus();
		String message= ex.getLocalizedMessage();
		if (message == null || message.length() < 1) {
			message= "JarFileExportOperation.coreErrorDuringExport"; //$NON-NLS-1$
			status= new Status(status.getSeverity(), status.getPlugin(), status.getCode(), message, ex);
		}
		fStatus.add(status);
	}

	private void addWarning(String message, Throwable error)
	{
		fStatus.add(new Status(2, JavaPlugin.getPluginId(), 10001, message, error));
	}

	private void addError(String message, Throwable error)
	{
		fStatus.add(new Status(4, JavaPlugin.getPluginId(), 10001, message, error));
	}

	public IStatus getStatus() {
		String message = null;
		switch(fStatus.getSeverity())
		{
		case 0: // '\0'
			message = "";
			break;

		case 1: // '\001'
			message = JarPackagerMessages.JarFileExportOperation_exportFinishedWithInfo;
			break;

		case 2: // '\002'
			message = JarPackagerMessages.JarFileExportOperation_exportFinishedWithWarnings;
			break;

		case 4: // '\004'
			if(fJarPackages.length > 1)
				message = JarPackagerMessages.JarFileExportOperation_creationOfSomeJARsFailed;
			else
				message = JarPackagerMessages.JarFileExportOperation_jarCreationFailed;
			break;

		case 3: // '\003'
		default:
			message = "";
			break;
		}
		fStatus.setMessage(message);
		return fStatus;
	}

	static class MessageMultiStatus extends MultiStatus
	{

		protected void setMessage(String message)
		{
			super.setMessage(message);
		}

		MessageMultiStatus(String pluginId, int code, String message, Throwable exception)
		{
			super(pluginId, code, message, exception);
		}
	}
}