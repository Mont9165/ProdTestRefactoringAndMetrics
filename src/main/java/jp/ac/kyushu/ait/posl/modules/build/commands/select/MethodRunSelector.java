package jp.ac.kyushu.ait.posl.modules.build.commands.select;

import jp.ac.kyushu.ait.posl.beans.source.MethodDefinition;
import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.source.structure.Structure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MethodRunSelector implements TestTargetSelector {
    Map<String, String> modules;
    public MethodRunSelector(){
        modules = new HashMap<>();
    }

    @Override
    public Set<String> getTestTargets(Structure structure) {
        Set<String> signatures = new HashSet<>();
        for (MethodDefinition md: structure.getALLTestMethods()){
            if (!md.isTestCase()) continue;
            String sig = md.methodInfo.packageName+"."+md.methodInfo.className+"#"+md.getMethodName();
            if(md.methodInfo.packageName.equals("")){
                sig = md.methodInfo.className+"#"+md.getMethodName();
            }
            signatures.add(sig);
            modules.put(sig, md.getModuleName());
        }

        return signatures;//method level run
    }

    @Override
    public String getModuleName(Structure structure, String targetName) {
        return modules.get(targetName);
    }

    @Override
    public BuildToolSettingController findBuildControllerForModule(BuildToolSettingController mavenSetting, String modName) {
        if(modName!=null){
            return mavenSetting.getModules().get(modName);
        }else{
            return mavenSetting;
        }
    }
}
