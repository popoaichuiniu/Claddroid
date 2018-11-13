import soot.G;
import soot.options.Options;

public class GenerateJimple {
	public static void main(String[] args){
		
		String dexPath = "/home/cheng/comicsreader.apk";
		String androidJar = "/home/cheng/android-sdk-linux/platforms/android-25/android.jar";
		String outputDir = "jimple";
		retargetDex(dexPath,androidJar,outputDir);
		
	}
	
	public static void retargetDex(String dexPath, String androidJar, String outputDir) 
	{
		G.reset();
		
		String[] args2 =
        {
            "-force-android-jar", androidJar,
            "-process-dir", dexPath,
            "-d", outputDir,
            "-ire",
			"-pp",
			"-keep-line-number",
			"-allow-phantom-refs",
			"-w",
			"-p", "cg", "enabled:false",
			"-p", "wjtp.rdc", "enabled:true",
			"-src-prec", "apk"
        };
			
		//Options.v().set_output_format(Options.output_format_class);
		Options.v().set_output_format(Options.output_format_jimple);
		soot.Main.main(args2);
		
		G.reset();
	}
	
	static void transformApkToJimple(){
		String[] apkArguments = {"-src-prec","apk",
				"-process-dir","sootOutput/Reflection_Reflection13.apk",
				"--keep-line-number",
				//"-allow-phantom-refs",
				"-f","J",
				//"-cp","/home/cheng/tools/jdk1.8.0_111/jre/lib/rt.jar",
				"-android-jars","/home/cheng/android-sdk-linux/platforms"};
		
		soot.Main.main(apkArguments);
	}
	static void npAnalysis(){
		String[] arguments = {"-f","J",
				"-xml-attributes",
				"-p","jap.npcolorer",
				"on","Hello",
				"-soot-class-path","/home/cheng/test:/home/cheng/tools/jdk1.8.0_111/jre/lib/rt.jar"};
		soot.Main.main(arguments);
	}
}
