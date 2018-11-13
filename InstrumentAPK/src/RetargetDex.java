import soot.G;
import soot.options.Options;

public class RetargetDex {
	public static void main(String[] args){
		G.reset();
		String androidJars = "/home/cheng/android-sdk-linux/platforms";
		String dexPath = "/home/cheng/AndroidStudioProjects/DeviceId/app/build/outputs/apk/app-debug.apk";
		String[] args2 =
        {
            "-android-jars", androidJars,
            "-process-dir", dexPath,
            "-ire",
			"-pp",
			"-keep-line-number",
			"-allow-phantom-refs",
			"-w",
			"-p", "cg", "enabled:false",
			"-p", "wjtp.rdc", "enabled:true",
			"-src-prec", "apk"
        };
			
		Options.v().set_output_format(Options.output_format_jimple);
		soot.Main.main(args2);
		
		G.reset();
	}
}
