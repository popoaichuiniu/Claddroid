import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.cheng.Element;
import com.cheng.FieldInstrumentation;
import com.cheng.GlobalValue;
import com.cheng.MessageResolver;
import com.cheng.MethodInstrumentation;
import com.cheng.NewInstanceInstrumentation;
import com.cheng.util.Utils;

import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.Transform;
import soot.options.Options;


public class Instrument {
	/**
	 * appPath是应用的绝对路径
	 * platforms是SDK中platforms的路径
	 * msgPath是stadyna输出路径 + '_msg.txt'
	 */
	static String appPath = "/home/cheng/reflection/googleplay/com.macropinch.hydra.android/com.macropinch.hydra.android_3.2.apk";
	static String platforms = "/home/cheng/android-sdk-linux/platforms";
	//static String msgPath = "/home/cheng/stadyna_result" + File.separator + defaultAppPath.substring(defaultAppPath.lastIndexOf(File.separator),defaultAppPath.lastIndexOf(".")) + "_msg.txt";
	static String msgPath = "/home/cheng/reflection/googleplay/com.macropinch.hydra.android/com.macropinch.hydra.android_3.2_msg.txt";
	public static void main(String[] args) {
		
		//preProcess();
		
		String args2[] = {
				"-cp","/home/cheng/android_full-19.jar",
				"-process-dir",appPath,
				"-android-jars",platforms,
				"-ire",
				"-pp",
				"-keep-line-number",
				"-allow-phantom-refs",
				"-w",
				"-p", "cg", "enabled:true"
		};
		//prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_keep_line_number(true);
		//output as APK, too//-f J
		Options.v().set_output_format(Options.output_format_dex);
		
        
        
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.myInstrumenter", new SceneTransformer() {
        	@Override
        	protected void internalTransform(String phaseName, Map<String, String> options){
        		System.out.println("************************************************");
        		
        		List<Element> elements = MessageResolver.fetchElements(msgPath);
        		for(Element element : elements){
        			System.out.println("element.operation="+element.getOperation());
        			switch(element.getOperation()){
        			case GlobalValue.OP_METHOD_INVOKE:
        				MethodInstrumentation methodInstrumentation = new MethodInstrumentation(element);
        				methodInstrumentation.instrument();
        				break;
        			case GlobalValue.OP_FIELD_INVOKE:
        				FieldInstrumentation fieldInstrumentation = new FieldInstrumentation(element);
        				fieldInstrumentation.instrument();
        				break;
        			case GlobalValue.OP_NEW_INSTANCE:
        				NewInstanceInstrumentation newInstanceInstrumentation = new NewInstanceInstrumentation(element);
        				newInstanceInstrumentation.instrument();
        				break;
        			}
        		}
        	}
        }));
		soot.Main.main(args2);
		/*try {
			FileUtils.cleanDirectory(new File("sootOutput"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

    public static void preProcess(){

    	try {
			FileUtils.cleanDirectory(new File("jimple"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String path = "/home/cheng/stadyna_result/source_files";
    	String androidJar = "/home/cheng/android-sdk-linux/platforms/android-25/android.jar";
    	for(String dex : Utils.getDexPaths(path)){
    		GenerateJimple.retargetDex(dex, androidJar, "jimple");
    	}
    	
    }
    
}