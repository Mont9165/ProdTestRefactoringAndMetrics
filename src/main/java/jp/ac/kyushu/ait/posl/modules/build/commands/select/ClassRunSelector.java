package jp.ac.kyushu.ait.posl.modules.build.commands.select;

import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.source.structure.Structure;

import java.util.Set;

public class ClassRunSelector implements TestTargetSelector {


    @Override
    public Set<String> getTestTargets(Structure structure) {
        return structure.getTestFiles();//class level run
    }

    @Override
    public String getModuleName(Structure structure, String targetName) {
        return structure.getModuleNameByClassName(targetName);
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
