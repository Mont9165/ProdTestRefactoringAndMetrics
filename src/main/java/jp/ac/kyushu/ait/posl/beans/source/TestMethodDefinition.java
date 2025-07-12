package jp.ac.kyushu.ait.posl.beans.source;

import gr.uom.java.xmi.UMLAbstractClass;
import gr.uom.java.xmi.UMLClass;
import gr.uom.java.xmi.UMLOperation;

import java.util.List;

/**
 * Method Definition for test code
 */
public class TestMethodDefinition extends MethodDefinition{


    /**
     * When this method has @Test, this variable will be True
     */
    public boolean isTestCase;

    /**
     * Initialize
     *
     * @param umlClass
     * @param umlOperation
     */
    public TestMethodDefinition(UMLAbstractClass umlClass, UMLOperation umlOperation, List<UMLClass> classes, String moduleName) {
        super(umlClass, umlOperation, classes, moduleName);
        isTestCase = isTestCase();
    }

    public TestMethodDefinition(){}

    public boolean isInTest(){
        return true;
    }


}
