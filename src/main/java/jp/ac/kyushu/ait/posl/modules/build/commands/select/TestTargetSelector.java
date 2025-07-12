package jp.ac.kyushu.ait.posl.modules.build.commands.select;

import jp.ac.kyushu.ait.posl.modules.build.setting.BuildToolSettingController;
import jp.ac.kyushu.ait.posl.modules.source.structure.Structure;

import java.util.Set;

public interface TestTargetSelector {
    Set<String> getTestTargets(Structure structure);

    String getModuleName(Structure structure, String targetName);
    BuildToolSettingController findBuildControllerForModule(BuildToolSettingController bc, String target);
}
