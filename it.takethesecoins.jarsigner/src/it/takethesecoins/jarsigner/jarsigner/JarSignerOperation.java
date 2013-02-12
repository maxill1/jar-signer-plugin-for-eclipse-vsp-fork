package it.takethesecoins.jarsigner.jarsigner;

import it.takethesecoins.jarsigner.Activator;
import it.takethesecoins.jarsigner.preferences.PreferenceConstants;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.jarpackager.JarFileExportOperation;
import org.eclipse.jdt.internal.ui.jarpackager.JarPackagerMessages;
import org.eclipse.jdt.launching.JavaRuntime;
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
//    		if (!preconditionsOK())
//    			throw new InvocationTargetException(null, JarPackagerMessages.getString("JarFileExportOperation.jarCreationFailedSeeDetails")); //$NON-NLS-1$
    		int totalWork = fJarPackages.length;

    		progressMonitor.beginTask("", totalWork); //$NON-NLS-1$

    		sign();

    	} finally {
    		progressMonitor.done();
    	}
    }

    protected void sign() 
    {
    	
    	//"C:\Program Files (x86)\Java\jdk1.6.0_23\bin\jarsigner.exe" -storetype pkcs12 -keystore X:\Sistema\FirmaCodice201204.p12 -storepass 1985GH$e %1 1
//    	C:\Program Files (x86)\Java\jdk1.6.0_23\bin\jarsigner  -keystore X:\Sistema\FirmaCodice201204.p12 -storepass 1985GH$e -storetype pkcs12 D:\Test\ResCSO13.jar test
    	
    	IPreferenceStore store = Activator.getDefault().getPreferenceStore();

    	String verify = store.getString(PreferenceConstants.VERIFY);
    	if (verify.equals("") == false) {
    		verify = " -verify "; 
    	}

    	String keystore = " -keystore " + store.getString(PreferenceConstants.KEYSTORE);
    	
    	String storepass = store.getString(PreferenceConstants.STOREPASS);
    	if (storepass.equals("") == false) {
    		storepass = " -storepass " + storepass; 
    	}

    	String storetype = store.getString(PreferenceConstants.STORETYPE);
    	if (storetype.equals("") == false) {
    		storetype = " -storetype " + storetype; //BUG fixed era storepass...
    	}

    	String alias = store.getString(PreferenceConstants.ALIAS);

        IPath destination = fJarPackage.getJarLocation();

        String options =  storetype + keystore + storepass; //BUG FIX  riposizionato storetype prima di keystore
        
        File miJRE = JavaRuntime.getDefaultVMInstall().getInstallLocation();
        
        String jre = miJRE.toString();
        
    	String command = jre + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "jarsigner " + verify + options + " " + destination.toOSString() + " " + alias;
    	
    	System.out.println(command);

    	try {

    		Process miP = Runtime.getRuntime().exec(command);
    		
			InputStream in = miP.getInputStream();
			InputStream err = miP.getErrorStream();

			StreamCoupler sce = new StreamCoupler(in, System.out);
			StreamCoupler sci =	new StreamCoupler(err, System.err);

			sce.start();
			sci.start();

    		int res = miP.waitFor();
    		if (res != 0) {
    			addError(sce.getLog(), null);    			
    			return;
    		}
    		
    		MessageDialog.openConfirm(fParentShell, "JAR Exported", "JAR signed sucessfully");

    	} catch (Exception e) {
    		addError("JarFileExportOperation_errorSavingDescription", e);
    	} catch (Error e) {
    		addError("JarFileExportOperation_errorSavingDescription", e);
		}
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