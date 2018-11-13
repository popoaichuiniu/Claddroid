package com.popoaichuiniu.intentGen;


import org.junit.Test;

import java.util.*;


public class Intent {

    public Set<IntentExtraKey> myExtras = new HashSet<>();
    public String action;
    public String targetComponent;
    public Set<String> categories = new LinkedHashSet<String>();

    public Intent(Intent intent) {
        if (intent.myExtras != null) {
            this.myExtras = new HashSet<>(intent.myExtras);
        } else {
            this.myExtras = null;
        }

        if (intent.action != null) {
            this.action = new String(intent.action);
        } else {
            this.action = null;
        }

        if (intent.targetComponent != null) {
            this.targetComponent = new String(intent.targetComponent);
        } else {
            this.targetComponent = null;
        }

        if (this.categories != null) {
            this.categories = new LinkedHashSet<>(intent.categories);
        } else {
            this.categories = null;
        }

        initialIntentExtraValueSet();
    }

    public Intent() {
        super();
        initialIntentExtraValueSet();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Intent intent = (Intent) o;

        if (!equivExtraTo(myExtras, intent.myExtras)) return false;
        if (action != null ? !action.equals(intent.action) : intent.action != null) return false;
        if (targetComponent != null ? !targetComponent.equals(intent.targetComponent) : intent.targetComponent != null)
            return false;
        return equivCategoryTo(categories, intent.categories);

    }

    public boolean equivExtraTo(Set<IntentExtraKey> set1, Set<IntentExtraKey> set2)//不能用两个set比，因为IntentExtraKey对象比不看value值
    {
        if (set1 == null && set2 != null || set1 != null && set2 == null) {
            return false;
        }

        if (set1 == null && set2 == null) {
            return true;
        }


        if (set1.size() != set2.size()) {
            return false;
        }

        Set<IntentExtraValue> sum = new HashSet<>();

        for (IntentExtraKey intentExtraKey1 : set1) {
            sum.add(new IntentExtraValue(intentExtraKey1));
        }
        for (IntentExtraKey intentExtraKey2 : set2) {
            sum.add(new IntentExtraValue(intentExtraKey2));
        }

        if (sum.size() != set1.size()) {
            return false;
        } else {
            return true;
        }


    }

    public boolean equivCategoryTo(Set<String> cateSet1, Set<String> cateSet2) {
        if (cateSet1 == null && cateSet2 != null || cateSet1 != null && cateSet2 == null) {
            return false;
        }

        if (cateSet1 == null && cateSet2 == null) {
            return true;
        }

        if (cateSet1.size() != cateSet2.size()) {
            return false;
        }

        return cateSet1.containsAll(cateSet2);

    }

    @Override
    public int hashCode() {
        int result = myExtras != null ? myExtras.hashCode() : 0;
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + (targetComponent != null ? targetComponent.hashCode() : 0);
        result = 31 * result + (categories != null ? categories.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Intent{" +
                "extras=" + myExtras +
                ", action='" + action + '\'' +
                ", targetComponent='" + targetComponent + '\'' +
                ", categories=" + categories +
                '}';
    }


    private Set<IntentExtraValue> intentExtraValueSet = null;//使用前需更新一下

    public void initialIntentExtraValueSet() {
        intentExtraValueSet=new HashSet<>();
        for (IntentExtraKey intentExtraKey : myExtras) {
            intentExtraValueSet.add(new IntentExtraValue(intentExtraKey));
        }

    }

    public Set<IntentExtraValue> getIntentExtraValueSet() {
        initialIntentExtraValueSet();
        return intentExtraValueSet;
    }

    public boolean contains(Intent intent)//这个intent的所有属性值this都有
    {
        if(targetComponent!=null &&(!targetComponent.equals(intent.targetComponent)))
        {
            return false;
        }

        if (intent.action != null && (!intent.action.equals(action)))//如果intent.action不为null的话，this.action必须等于其
        {
            return false;
        }

        if(!this.intentExtraValueSet.containsAll(intent.getIntentExtraValueSet()))//注意要使用getIntentExtraValueSet()
        {
            return false;

        }

        if(!categories.containsAll(intent.categories))//包含全部的categories
        {
            return false;
        }

        return true;


    }


}





