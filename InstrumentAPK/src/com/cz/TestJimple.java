package com.cz;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import soot.Body;
import soot.Main;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.jimple.IfStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;
import soot.util.Chain;

public class TestJimple {

	static String appPath = "/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/InstrumentAPK/codeProcessPath";
	

	public static void main(String[] args) {
		
		
		// TODO Auto-generated method stub

		soot.G.reset();// 标准的soot操作，清空soot之前所有操作遗留下的缓存值
		Options.v().set_app(true);//
		List<String> excludeList = new LinkedList<String>();
		excludeList.add("java.*");

		excludeList.add("soot.*");

		Options.v().set_exclude(excludeList);

		// prefer Android APK files// -src-prec apk
		Options.v().set_src_prec(Options.src_prec_java);

		Options.v().set_no_bodies_for_excluded(true);// 去除的类不加载body

		Options.v().set_whole_program(true);// 以全局模式运行，这个默认是关闭的，否则不能构建cg(cg是全局的pack)

		Options.v().set_allow_phantom_refs(true);// 允许未被解析的类，可能导致错误

		
		// output as APK, too//-f J
		Options.v().set_output_format(Options.output_format_J);
		
//		Options.v().setPhaseOption("jb", "use-original-names:true");		
//		Options.v().set_keep_line_number(true);
		
		
		Options.v().set_process_dir(Collections.singletonList(appPath));
		Main.v().autoSetOptions();
		
		
		//设置cg pack的选项		
		
				Options.v().setPhaseOption("cg.cha", "on");//不用设置的默认就为true
				
				Options.v().setPhaseOption("cg.cha", "verbose:true");
				
				Options.v().setPhaseOption("cg.cha", "apponly:true");
		
		
		Options.v().set_force_overwrite(true);
		
		// Load whetever we need
		Scene.v().loadNecessaryClasses();
		
		PackManager.v().runPacks();
		PackManager.v().writeOutput();
		
		CallGraph cGraph=Scene.v().getCallGraph();
		List<SootMethod> entryPoints=Scene.v().getEntryPoints();
//		Chain<SootClass> classes=Scene.v().getClasses();
//		for(SootClass sootClassTemp: classes)
//		{
//			//System.out.println(sootClassTemp.toString()+"       "+sootClassTemp.isPhantom());
//			if(sootClassTemp.toString().contains("Test"))
//			{
//				System.out.println(sootClassTemp.toString()+"       "+sootClassTemp.isConcrete());
//				
//				System.out.println(sootClassTemp.getMethodByName("main"));
//				
//				System.out.println(sootClassTemp.resolvingLevel());
//				
//				SootMethod sootMethod=sootClassTemp.getMethodByName("xxx");
//				sootMethod.getActiveBody();
//			}
//		}
		assert entryPoints.size()==1;
		SootMethod eMethod=entryPoints.get(0);
		//Body body_error=eMethod.getActiveBody();///why??? error
//		Body body=eMethod.retrieveActiveBody();
//		
//		PatchingChain<Unit> units=body.getUnits();
//		
//		for(Unit unit:units)
//		{
//			if(unit instanceof IfStmt)
//			{
//				IfStmt ifStmt=(IfStmt) unit;
//				System.out.println(ifStmt.getTarget());
//			}
//			
//			//System.out.println(unit.toString());
//		}
		
		SootMethod oMethod=cGraph.edgesOutOf(eMethod).next().getTgt().method();
		
		System.out.println(oMethod.getSignature());
		
		

	}

}
