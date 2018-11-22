package com.popoaichuiniu.test;

import com.popoaichuiniu.jacy.statistic.AndroidCallGraph;
import soot.Scene;
import soot.SootClass;
import soot.util.Chain;

public class TestConfig {
    public static String androidPlatformPath = "/home/zms/platforms";

    // private static String appDir = "./DroidBench/apk";
    // private static String appDir = "./MyApp";
    // private static String appDir =
    // "/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/apks_wandoujia/apks";
    private static String appDir = "./AnalysisAPKIntent/sms2.apk";
    public static void main(String [] args)
    {
        AndroidCallGraph androidCallGraph =new AndroidCallGraph(appDir,androidPlatformPath);

//        Chain<SootClass> libraryClasses = Scene.v().getLibraryClasses();
//        for (SootClass libClass : libraryClasses) {
//            System.out.println(libClass.getName());
//
//        }

//        Chain<SootClass> applicationClasses=Scene.v().getApplicationClasses();
//        for (SootClass appClass : applicationClasses) {
//            System.out.println(appClass.getName());
//
//        }

        Chain<SootClass> classes=Scene.v().getClasses();
        for (SootClass cls : classes) {
            System.out.println(cls.getName());

        }
        System.out.println();

    }
}
