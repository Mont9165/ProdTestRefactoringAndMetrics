package jp.ac.kyushu.ait.posl.stub;

import jp.ac.kyushu.ait.posl.modules.build.commands.select.MethodRunSelector;
import jp.ac.kyushu.ait.posl.modules.source.structure.Structure;

import java.util.HashSet;
import java.util.Set;

public class SelectorMethodStub extends MethodRunSelector {
    private Set<String> targets;
    public SelectorMethodStub(String... targets){
        this.targets = new HashSet<>();
        for(String target: targets){
            this.setTarget(target);
        }
    }

    private void setTarget(String target) {
        targets.add(target);
    }


    @Override
    public Set<String> getTestTargets(Structure structure) {
        super.getTestTargets(structure);
        return this.targets;//method level run
    }

}
