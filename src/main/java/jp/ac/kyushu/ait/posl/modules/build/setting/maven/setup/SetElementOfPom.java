package jp.ac.kyushu.ait.posl.modules.build.setting.maven.setup;

import jp.ac.kyushu.ait.posl.utils.exception.NoSureFireException;
import jp.ac.kyushu.ait.posl.utils.log.MyLogger;
import jp.ac.kyushu.ait.posl.utils.setting.inner.Project;
import org.apache.maven.model.Model;

public interface SetElementOfPom {
    MyLogger logger = MyLogger.getInstance();
    Model setUp(Model model, String project) throws NoSureFireException;
    default Model setUpPom(Model model, Project project) throws NoSureFireException {
        model = setUp(model, project.name);
        return model;
    }

}
