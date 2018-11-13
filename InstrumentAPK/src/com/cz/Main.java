package com.cz;

/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.  
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */


//package ashes.examples.createclass;

import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAddExpr;
import soot.options.Options;
import soot.util.*;
import java.io.*;
import java.util.*;


/** Example of using Soot to create a classfile from scratch.
 * The 'createclass' example creates a HelloWorld class file using Soot.
 * It proceeds as follows:
 *
 * - Create a SootClass <code>HelloWorld</code> extending java.lang.Object.
 *
 * - Create a 'main' method and add it to the class.
 *
 * - Create an empty JimpleBody and add it to the 'main' method.
 *
 * - Add locals and statements to JimpleBody.
 *
 * - Write the result out to a class file.
 */

public class Main
{
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        SootClass sClass;
        SootMethod method;
        SootMethod method2;
        // Resolve dependencies
           Scene.v().loadClassAndSupport("java.lang.Object");
           Scene.v().loadClassAndSupport("java.lang.System");
           
        // Declare 'public class HelloWorld'   
           sClass = new SootClass("HelloWorld", Modifier.PUBLIC);
        
        // 'extends Object'
           sClass.setSuperclass(Scene.v().getSootClass("java.lang.Object"));
           Scene.v().addClass(sClass);
           
        // Create the method, public static void main(String[])
           method = new SootMethod("main",
                Arrays.asList(new Type[] {ArrayType.v(RefType.v("java.lang.String"), 1)}),
                VoidType.v(), Modifier.PUBLIC | Modifier.STATIC);
        
           sClass.addMethod(method);
           
           method2 = new SootMethod("insert", Arrays.asList(new Type[] { IntType.v() }), BooleanType.v(), Modifier.PUBLIC);
           sClass.addMethod(method2);
           JimpleBody body2 = Jimple.v().newBody(method2);
           method2.setActiveBody(body2);
        // Create the method body
        {
            // create empty body
            JimpleBody body = Jimple.v().newBody(method);
            
            method.setActiveBody(body);
            
            Chain units = body.getUnits();
            Local arg, tmpRef, a, b;
            
            // Add some locals, java.lang.String l0
                arg = Jimple.v().newLocal("l0", ArrayType.v(RefType.v("java.lang.String"), 1));
                body.getLocals().add(arg);
            
            // Add locals, java.io.printStream tmpRef
                tmpRef = Jimple.v().newLocal("tmpRef", RefType.v("java.io.PrintStream"));
                body.getLocals().add(tmpRef);
                b = Jimple.v().newLocal("test", IntType.v());
                AddExpr addExpr = Jimple.v().newAddExpr(b, IntConstant.v(10));
               units.add(Jimple.v().newAssignStmt(b, addExpr));
                
            // add "l0 = @parameter0"
                units.add(Jimple.v().newIdentityStmt(arg, 
                     Jimple.v().newParameterRef(ArrayType.v(RefType.v("java.lang.String"), 1), 0)));
            
            // add "tmpRef = java.lang.System.out"
                units.add(Jimple.v().newAssignStmt(tmpRef, Jimple.v().newStaticFieldRef(
                    Scene.v().getField("<java.lang.System: java.io.PrintStream out>").makeRef())));
            
            // insert "tmpRef.println("Hello world!")"
            {
                SootMethod toCall = Scene.v().getMethod("<java.io.PrintStream: void println(java.lang.String)>");
                units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(tmpRef, toCall.makeRef(), StringConstant.v("Hello world!"))));
            }                        
            
            // insert "return"
                units.add(Jimple.v().newReturnVoidStmt());
                //units.add(Jimple.v().new);
        }
        List<SootMethod> methods = sClass.getMethods();
        SootMethod sootMethod = methods.get(0);
        
        Body body = sootMethod.getActiveBody();
        //body.getUnits().get(0);
        //body.getAllUnitBoxes()
        //Body body = method.getActiveBody();
        ValueBox valueBox = body.getDefBoxes().get(0);
        int line = valueBox.getJavaSourceStartLineNumber();
        System.out.println(body.getUnits().getFirst()+"--"+ line);
        //body.getUnits()
        PatchingChain<Unit> units = body.getUnits();
        units.forEach(it->{
        	System.out.println(it.toString());
        });
       //JimpleBody body = Jimple.v().newBody(method);
       
       //method.setActiveBody(body);
//        String fileName = SourceLocator.v().getFileNameFor(sClass, Options.output_format_class);
//        OutputStream streamOut = new JasminOutputStream(
//                                    new FileOutputStream(fileName));
//        PrintWriter writerOut = new PrintWriter(
//                                    new OutputStreamWriter(streamOut));
//        JasminClass jasminClass = new soot.jimple.JasminClass(sClass);
//        jasminClass.print(writerOut);
//        writerOut.flush();
//        streamOut.close();
        String fileName = SourceLocator.v().getFileNameFor(sClass, Options.output_format_jimple);
        OutputStream streamOut = new FileOutputStream(fileName);
        PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
        Printer.v().printTo(sClass, writerOut);
        writerOut.flush();
        streamOut.close();
    }
        
}
