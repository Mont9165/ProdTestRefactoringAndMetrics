package jp.ac.kyushu.ait.posl.modules.build.setting.maven;

import jp.ac.kyushu.ait.posl.utils.Utils;

public class MavenSettingControllerStub extends MavenSettingController{

    public MavenSettingControllerStub(String file) {
        super();
        String fileName = Utils.getFileInResources(file);
        this.pomFileAbstractPath = fileName;
        topDirectory=true;
    }
}
