package com.popoaichuiniu.intentGen;

import soot.jimple.infoflow.entryPointCreators.AndroidEntryPointCreator;

import java.util.Collection;

public class ICCEntryPointCreator extends AndroidEntryPointCreator {

    public ICCEntryPointCreator(Collection<String> androidClasses) {
        super(androidClasses);
        dummyClassName="IccClass";
        dummyMethodName="iccMain";
    }
}
