package com.popoaichuiniu.jacy;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.activation.UnsupportedDataTypeException;

import com.popoaichuiniu.util.Config;
import com.popoaichuiniu.util.Util;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import soot.JastAddJ.Opt;
import soot.Main;
import soot.PackManager;
import soot.Scene;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.infoflow.android.callbacks.AbstractCallbackAnalyzer;
import soot.jimple.infoflow.android.data.parsers.PermissionMethodParser;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.android.resources.ARSCFileParser;
import soot.jimple.infoflow.android.resources.LayoutFileParser;
import soot.jimple.infoflow.android.source.parsers.xml.XMLSourceSinkParser;
import soot.jimple.infoflow.rifl.RIFLSourceSinkDefinitionProvider;
import soot.jimple.infoflow.source.data.ISourceSinkDefinitionProvider;
import soot.options.Options;

public class MySetupApplication extends SetupApplication {

    public void calculateMyEntrypoints(String sourceSinkFile) throws IOException, XmlPullParserException {
        ISourceSinkDefinitionProvider parser = null;// Common interface for all
//													// classes that support
//													// loading source and sink
//													// definitions
//
//		String fileExtension = sourceSinkFile.substring(sourceSinkFile.lastIndexOf("."));
//		fileExtension = fileExtension.toLowerCase();
//
//		try {
//			if (fileExtension.equals(".xml"))
//				parser = XMLSourceSinkParser.fromFile(sourceSinkFile);
//			else if (fileExtension.equals(".txt"))
//				parser = PermissionMethodParser.fromFile(sourceSinkFile);
//			else if (fileExtension.equals(".rifl"))
//				parser = new RIFLSourceSinkDefinitionProvider(sourceSinkFile);
//			else
//				throw new UnsupportedDataTypeException("The Inputfile isn't a .txt or .xml file.");
//
//			
//		} catch (SAXException ex) {
//			throw new IOException("Could not read XML file", ex);
//		}

        //calculateSourcesSinksEntrypoints(parser);
        calculateSourcesSinksEntrypoints(parser);
    }

    public MySetupApplication(String androidJar, String apkFileLocation) {
        super(androidJar, apkFileLocation);
        // TODO Auto-generated constructor stub
    }

    private void calculateCallbackMethods(ARSCFileParser resParser, LayoutFileParser lfp) throws IOException {
        AbstractCallbackAnalyzer jimpleClass = null;

        boolean hasChanged = true;
        while (hasChanged) {
            hasChanged = false;

            // Create the new iteration of the main method

            initializeSoot();
            //createMainMethod();// 这些callback基于这个方法，如果不调用否则会丢失回调中的方法

//			if (jimpleClass == null) {
//				// Collect the callback interfaces implemented in the app's
//				// source code
//				jimpleClass = callbackClasses == null ? new DefaultCallbackAnalyzer(config, entrypoints, callbackFile)
//						: new DefaultCallbackAnalyzer(config, entrypoints, callbackClasses);
//				jimpleClass.collectCallbackMethods();
//
//				// Find the user-defined sources in the layout XML files. This
//				// only needs to be done once, but is a Soot phase.
//				lfp.parseLayoutFile(apkFileLocation);
//			} else
//				jimpleClass.collectCallbackMethodsIncremental();

            // Run the soot-based operations
            //run pack
            PackManager.v().getPack("wjpp").apply();//Whole-Jimple Pre-processing Pack
            PackManager.v().getPack("cg").apply();
            PackManager.v().getPack("wjtp").apply();

            // Collect the results of the soot-based phases 回调函数的收集
//			for (Entry<String, Set<SootMethodAndClass>> entry : jimpleClass.getCallbackMethods().entrySet()) {
//				Set<SootMethodAndClass> curCallbacks = this.callbackMethods.get(entry.getKey());
//				if (curCallbacks != null) {
//					if (curCallbacks.addAll(entry.getValue()))
//						hasChanged = true;
//				} else {
//					this.callbackMethods.put(entry.getKey(), new HashSet<>(entry.getValue()));
//					hasChanged = true;
//				}
//			}

//			if (entrypoints.addAll(jimpleClass.getDynamicManifestComponents()))
//				hasChanged = true;
        }

        // Collect the XML-based callback methods

        //collectXmlBasedCallbackMethods(resParser, lfp, jimpleClass);
    }

    public void calculateSourcesSinksEntrypoints(String sourceSinkFile) throws IOException, XmlPullParserException {
        ISourceSinkDefinitionProvider parser = null;// Common interface for all
        // classes that support
        // loading source and sink
        // definitions

        String fileExtension = sourceSinkFile.substring(sourceSinkFile.lastIndexOf("."));
        fileExtension = fileExtension.toLowerCase();

        try {
            if (fileExtension.equals(".xml"))
                parser = XMLSourceSinkParser.fromFile(sourceSinkFile);
            else if (fileExtension.equals(".txt"))
                parser = PermissionMethodParser.fromFile(sourceSinkFile);
            else if (fileExtension.equals(".rifl"))
                parser = new RIFLSourceSinkDefinitionProvider(sourceSinkFile);
            else
                throw new UnsupportedDataTypeException("The Inputfile isn't a .txt or .xml file.");

            calculateSourcesSinksEntrypoints(parser);
        } catch (SAXException ex) {
            throw new IOException("Could not read XML file", ex);
        }
    }

    public void calculateSourcesSinksEntrypoints(ISourceSinkDefinitionProvider sourcesAndSinks)
            throws IOException, XmlPullParserException {
        // To look for callbacks, we need to start somewhere. We use the Android
        // lifecycle methods for this purpose.

        //this.sourceSinkProvider = sourcesAndSinks;
        ProcessManifest processMan = new ProcessManifest(apkFileLocation);// This
        // class
        // provides
        // easy
        // access
        // to
        // all
        // data
        // of
        // an
        // AppManifest.
        // Nodes and attributes of a parsed manifest can be changed. A new byte
        // compressed manifest considering the changes can be generated.
        this.appPackageName = processMan.getPackageName();
        this.entrypoints = processMan.getEntryPointClasses();// Gets all classes the contain entry points in this
        // applications四大组件

        // Parse the resource file

//		long beforeARSC = System.nanoTime();
//		ARSCFileParser resParser = new ARSCFileParser();// Parser for reading out the contents of Android's
//														// resource.arsc file. Structure declarations and comments taken
//														// from the Android source code and ported from C to Java.
//		resParser.parse(apkFileLocation);
//		logger.info("ARSC file parsing took " + (System.nanoTime() - beforeARSC) / 1E9 + " seconds");
//		this.resourcePackages = resParser.getPackages();// Its value is
//														// [soot.jimple.infoflow.android.resources.ARSCFileParser$ResPackage@3e6fa38a]

        // Add the callback methods
        LayoutFileParser lfp = null;// Parser for analyzing the layout XML files inside an android application
        if (config.getEnableCallbacks()) {// Gets whether the taint analysis shall consider callbacks
            if (callbackClasses != null && callbackClasses.isEmpty()) {
                logger.warn("Callback definition file is empty, disabling callbacks");
            } else {
                //lfp = new LayoutFileParser(this.appPackageName, resParser);
                switch (config.getCallbackAnalyzer()) {// Gets the callback analyzer that is being used in preparation
                    // for the taint analysis
                    case Fast:
                        //calculateCallbackMethodsFast(resParser, lfp);
                        calculateCallbackMethodsFast(null, null);
                        break;
                    case Default:
                        calculateCallbackMethods(null, null);//config.getCallbackAnalyzer()==“Defalut”
                        break;
                    default:
                        throw new RuntimeException("Unknown callback analyzer");
                }

                // Some informational output
                //System.out.println("Found " + lfp.getUserControls() + " layout controls");
            }
        }

        System.out.println("Entry point calculation done.");

        // Clean up everything we no longer need
        //	soot.G.reset();

//		// Create the SourceSinkManager
//		{
//			Set<SootMethodAndClass> callbacks = new HashSet<>();
//			for (Set<SootMethodAndClass> methods : this.callbackMethods.values())
//				callbacks.addAll(methods);
//
//			sourceSinkManager = new AccessPathBasedSourceSinkManager(this.sourceSinkProvider.getSources(),
//					this.sourceSinkProvider.getSinks(), callbacks, config.getLayoutMatchingMode(),
//					lfp == null ? null : lfp.getUserControlsByID());// SourceSinkManager for Android applications. This
//																	// class uses precise access path-based source and
//																	// sink definitions.
//
//			sourceSinkManager.setAppPackageName(this.appPackageName);
//			sourceSinkManager.setResourcePackages(this.resourcePackages);
//			sourceSinkManager.setEnableCallbackSources(this.config.getEnableCallbackSources());
//		}

        entryPointCreator = createEntryPointCreator();
    }


    protected void initializeSoot() {

        Config.setSootOptions(apkFileLocation);


    }
}
