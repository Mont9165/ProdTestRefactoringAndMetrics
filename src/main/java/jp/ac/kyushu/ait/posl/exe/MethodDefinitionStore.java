package jp.ac.kyushu.ait.posl.exe;

import jp.ac.kyushu.ait.posl.beans.run.Registry;
import jp.ac.kyushu.ait.posl.beans.source.MethodDefinition;
import jp.ac.kyushu.ait.posl.beans.source.db.MethodDefinition4DB;
import jp.ac.kyushu.ait.posl.utils.db.Dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class stores and loads method information
 */
public class MethodDefinitionStore {

    /**
     * Store the list of methods
     */
    public static void store(Registry r, String type,  Collection<MethodDefinition> allMethods){//"X_1"
        List<MethodDefinition4DB> methods = transform(r, type, allMethods);
        Dao<MethodDefinition4DB> dao = new Dao<>();
        dao.init();
        dao.insert(methods);
        dao.close();
    }
    /**
     * Store the list of methods
     */
    public static void update(Registry r, String type,  Collection<MethodDefinition> allMethods){//"X_1"
        List<MethodDefinition4DB> methods = transform(r, type, allMethods);
        Dao<MethodDefinition4DB> dao = new Dao<>();
        dao.init();
        dao.update(methods);
        dao.close();
    }

    private static List<MethodDefinition4DB> transform(Registry registry, String type, Collection<MethodDefinition> allMethods) {
        List<MethodDefinition4DB> methods = new ArrayList<>();
        for(MethodDefinition md: allMethods){
            MethodDefinition4DB methodInfo =transform(registry.project, registry.commitId, type, md);
            methods.add(methodInfo);
        }
        return methods;
    }
    public static MethodDefinition4DB transform(String project, String commitId, String type, MethodDefinition md){
        md.methodInfo.project = project;
        md.methodInfo.commitId = commitId;
        md.methodInfo.type = type;
        md.methodInfo.isInTest = md.isInTest();
        md.methodInfo.isTestCase = md.isTestCase();
        return md.methodInfo;
    }


}
