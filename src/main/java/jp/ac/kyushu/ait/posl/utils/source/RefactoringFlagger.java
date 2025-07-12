package jp.ac.kyushu.ait.posl.utils.source;

import jp.ac.kyushu.ait.posl.beans.source.MethodDefinition;
import gr.uom.java.xmi.diff.CodeRange;
import jp.ac.kyushu.ait.posl.modules.source.structure.Structure;
import org.refactoringminer.api.Refactoring;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RefactoringFlagger {


    /**
     * assign refactoring edits information on methods
     * @param refactorings
     */
    public static void setRefactoring(List<Refactoring> refactorings, Structure structure, boolean isX_1) {
        for (Refactoring rf : refactorings) {
            boolean isFieldRefactoring = isFieldRefactoring(rf);
            if(isX_1) {
                List<CodeRange> left = rf.leftSide();
                setRefactoring(structure, rf, left);
                if(isFieldRefactoring){
                    setFieldRefactoring(structure, rf, left);
                }
            }else{
                List<CodeRange> right = rf.rightSide();
                setRefactoring(structure, rf, right);
                if(isFieldRefactoring){
                    setFieldRefactoring(structure, rf, right);
                }
            }
        }
    }

    /**
     * assign a refactoring edit information on methods
     * @param rf
     * @param codeRanges
     */
    protected static void setRefactoring(Structure structure, Refactoring rf, List<CodeRange> codeRanges) {
        for (CodeRange codeRange : codeRanges) {
            for (int lineNo = codeRange.getStartLine(); lineNo <= codeRange.getEndLine(); lineNo++) {
                MethodDefinition mdX_1 = structure.getMethod(codeRange.getFilePath(), lineNo);
                if (mdX_1 != null) {
                    Set<Refactoring> rf2List = mdX_1.inherentRefactorings.get(lineNo);
                    rf2List.add(rf);
                    mdX_1.inherentRefactorings.put(lineNo, rf2List);
                    structure.updateMethod(mdX_1);
                }
            }
        }
    }

    static Set<String> fieldRefactorings= new HashSet<String>(
            Arrays.asList("Move Attribute", "Pull Up Attribute","Push Down Attribute", "Rename Attribute",
                    "Move And Rename Attribute", "Replace Variable With Attribute", "Replace Attribute (With Attribute)",
                    "Merge Attribute", "Split Attribute", "Change Attribute Type", "Extract Attribute", "Add Attribute Annotation",
                    "Remove Attribute Annotation", "Modify Attribute Annotation"));

    /**
     * check field refactoring
     * @param rf
     * @return
     */
    private static boolean isFieldRefactoring(Refactoring rf) {
        String dispName = rf.getRefactoringType().getDisplayName();
        return fieldRefactorings.contains(dispName);
    }

    /**
     * flag field refactoring in the method that use the refactored fields
     * @param refactoringType
     * @param codeRanges
     */
    public static void setFieldRefactoring(Structure structure, Refactoring refactoringType, List<CodeRange> codeRanges) {
        for(CodeRange r: codeRanges){
            String filePath = r.getFilePath();
            Integer lineNo = r.getStartLine();
            Set<String> fieldTypeFullPath = structure.getFields(filePath, lineNo);
            for(MethodDefinition md: structure.getAllMethods()){
                md.setFieldRefactoring(refactoringType, fieldTypeFullPath);
            }
        }
    }
}
