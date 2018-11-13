import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Map;

import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.options.Options;

public class APKTransformer extends SceneTransformer{

	@Override
	protected void internalTransform(String phaseName, Map<String, String> options) {
		// TODO Auto-generated method stub
		SootClass sootClass = Scene.v().getSootClass("com.google.android.ads.zxxz.b");
		try{
			PrintStream ps = new PrintStream("test.txt");
			for(SootMethod method: sootClass.getMethods()){
				ps.println(method.getSignature());
			}
		}catch(Exception e){
			
		}
		
	}
	
	public static void main(String[] args){
		//prefer Android APK files// -src-prec apk
				Options.v().set_src_prec(Options.src_prec_apk);
				
				//output as APK, too//-f J
				Options.v().set_output_format(Options.output_format_none);
		PackManager.v().getPack("wjtp").add(new Transform("wjtp.myInstrumenter",new APKTransformer()));
		String[] args1 = {
				"-cp","/home/cheng/android_full-19.jar",
				"-android-jars","/home/cheng/android-sdk-linux/platforms",
				"-process-dir","/home/cheng/work/dataset/googleplay/dataset/com.macropinch.hydra.android_3.2.apk",
				"-ire",
				"-pp",
				"-keep-line-number",
				"-allow-phantom-refs",
				"-w",
				"-p", "cg", "enabled:false",
				"-p", "wjtp.rdc", "enabled:true",
				"-src-prec", "apk"
		};
		soot.Main.main(args1);
	}
}
