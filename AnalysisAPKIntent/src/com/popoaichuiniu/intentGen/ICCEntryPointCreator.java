package com.popoaichuiniu.intentGen;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;
import soot.util.Chain;

import java.util.Collection;

public class ICCEntryPointCreator extends AndroidEntryPointCreator {

    public ICCEntryPointCreator(Collection<String> androidClasses, String name) {
        super(androidClasses);
        dummyClassName = "IccClass" + name;
        dummyMethodName = "iccMain";

    }

    @Override
    public SootMethod createDummyMain() {

        if (Scene.v().containsClass(dummyClassName)) {

            SootClass sootClass = Scene.v().getSootClass(dummyClassName);

            SootMethod sootMethod = sootClass.getMethodByName(dummyMethodName);
            if (sootMethod != null) {
                return sootMethod;
            }


        }
        //楼上不要也可以不要，BaseEntryCreator做了处理，但是这样节省时间。
        // If we already have a main class, we need to make sure to use a fresh
        // method name
        return super.createDummyMain();
    }
}
