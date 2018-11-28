package com.popoaichuiniu.intentGen;

import soot.Unit;
import soot.jimple.JimpleBody;

import java.util.HashMap;
import java.util.Map;

class BodyMapping {
    JimpleBody jimpleBodyP = null;
    JimpleBody jimpleBodyA = null;
    Map<Object, Object> bindings = null;

}

public class CloneJimpleBodyFactory {

    /**
     * Clones the current body, making deep copies of the contents.
     */
    private static BodyMapping clone(JimpleBody jimpleBody) {
        BodyMapping bodyMapping = new BodyMapping();
        bodyMapping.jimpleBodyP = jimpleBody;
        JimpleBody b = new JimpleBody(jimpleBody.getMethod());
        bodyMapping.jimpleBodyA = b;
        bodyMapping.bindings = b.importBodyContentsFrom(jimpleBody);

        return bodyMapping;
    }

    public static BodyMapping cloneJimpleBody(JimpleBody jimpleBodyP) {



            BodyMapping bodyMapping = clone(jimpleBodyP);
            return bodyMapping;








    }


}
