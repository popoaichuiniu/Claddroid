package com.popoaichuiniu.intentGen;

import soot.Value;
import soot.toolkits.scalar.ArraySparseSet;

public class MyArraySparseSet<T> extends ArraySparseSet<T> {//这里设定T一定为Value

    @Override
    public boolean contains(Object obj) {


////        同一个方法中相同字符串值表示相同value  ： 一个变量如$r5会多次赋值，代表不同，已经考虑到如果$r5被复制新值了，重新检查是否和intent相关。
////        对于参数和类属性 比如： 类属性 $r0.intent（$r0=this）  （这种情况暂时不考虑）会不会不同名字表示同一个属性($r4=$r0) $r4.intent 重新赋值不和intent相关 ，结果$r0.intent已经不相关，但是还在集合里
//        for (int i = 0; i < numElements; i++)
//        {
//
//            if (elements[i].toString().equals(obj.toString())) {
//                return true;
//            }
//        }
//
//
//
//
//
//        return false;


        if(obj instanceof Value)
        {
            Value value= (Value) obj;
            for (int i = 0; i < numElements; i++)
            {

                if (value.equivTo(elements[i])) {
                    return true;
                }
            }

            return false;

        }
        else
        {
            throw new RuntimeException("obj is not Value!");

        }

    }

    @Override
    public void remove(Object obj) {
//        for (int i = 0; i < numElements; i++)
//            if (elements[i].toString().equals(obj.toString())) {
//                remove(i);
//                break;
//            }

        if(obj instanceof Value)
        {
            Value value= (Value) obj;
            for (int i = 0; i < numElements; i++)
                if (value.equivTo(elements[i]) ) {
                    remove(i);
                    break;
                }
        }
        else
        {
            throw new RuntimeException("obj is not Value!");
        }


    }
}
