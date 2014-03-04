package it.takethesecoins.jarsigner.preferences;

import it.takethesecoins.jarsigner.Activator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class JarSignerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage 
{
	protected StringFieldEditor aliasEditor = null;
	protected StringFieldEditor keystoreEditor = null;
	protected StringFieldEditor storepassEditor = null;
	protected StringFieldEditor keypassEditor = null;
	protected StringFieldEditor sigfileEditor = null;
	protected StringFieldEditor signedjarEditor = null;
	protected BooleanFieldEditor verifyEditor = null;
	protected BooleanFieldEditor verboseEditor = null;
	protected BooleanFieldEditor certsEditor = null;
	protected StringFieldEditor tsaEditor = null;
	protected StringFieldEditor tsacertEditor = null;
	protected StringFieldEditor altsignerEditor = null;
	protected StringFieldEditor altsignerpathEditor = null;
	protected BooleanFieldEditor internalsfEditor = null;
	protected BooleanFieldEditor sectionsonlyEditor = null;
	protected BooleanFieldEditor protectedEditor = null;
	protected StringFieldEditor storetypeEditor = null;
	protected StringFieldEditor customBinEditor = null;
	
//	protected StringFieldEditor providernameEditor = null;
//	protected StringFieldEditor providerclassEditor = null;
	
	public JarSignerPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Select jarsigner params:");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		aliasEditor = new StringFieldEditor(PreferenceConstants.ALIAS, "A&lias: ", getFieldEditorParent()); 
		keystoreEditor = new StringFieldEditor(PreferenceConstants.KEYSTORE, "&Keystore: ", getFieldEditorParent()); 
		storepassEditor = new StringFieldEditor(PreferenceConstants.STOREPASS, "&Storepass: ", getFieldEditorParent()); 
		storetypeEditor  = new StringFieldEditor(PreferenceConstants.STORETYPE, "Storet&ype: ", getFieldEditorParent()); 
		keypassEditor = new StringFieldEditor(PreferenceConstants.KEYPASS, "K&eypass: ", getFieldEditorParent());
		sigfileEditor = new StringFieldEditor(PreferenceConstants.SIGFILE, "Sig&file: ", getFieldEditorParent());
		signedjarEditor = new StringFieldEditor(PreferenceConstants.SIGNEDJAR, "Signed&jar: ", getFieldEditorParent());
		verifyEditor = new BooleanFieldEditor(PreferenceConstants.VERIFY, "&Verify", getFieldEditorParent());
		verboseEditor = new BooleanFieldEditor(PreferenceConstants.VERBOSE, "Ver&bose", getFieldEditorParent());
		certsEditor = new BooleanFieldEditor(PreferenceConstants.CERTS, "&Certs", getFieldEditorParent());
		tsaEditor = new StringFieldEditor(PreferenceConstants.TSA, "&Tsa: ", getFieldEditorParent());
		tsacertEditor = new StringFieldEditor(PreferenceConstants.TSACERT, "Tsace&rt: ", getFieldEditorParent());
		altsignerEditor = new StringFieldEditor(PreferenceConstants.ALTSIGNER, "&Altsigner: ", getFieldEditorParent());
		altsignerpathEditor = new StringFieldEditor(PreferenceConstants.ALTSIGNERPATH, "Altsigner&path: ", getFieldEditorParent());
		internalsfEditor = new BooleanFieldEditor(PreferenceConstants.INTERNALSF, "&Internalsf", getFieldEditorParent());
		sectionsonlyEditor = new BooleanFieldEditor(PreferenceConstants.SECTIONSONLY, "Sections&only", getFieldEditorParent());
		protectedEditor = new BooleanFieldEditor(PreferenceConstants.PROTECTED, "protecte&d", getFieldEditorParent());
		customBinEditor  = new StringFieldEditor(PreferenceConstants.CUSTOMBATCH, "Custom&Bin: ", getFieldEditorParent()); 
		addField(aliasEditor);
		addField(keystoreEditor);
		addField(storepassEditor);
		addField(storetypeEditor);
		addField(keypassEditor);
		addField(sigfileEditor);
		addField(signedjarEditor);
		addField(verifyEditor);
		addField(verboseEditor);
		addField(certsEditor);
		addField(tsaEditor);
		addField(tsacertEditor);
		addField(altsignerEditor);
		addField(altsignerpathEditor);
		addField(internalsfEditor);
		addField(sectionsonlyEditor);
		addField(protectedEditor);
		addField(customBinEditor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
	protected void checkState() {
		super.checkState();
		
		boolean hasKeyStore = keystoreEditor.getStringValue()!= null && !keystoreEditor.getStringValue().equals("");
		boolean hasCustomBin = customBinEditor.getStringValue()!= null && !customBinEditor.getStringValue().equals("");
		
		if(hasKeyStore || hasCustomBin){
			setErrorMessage(null);
			setValid(true);
		}
		
		if(!hasKeyStore && !hasCustomBin){
			setErrorMessage("Please put a value in 'Keystore' or in 'Custom bin' (a bat file or sh file that accept 1 argument)");
			setValid(false);
		}
	}
	
	public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        if (event.getProperty().equals(FieldEditor.VALUE)) {
                  checkState();
        }        
	}
}