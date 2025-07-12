package jp.ac.kyushu.ait.posl.stub;

import jp.ac.kyushu.ait.posl.beans.commit.Commit;
import jp.ac.kyushu.ait.posl.modules.source.execution_trace.impl.selogger.SeLogger;
import jp.ac.kyushu.ait.posl.modules.test.JunitTestResultManager;
import jp.ac.kyushu.ait.posl.modules.build.BuildRunner;
import jp.ac.kyushu.ait.posl.modules.build.setting.maven.MavenSettingController;
import jp.ac.kyushu.ait.posl.modules.git.GitController;
import jp.ac.kyushu.ait.posl.modules.source.structure.Structure;
import jp.ac.kyushu.ait.posl.modules.source.structure.StructureScanner;
import jp.ac.kyushu.ait.posl.utils.setting.SettingManager;

public abstract class SpecifierStub {
        public Commit commit;
        public SettingManager sm;
        public GitController gitX;
        public BuildRunner buildRunner;
        public JunitTestResultManager result;
        public StructureScanner analyzer;
        public Structure structure;

        
        public SpecifierStub(String project, String commitId) throws Exception{
            this(project, commitId, true);
         }
         public SpecifierStub(String project, String commitId, boolean run)throws Exception{
            initSettingManager(project);
             gitX = new GitController(sm, "/main/");
             MavenSettingController mc = new MavenSettingController(gitX);
             mc.checkout(commitId);
             mc.readBuildFile();
             commit = gitX.getCommit();//next build
             commit.project = sm.getProject().name;
             if(run){
                 buildRunner = new BuildRunner(0, new SeLogger(mc));
                 buildRunner.deploy();
                 for (String target: buildRunner.getTargets()){
                     buildRunner.run(target);
                 }
                 result = buildRunner.getResults();
                 analyzer = new StructureScanner(mc);
                 structure = analyzer.scan();
             }else{
                 MavenSettingController maven = new MavenSettingController(gitX);
                 maven.checkout(commitId);
                 maven.readBuildFile();
                 analyzer = new StructureScanner(maven);
                 structure = analyzer.scan();
             }

         }

    protected abstract void initSettingManager(String project);


}
