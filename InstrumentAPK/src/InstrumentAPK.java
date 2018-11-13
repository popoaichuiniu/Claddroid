import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.Value;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.options.Options;


public class InstrumentAPK {
	private final static String METHOD_INVOKE_FORMAT = "<java.lang.reflect.Method: java.lang.Object invoke(java.lang.Object,java.lang.Object[])>";
	public static void main(String[] args) {
		
		String args2[] = {
				"-android-jars","/home/cheng/android-sdk-linux/platforms",
				"-process-dir","/home/cheng/tools/dynamicdex.apk",
				"-process-dir","/home/cheng/workspace/InstrumentAPK/sootOutput"
		};
		//prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_apk);
		//output as APK, too//-f J
		Options.v().set_output_format(Options.output_format_dex);
		
        // resolve the PrintStream and System soot-classes
		Scene.v().addBasicClass("java.io.PrintStream",SootClass.SIGNATURES);
        Scene.v().addBasicClass("java.lang.System",SootClass.SIGNATURES);

        PackManager.v().getPack("jtp").add(new Transform("jtp.myInstrumenter", new BodyTransformer() {

			@Override
			protected void internalTransform(final Body b, String phaseName, @SuppressWarnings("rawtypes") Map options) {/*
				final PatchingChain<Unit> units = b.getUnits();
				//important to use snapshotIterator here
				for(Iterator<Unit> iter = units.snapshotIterator(); iter.hasNext();) {
					final Unit u = iter.next();
					u.apply(new AbstractStmtSwitch() {
						public void caseAssignStmt(AssignStmt stmt){
							String uString = u.toString();
							if(uString.contains(METHOD_INVOKE_FORMAT)){
								Local versionRef = addVersionRef(b);
								
								//insert versionRef = new Version();
								units.insertBefore(Jimple.v().newAssignStmt(
											versionRef, Jimple.v().newNewExpr(RefType.v("com.cheng.dynamicdex.Version"))), u);
								System.err.println(uString);
								SootMethod toCall = Scene.v().getSootClass("com.cheng.dynamicdex.Version").getMethod("java.lang.String getVersion()");
								
								//insert versionRef.getVersion();
								units.insertBefore(Jimple.v().newInvokeStmt(
					                      Jimple.v().newVirtualInvokeExpr(versionRef, toCall.makeRef(), Collections.<Value>emptyList())), u);
								b.validate();
								
							}
							
						}
					});
				}
			*/
				SootClass cls = Scene.v().getSootClass("android.telephony.android.telephony.TelephonyManager");
			}


		}));
		
		soot.Main.main(args2);
	}

    private static Local addTmpRef(Body body)
    {
        Local tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
        body.getLocals().add(tmpRef);
        return tmpRef;
    }
    
    private static Local addVersionRef(Body body){
    	Local versionRef = Jimple.v().newLocal("tmpRef", RefType.v("com.cheng.dynamicdex.Version"));
    	body.getLocals().add(versionRef);
    	return versionRef;
    }
    
    private static Local addTmpString(Body body)
    {
        Local tmpString = Jimple.v().newLocal("tmpString", RefType.v("java.lang.String")); 
        body.getLocals().add(tmpString);
        return tmpString;
    }
}