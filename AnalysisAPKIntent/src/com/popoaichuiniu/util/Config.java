package com.popoaichuiniu.util;

import soot.Main;
import soot.Scene;
import soot.options.Options;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Config {


    public static String androidJar = "platforms";//public  static  String androidJar="/home/zms/platforms";



    public static String fDroidAPPDir = "/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/sootOutput";

    public static String wandoijiaAPP = "/media/mobile/myExperiment/apps/apks_wandoujia/apks/all_app";
    public static String selectAPP = "/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/selectAPP";

    public static String big_app_set = "/home/zms/benign_apps/2016";
    public static String Z3_RUNTIME_SPECS_DIR = "Z3_RUNTIME_SPECS_DIR";

    public static String selectAPP2 = "/home/zms/selectAPP2";

    public static String huaweiApp = "/home/zms/huaweiApp";
    public static String huaweiAppSelect = "/home/zms/huaweiAPPSelect";

    public static String xiaomiApp = "/home/zms/xiaomiAPP";
    public static String xiaomiAppSelect = "/home/zms/xiaomiAPPSelect";

    public static String repeatExperiment="repeat_experiment";

    public static String defaultAppPath = "android_project/Camera/TestWebView2/app/build/outputs/apk/debug/app-debug.apk";

    public static String defaultApkDir = "android_project/Camera/TestWebView2/app/build/outputs/apk/debug";

    public static String defaultAppDirPath=Config.wandoijiaAPP;


    //log path






    public static String logDir = "/home/zms/logger_file";

    public static String intent_file_path=logDir+"/"+"intent_file";

    public static String intent_ulti_path=logDir+"/"+"intent_ulti";

    public static String intentConditionSymbolicExcutationResults = logDir+"/"+"intentConditionSymbolicExcutationResults";

    public static String unitNeedAnalysisGenerate = "AnalysisAPKIntent/unitNeedAnalysisGenerate";


    public static boolean isTest = false;

    // public  static  String defaultAppPath="/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/AnalysisAPKIntent/万花筒之旅一宝宝巴士.apk";
    public static void setSootOptions(String appPath) {
        soot.G.reset();// 标准的soot操作，清空soot之前所有操作遗留下的缓存值

        Options.v().set_src_prec(Options.src_prec_apk);// 设置优先处理的文件格式


        //Options.v().set_android_jars(androidPlatformPath);// 在该路径下找android.jar

        //////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////

        Options.v().set_app(false);//app false：整个app都是application class （app开发者代码和第三方库（包括android support 库））,其他都是library class


//		Options.v().set_app(true);//
//
//		List<String> excludeList = new LinkedList<String>();
//		excludeList.add("java.*");
//		excludeList.add("sun.misc.*");//app false也是可以设置这个的
//		excludeList.add("android.*");
//		excludeList.add("org.apache.*");
//		excludeList.add("soot.*");
//		excludeList.add("javax.servlet.*");
//		Options.v().set_exclude(excludeList);//这个底下也有，那个sootConfig.setSootOptions(Options.v());
//
//		List<String> includeList = new LinkedList<String>();
//		includeList.add("android.support.*");//不添加的话会有android.support.* 某类的getActiveBody为空的错误，
//		//为什么android.*不会呢？是应为classpath有android.jar吗？测试了，好像也不行。
//		Options.v().set_include(includeList);


        ////////////////////////////////////////////////////////////////////////////////不太懂

        List<String> excludeList = new LinkedList<String>();
//        excludeList.add("java.*");
//        excludeList.add("sun.misc.*");
//        excludeList.add("android.*");//app false，整个apk都是application class  app中不包含android.jar ,还有java
        //excludeList.add("android.support.*");//不加载support
//        excludeList.add("org.apache.*");
//        excludeList.add("soot.*");
//        excludeList.add("javax.servlet.*");
//        excludeList.add("org.javatuples.*");
//		Set<String> libraryList=AndroidInfo.getThirdLibraryPackageNameSet();不去除第三方包，因为其可能也有目标API调用，然后其实那些没有目标API的第三方调用不会出现在最后的简约路径图中。
//		for(String packageName:libraryList)
//		{
//			excludeList.add(packageName+".*");
//		}
        Options.v().set_exclude(excludeList);//这样就不会有这些方法的内部的边。这些方法作为library函数
//
//
//
        Options.v().set_no_bodies_for_excluded(true);//这里的值和excludeList.add("android.*"); app false还有 soot classpath决定是否加载某个类，对于a.foo()方法如果a是phantom class，则调用者没有到foo方法的边。


//		Hi John,
//
//
//
//		If the package names are freemarker.*, it’s important to put the asterisk into the exclusion option on the Soot command line as well. Otherwise, Soot will assume that there is a single class called “freemarker” which you would like to exclude.
//
//
//
//		If your code under analysis references a class which is not on the Soot classpath and –allow-phantom-refs is used, this class will automatically be considered as a phantom. Nothing will be loaded, because Soot cannot know where to load it from. If you exclude a class, its structure (method signatures, etc.) will be loaded, but it will be marked as a library class. If you specify –no-bodies-for-excluded, Soot will not load any bodies for those classes, even if some code tries to access the body. Therefore, not putting the class on the classpath is the most rigorous option for guaranteeing that it will not be loaded.
//
//
//
//				Your analysis then needs to deal with such cases. If you have a call to a.foo() and the type of the variable “a” refers to a phantom class, there will not be a callgraph edge from the call site to anywhere and thus there will not be an IFDS call edge. There will, however, still be an IFDS call-to-return edge in which you can detect that you are dealing with such an exclusion case and apply e.g., an external library model to make up for the information loss. Depending on your analysis, you might also be able to simply ignore the effects of the method call on your data flow abstraction, that’s something you as the analysis designer need to decide.
//
//
//
//		Best regards,
//
//				Steven




        Options.v().set_allow_phantom_refs(true);//不设置会报错


        Options.v().set_whole_program(true);// 以全局模式运行，这个默认是关闭的，否则不能构建cg(cg是全局的pack)


        //Phantom classes are classes that are neither in the process directory nor on
//		the Soot classpath, but that are referenced by some class / method body that
//		Soot loads. If phantom classes are enabled, Soot will not just abort and
//		fail on such an unresolvable reference, but create an empty stub called a
//		phantom class which in turn contains phanom methods to make up for the
//		missing bits and pieces.


        //设置cg pack的选项

        Options.v().setPhaseOption("cg.cha", "on");//不用设置的默认就为true

        Options.v().setPhaseOption("cg.cha", "verbose:true");

        Options.v().setPhaseOption("cg.cha", "apponly:true");

        //Options.v().setPhaseOption("cg.spark", "off");// 默认为fasle 构建cg的选项，spark是一个指向性分析框架 这个打开的会可能会消除一些 节点


        Options.v().set_output_format(Options.output_format_none);

        Options.v().set_process_dir(Collections.singletonList(appPath));// 处理文件夹中所有的class
        // singletonList(T) 方法用于返回一个只包含指定对象的不可变列表


        Options.v().set_android_jars(androidJar);


        // Set the Soot configuration options. Note that this will needs to be
        // done before we compute the classpath.


        Options.v().set_keep_line_number(true);

        Options.v().set_coffi(true);

        Options.v().set_keep_offset(true);


        //Options.v().set_soot_classpath(apkFileLocation+ File.pathSeparator+"/home/zms/platforms/android-27/android.jar");
        Options.v().set_soot_classpath(Scene.v().getAndroidJarPath(androidJar, appPath));//+":/media/softdata/AndroidSDKdirectory/extras/android/support"

        Main.v().autoSetOptions();

        // Load whetever we need
        Scene.v().loadNecessaryClasses();

    }
}
