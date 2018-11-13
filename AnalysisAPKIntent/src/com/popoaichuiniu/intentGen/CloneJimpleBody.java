package com.popoaichuiniu.intentGen;

import soot.jimple.JimpleBody;

import java.util.HashMap;
import java.util.Map;

public class CloneJimpleBody{


    public static HashMap<JimpleBody,JimpleBody> bodyMap =new HashMap<>();

    public static HashMap<JimpleBody,Map<Object,Object>> bodyInfoMap =new HashMap<>();
    private JimpleBody jimpleBodyP=null;
    private JimpleBody jimpleBodyA=null;
    public Map<Object,Object> bindings=null;
    /** Clones the current body, making deep copies of the contents. */
    private JimpleBody clone(JimpleBody jimpleBody)
    {
        JimpleBody b = new JimpleBody(jimpleBody.getMethod());
        bindings=b.importBodyContentsFrom(jimpleBody);
        return b;
    }

    public CloneJimpleBody(JimpleBody jimpleBodyP) {
        this.jimpleBodyP = jimpleBodyP;
        this.jimpleBodyA=clone(jimpleBodyP);
        bodyMap.put(jimpleBodyP,jimpleBodyA);
        bodyInfoMap.put(jimpleBodyP,bindings);


    }

    public JimpleBody getJimpleBodyP() {
        return jimpleBodyP;
    }

    public JimpleBody getJimpleBodyA() {
        return jimpleBodyA;
    }
}
