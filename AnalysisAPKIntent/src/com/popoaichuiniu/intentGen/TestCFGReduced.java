package com.popoaichuiniu.intentGen;

public class TestCFGReduced {//没有使用
//    private static Set<Pair<Integer, String>> targets = new LinkedHashSet<Pair<Integer, String>>();
//
//    private static BufferedWriter bufferedWriter = null;
//
//    public static void main(String[] args) {
//        String appDir = "/media/lab418/4579cb84-2b61-4be5-a222-bdee682af51b/myExperiment/idea_ApkIntentAnalysis/sootOutput";
//        File appDirFile = new File(appDir);
//        try {
//            bufferedWriter = new BufferedWriter(new FileWriter("ifReducedSituation.txt"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        if (appDirFile.isDirectory()) {
//
//
//            List<String> hasAnalysisAPP = null;
//
//            try {
//
//                BufferedReader hasAnalysisAPPBufferedReader = new BufferedReader(new FileReader("hasSatisticIfReducedAndPreviousIF.txt"));
//                hasAnalysisAPP = new ArrayList<>();
//                String line = null;
//                while ((line = hasAnalysisAPPBufferedReader.readLine()) != null) {
//                    hasAnalysisAPP.add(line);
//                }
//
//                hasAnalysisAPPBufferedReader.close();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            if (hasAnalysisAPP == null) {
//                return;
//            }
//
//            BufferedWriter hasAnalysisAPPBufferedWriter = null;
//            try {
//                hasAnalysisAPPBufferedWriter = new BufferedWriter(new FileWriter("hasSatisticIfReducedAndPreviousIF.txt", true));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            for (File file : appDirFile.listFiles()) {
//                if (file.getName().endsWith("_signed_zipalign.apk")) {
//
//                    if (hasAnalysisAPP.contains(file.getAbsolutePath())) {
//                        continue;
//                    }
//                    System.out.println(file.getAbsolutePath());
//
//                    Thread childThread = new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//
//
//                            singleAPPAnalysis(file.getAbsolutePath());
//                            try {
//                                bufferedWriter.flush();
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//
//                        }
//
//                    });
//
//                    childThread.start();
//
//                    try {
//                        childThread.join();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        hasAnalysisAPPBufferedWriter.write(file.getAbsolutePath() + "\n");
//                        hasAnalysisAPPBufferedWriter.flush();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//
//
//
//                }
//
//
//            }
//
//        }
//        try {
//            bufferedWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    private static void singleAPPAnalysis(String appPath) {
//        BufferedReader bufferedReader = null;
//        try {
//            bufferedReader = new BufferedReader(new FileReader(new File(appPath.replace("_signed_zipalign.apk",".apk") + "_" + "UnitsNeedAnalysis.txt")));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String line = null;
//        try {
//            while ((line = bufferedReader.readLine()) != null) {
//                String[] strs = line.split("#");
//                String sooMethodString = strs[0];
//                String byteCode = strs[1];
//                int intByteCode = Integer.parseInt(byteCode);
//                String unitString = strs[2];
//                targets.add(new Pair<>(intByteCode, sooMethodString));
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            bufferedReader.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        AndroidCallGraph androidCallGraph = new AndroidCallGraph(appPath, Config.androidJar);
//        AndroidInfo androidInfo = new AndroidInfo(appPath);
//
//
//        List<SootMethod> ea_entryPoints = Util.getEA_EntryPoints(androidCallGraph, androidInfo);
//
//        List<SootMethod> roMethods = Util.getMethodsInReverseTopologicalOrder(ea_entryPoints, androidCallGraph.getCg());
//
//        for (SootMethod sootMethod : roMethods) {
//            analysisSootMethod(sootMethod, androidCallGraph.getCg());
//        }
//    }
//
//    private static void analysisSootMethod(SootMethod sootMethod, CallGraph cg) {
//
//
//        BriefUnitGraph briefUnitGraph = new BriefUnitGraph(sootMethod.getActiveBody());
//
//        IntentFlowAnalysis intentFlowAnalysis = new IntentFlowAnalysis(briefUnitGraph);
////        MyUnitGraph myUnitGraph = Util.reducedCFG(sootMethod, intentFlowAnalysis, unit);
////        int countIfPrevious = 0;
////        for (Unit unit : sootMethod.getActiveBody().getUnits()) {
////
////            if (briefUnitGraph.getSuccsOf(unit).size() >= 2) {
////                countIfPrevious++;
////            }
////        }
////
////        int countReduced = 0;
////        for (Unit unit : sootMethod.getActiveBody().getUnits()) {
////
////            List<Unit> successors = myUnitGraph.getSuccsOf(unit);
////            if (successors != null && successors.size() >= 2) {
////                countReduced++;
////            }
////        }
////
////        try {
////            bufferedWriter.write(countIfPrevious + "****************************************" + countReduced +"&&&&&&&&&&&&&&&&&&&&&&&"+(countIfPrevious-countReduced) +"\n");
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        System.out.println(countIfPrevious + "****************************************" + countReduced+"&&&&&&&&&&&&&&&&&&&&&&&"+(countIfPrevious-countReduced) );
//
//
//    }
}
