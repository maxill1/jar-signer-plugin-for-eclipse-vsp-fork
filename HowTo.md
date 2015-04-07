# HowTo #

This illustrate how to set up the plugin in eclipse.

# Set eclipse to use JDK's JRE #
Download latest Java JDK (i personally use java6 jdk http://www.oracle.com/technetwork/java/javase/downloads/index.html).

Add this to your eclipse.ini (Example: D:\Program Files (x86)\eclipse\eclipse.ini):
```
-vm
C:/Program Files (x86)/Java/jdk<yourVersion>/jre/bin/javaw.exe
```


# Install the plugin #

This are the steps:
  * Download the latest version of the plugin from [download page](http://code.google.com/p/jar-signer-plugin-for-eclipse-vsp-fork/downloads/list).
  * put the jar in the "dropins" folder under your eclipse installation path (Example: D:\Program Files (x86)\eclipse\dropins)
  * Open eclipse :D

# Verify installation #

If the plugin is installed correctly you should see a "Jar Signer" item under "Window>Preferences"


# Setup signing options #

Since this tool uses oracle jarsigner for signing a jar, use the [oracle documentation](http://docs.oracle.com/javase/6/docs/technotes/tools/windows/jarsigner.html) for any info about the signing options.

You can setup the options under "Window>Preferences>Jar Signer"

![http://jar-signer-plugin-for-eclipse-vsp-fork.googlecode.com/git-history/*/jarsigner-google-code-resources/images/settings.png](http://jar-signer-plugin-for-eclipse-vsp-fork.googlecode.com/git-history/*/jarsigner-google-code-resources/images/settings.png)

# Sign a jar #
In order to sign a jar you need to create a jardesc file. You can find a nice guide [here](http://www.eclipse-tips.com/tips/20-exporting-jar-the-easy-way).

Once a jardesc file is created, right click on it to open a context menu. Click on "Create Signed Jar" to build a jar and sign it with jarsigner options.

![http://jar-signer-plugin-for-eclipse-vsp-fork.googlecode.com/git-history/*/jarsigner-google-code-resources/images/context-menu.png](http://jar-signer-plugin-for-eclipse-vsp-fork.googlecode.com/git-history/*/jarsigner-google-code-resources/images/context-menu.png)