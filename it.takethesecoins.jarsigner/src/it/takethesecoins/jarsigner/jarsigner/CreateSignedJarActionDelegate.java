package it.takethesecoins.jarsigner.jarsigner;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.jarpackager.CreateJarActionDelegate;
import org.eclipse.jdt.internal.ui.jarpackager.JarPackagerMessages;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.PlatformUI;

public class CreateSignedJarActionDelegate extends CreateJarActionDelegate {

	public void run(IAction action) {
		super.run(action);
		
		/* Jarsigner code*/
        IFile descriptions[] = getDescriptionFiles(getSelection());
        int length = descriptions.length;
        if(length < 1)
            return;

        String message;
        if(length > 1)
            message = JarPackagerMessages.JarFileExportOperation_creationOfSomeJARsFailed;
        else
            message = JarPackagerMessages.JarFileExportOperation_jarCreationFailed;

        MultiStatus readStatus = new MultiStatus(JavaPlugin.getPluginId(), 0, message, null);

        JarPackageData jarPackages[] = readJarPackages(descriptions, readStatus);

        MultiStatus mergedStatus;

        IStatus status = sign(jarPackages);
        if(status == null)
            return;
        if(readStatus.getSeverity() == 4)
            message = readStatus.getMessage();
        else
            message = status.getMessage();

        mergedStatus = new MultiStatus(JavaPlugin.getPluginId(), status.getCode(), readStatus.getChildren(), message, null);
        mergedStatus.merge(status);

        if(!mergedStatus.isOK())
        	ErrorDialog.openError(getShell(), JarPackagerMessages.CreateJarActionDelegate_jarExport_title, null, mergedStatus);
	}

    private JarPackageData[] readJarPackages(IFile descriptions[], MultiStatus readStatus)
    {
        List jarPackagesList = new ArrayList(descriptions.length);
        for(int i = 0; i < descriptions.length; i++)
        {
            JarPackageData jarPackage = readJarPackage(descriptions[i], readStatus);
            if(jarPackage != null)
                jarPackagesList.add(jarPackage);
        }

        return (JarPackageData[])jarPackagesList.toArray(new JarPackageData[jarPackagesList.size()]);
    }

	private IStatus sign(JarPackageData[] jarPackages)
    {
        org.eclipse.swt.widgets.Shell shell = getShell();
        IJarExportRunnable op = new JarSignerOperation(jarPackages, shell);
        try
        {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(false, true, op);
        }
        catch(InvocationTargetException ex)
        {
            if(ex.getTargetException() != null)
            {
                ExceptionHandler.handle(ex, shell, JarPackagerMessages.CreateJarActionDelegate_jarExportError_title, JarPackagerMessages.CreateJarActionDelegate_jarExportError_message);
                return null;
            }
        }
        catch(InterruptedException _ex)
        {
            return null;
        }
        return op.getStatus();
    }	
}