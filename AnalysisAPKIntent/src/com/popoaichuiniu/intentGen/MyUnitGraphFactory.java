package com.popoaichuiniu.intentGen;

import org.apache.log4j.Logger;
import soot.Body;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;

import java.util.HashMap;
import java.util.Map;

public class MyUnitGraphFactory {

    private static String appPath=null;


    private static MyUnitGraph curMyUnitGraph=null;

    private static Map<Body,Map<Unit,MyUnitGraph>> myBodyUnitMap =new HashMap<>();


    public  static  MyUnitGraph getMyUnitGraph(String appPath, Body body, Unit targetUnit, Logger logger, UnitGraph ug, SingleSootMethodIntentFlowAnalysis intentFlowAnalysis, SimpleLocalDefs defs)
    {

        if (MyUnitGraphFactory.appPath==null)
        {
            MyUnitGraphFactory.appPath=appPath;
        }
        else if(!MyUnitGraphFactory.appPath.equals(appPath))// 新的apk开始分析
        {
            MyUnitGraphFactory.appPath=appPath;
            myBodyUnitMap.clear();
        }

        Map<Unit,MyUnitGraph> myUnitMap= myBodyUnitMap.get(body);
        if(myUnitMap==null)
        {
            myUnitMap=new HashMap<>();
        }

        MyUnitGraph myUnitGraph=myUnitMap.get(targetUnit);
        if(myUnitGraph==null)
        {
            myUnitGraph=new MyUnitGraph(body,targetUnit,appPath,logger,ug,intentFlowAnalysis,defs);
            myUnitMap.put(targetUnit,myUnitGraph);
        }

        curMyUnitGraph=myUnitGraph;
        //myBodyUnitMap.put(body,myUnitMap);//保存太多容易outofMemmory

        return myUnitGraph;



    }

    public static MyUnitGraph getCurMyUnitGraph() {
        if(curMyUnitGraph==null)
        {
            throw new RuntimeException("逻辑出现问题，需检查！");
        }
        return curMyUnitGraph;
    }
}
