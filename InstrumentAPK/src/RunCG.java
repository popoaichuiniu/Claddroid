import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.xmlpull.v1.XmlPullParserException;

import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

public class RunCG {
	public static void main(String[] args) throws IOException, XmlPullParserException{
		
		String name = "/home/cheng/test/origin/workspace_boosted_apps/at.univie.sensorium_15.apk";
		int size = cgSize(name);
		System.out.println(size);
		/*String originPath = "/home/cheng/reflection";
		String droidRAPath = "/home/cheng/workspace/DroidRA/workspace_boosted_apps";
		String mePath = "/home/cheng/workspace/InstrumentAPK/sootOutput";
		
		originPath += File.separator + name;
		droidRAPath += File.separator + name;
		mePath += File.separator + name;
		
		int originSize = cgSize(originPath);
		int droidRASize = cgSize(droidRAPath);
		int meSize = cgSize(mePath);
		
		System.out.println("originSize:" + originSize);
		System.out.println("droidRASize:" + droidRASize);
		System.out.print("meSize:" + meSize);*/
	}
	
	private static int cgSize(String path) throws IOException, XmlPullParserException{
		//String androidPlatformPath = "/home/cheng/android_full.jar";
		String androidPlatformPath = "/home/cheng/android-sdk-linux/platforms/android-25/android.jar";
		SetupApplication app = new SetupApplication
                (androidPlatformPath,
                        path);
        app.calculateSourcesSinksEntrypoints("SourcesAndSinks.txt");
        soot.G.reset();

        Options.v().set_src_prec(Options.src_prec_apk);
        Options.v().set_process_dir(Collections.singletonList(path));
        //Options.v().set_android_jars(androidPlatformPath);
        Options.v().set_force_android_jar(androidPlatformPath);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().setPhaseOption("cg.spark", "on");

        Scene.v().loadNecessaryClasses();

        SootMethod entryPoint = app.getEntryPointCreator().createDummyMain();
        Options.v().set_main_class(entryPoint.getSignature());
        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));
        System.out.println(entryPoint.getActiveBody());

        PackManager.v().runPacks();

        CallGraph appCallGraph = Scene.v().getCallGraph();
        return appCallGraph.size();
	}
}
