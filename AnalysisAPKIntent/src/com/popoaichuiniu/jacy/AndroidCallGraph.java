package com.popoaichuiniu.jacy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.Buffer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.log4j.FileAppender;
import org.apache.log4j.PatternLayout;
import org.xmlpull.v1.XmlPullParserException;

import soot.PackManager;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.infoflow.android.SetupApplication;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

public class AndroidCallGraph {


    private org.apache.log4j.Logger appLogger = null;// 应用日志


    public AndroidCallGraph(String appPath, String androidPlatformPath){

        this.appPath = appPath;
        appLogger = org.apache.log4j.Logger.getLogger(appPath);
        String logfilePath = "app-exceptionLogger/androidCallgraph/" + appPath.replaceAll("/|\\.", "_") + "_callgraph.txt";

        File temp = new File(logfilePath);
        if (temp.exists()) {
            temp.delete();
        }


        try {
            appLogger.addAppender(new FileAppender(new PatternLayout("%d %p [%t] %C.%M(%L) | %m%n"), logfilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }


        this.androidPlatformPath = androidPlatformPath;

    }

    public CallGraph getCg() {
        return cg;
    }

    public SetupApplication getApp() {
        return app;
    }

    private String appPath = null;
    private String androidPlatformPath = null;

    private CallGraph cg = null;

    private CGExporter cge = new CGExporter();

    private SootMethod entryPoint = null;

    private MySetupApplication app = null;

    public SootMethod getEntryPoint() {
        return entryPoint;
    }

    public void caculateAndroidCallGraph() throws IOException, XmlPullParserException {
        app = new MySetupApplication(androidPlatformPath, appPath);

        if (app == null)
            return;


            // 计算source,sink和入口点
        app.calculateMyEntrypoints("SourcesAndSinks.txt");


        //initializeSoot();//已经在calculateSourcesSinksEntrypoints初始化过了
        // Build the callgraph
        Logger logger = Logger.getLogger("constructCG");
        long beforeCallgraph = System.nanoTime();
        constructCallgraph();
        logger.info("Callgraph construction took " + (System.nanoTime() - beforeCallgraph) / 1E9 + " seconds");


    }

    private void constructCallgraph() {

        long beforecreateDummyMain = System.nanoTime();
        entryPoint = app.getEntryPointCreator().createDummyMain();

        System.out.println("tttttttttt创建DummyMain时间:" + (System.nanoTime() - beforecreateDummyMain) / 1E9 + "seconds");

        // 获得APK的函数入口点

        Options.v().set_main_class(entryPoint.getSignature());// 设置整个程序分析的main class

        Scene.v().setEntryPoints(Collections.singletonList(entryPoint));// 设置方法的入口点去构建call graph

        System.out.println(entryPoint.getActiveBody());

        PackManager.v().runPacks();

        cg = Scene.v().getCallGraph();

        // 可视化函数调用图
        //visit();

        // 导出函数调用图


//		cge.exportMIG("./AnalysisAPKIntent/cg_gexf_output/"+appPath.replaceAll("/|\\.", "_"), ".");
//		System.out.println("导出函数调用图为:" + appPath.replaceAll("/|\\.", "_") + ".gexf");


    }

//	private void initializeSoot() {
//
//		soot.G.reset();// 标准的soot操作，清空soot之前所有操作遗留下的缓存值
//
//		Options.v().set_src_prec(Options.src_prec_apk);// 设置优先处理的文件格式
//
//		Options.v().set_process_dir(Collections.singletonList(appPath));// 处理文件夹中所有的class
//		// singletonList(T) 方法用于返回一个只包含指定对象的不可变列表
//
//		Options.v().set_android_jars(androidPlatformPath);// 在该路径下找android.jar
//
//		Options.v().set_app(true);//
//
//		List<String> excludeList = new LinkedList<String>();
//		excludeList.add("java.*");
//		excludeList.add("sun.misc.*");
//		excludeList.add("android.*");
//		excludeList.add("org.apache.*");
//		excludeList.add("soot.*");
//		excludeList.add("javax.servlet.*");
//		Options.v().set_exclude(excludeList);
//		Options.v().set_no_bodies_for_excluded(true);//去除的类不加载body
//
//		Options.v().set_whole_program(true);// 以全局模式运行，这个默认是关闭的，否则不能构建cg(cg是全局的pack)
//
//		Options.v().set_allow_phantom_refs(true);// 允许未被解析的类，可能导致错误
//
//		//Phantom classes are classes that are neither in the process directory nor on
////		the Soot classpath, but that are referenced by some class / method body that
////		Soot loads. If phantom classes are enabled, Soot will not just abort and
////		fail on such an unresolvable reference, but create an empty stub called a
////		phantom class which in turn contains phanom methods to make up for the
////		missing bits and pieces.
//
//
//		//设置cg pack的选项
//
//		Options.v().setPhaseOption("cg.cha", "on");//不用设置的默认就为true
//
//		Options.v().setPhaseOption("cg.cha", "verbose:true");
//
//		Options.v().setPhaseOption("cg.cha", "apponly:true");
//
//		Options.v().setPhaseOption("cg.spark", "off");// 构建cg的选项，spark是一个指向性分析框架 这个打开的会可能会消除一些 节点
//
//		Scene.v().loadNecessaryClasses();// 加载soot需要的classes，包括命令行指定的
//										// 使用soot反编译dex文件，并将反编译后的文件加载到内存中
//
//	}

    // 可视化函数调用图的函数
    private void visit() {

        HashSet<String> visited = new HashSet<String>();
        BufferedWriter cgOutputWriter = null;//cg

        try {

            cgOutputWriter = new BufferedWriter(new FileWriter(new File("./AnalysisAPKIntent/CallGraphOutput/" + appPath.replaceAll("/|\\.", "_") + "_cfg" + ".txt")));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            appLogger.error(appPath, e);
        }

        // *******************************************************//
        System.out.println("*******CallGraph*******" + cg.size());
        for (Iterator<soot.jimple.toolkits.callgraph.Edge> iterator = cg.iterator(); iterator.hasNext(); ) {
            soot.jimple.toolkits.callgraph.Edge edge = iterator.next();
            System.out.println(
                    edge.getSrc().method().getBytecodeSignature() + "——>" + edge.getTgt().method().getBytecodeSignature());
            try {
                cgOutputWriter.write(edge.getSrc().method().getBytecodeSignature() + "——>" + edge.getTgt().method().getSignature() + "\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                appLogger.error(appPath, e);
            }
            if (!visited.contains(edge.getSrc().method().getSignature())) {
                cge.createNode(edge.getSrc().method().getSignature());
                visited.add(edge.getSrc().method().getSignature());
            }
            if (!visited.contains(edge.getTgt().method().getSignature())) {
                cge.createNode(edge.getTgt().method().getSignature());
                visited.add(edge.getTgt().method().getSignature());
            }
            cge.linkNodeByID(edge.getSrc().method().getSignature(), edge.getTgt().method().getSignature());
            System.out.println("**************");


        }
        try {
            cgOutputWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            appLogger.error(appPath, e);
        }

        // *******************************************************//
    }
}
