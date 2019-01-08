package com.popoaichuiniu.jacy.statistic;

import java.io.IOException;
import java.util.Collections;

import javax.activation.UnsupportedDataTypeException;

import com.popoaichuiniu.util.Config;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
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

    private void calculateMain(ARSCFileParser resParser, LayoutFileParser lfp) throws IOException {





        initializeSoot();


        entryPointCreator = createEntryPointCreator();
        long beforecreateDummyMain = System.nanoTime();
        //创建虚拟Dummymain class dummy main 方法
        SootMethod entryPoint = entryPointCreator.createDummyMain();
        System.out.println("tttttttttt创建DummyMain时间:" + (System.nanoTime() - beforecreateDummyMain) / 1E9 + "seconds");




        Options.v().set_main_class(entryPoint.getSignature());// 设置整个程序分析的main class
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));// 设置方法的入口点去构建call graph


        // Run the soot-based operations
        PackManager.v().getPack("wjpp").apply();
        PackManager.v().getPack("cg").apply();



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
//		exceptionLogger.info("ARSC file parsing took " + (System.nanoTime() - beforeARSC) / 1E9 + " seconds");
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
                        calculateMain(null, null);//config.getCallbackAnalyzer()==“Defalut”
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


    }


    protected void initializeSoot() {

        Config.setSootOptions(apkFileLocation);


    }
}
