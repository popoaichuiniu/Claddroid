package com.popoaichuiniu.base;



import soot.jimple.internal.JReturnVoidStmt;




public class ExitJStmt extends JReturnVoidStmt {

    private  String sootMethodSignature=null;

    public ExitJStmt(String sootMethodSignature) {

        this.sootMethodSignature=sootMethodSignature;
    }

    @Override
    public String toString() {
        return sootMethodSignature+"_"+"ZMSExit";
    }


    @Override
    public int hashCode() {
        return sootMethodSignature.hashCode()+"ZMSExit".hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ExitJStmt)
        {
            ExitJStmt objExitStmt= (ExitJStmt) obj;
            if(this.sootMethodSignature.equals(objExitStmt.sootMethodSignature))
            {
                return true;
            }
            else
            {
                return  false;
            }

        }
        else
        {
            return false;
        }
    }
}
