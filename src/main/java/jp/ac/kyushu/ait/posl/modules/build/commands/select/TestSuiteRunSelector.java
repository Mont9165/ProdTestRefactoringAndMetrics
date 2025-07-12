package jp.ac.kyushu.ait.posl.modules.build.commands.select;

import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.source.structure.Structure;

import java.util.HashSet;
import java.util.Set;

public class TestSuiteRunSelector implements TestTargetSelector {


    @Override
    public Set<String> getTestTargets(Structure structure) {
        Set<String> set = new HashSet<>();
        set.add(null);
        return set;//class level run
    }

    @Override
    public String getModuleName(Structure structure, String targetName) {
        return null;
    }

    @Override
    public BuildToolSettingController findBuildControllerForModule(BuildToolSettingController mavenSetting, String modName) {
        return mavenSetting;
    }
}
