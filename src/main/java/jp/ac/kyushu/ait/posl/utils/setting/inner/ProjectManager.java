package jp.ac.kyushu.ait.posl.utils.setting.inner;


import java.util.List;
import java.util.Scanner;

public class ProjectManager {
    private Project project;

    /**
     * This is for handling user input from IDE
     * @param pm
     * @param arg
     */
    public ProjectManager(PropertyManager pm, String arg) {
        String name = arg;
        if(arg==null) {
            name = getProjectFromUserInput(pm);
        } else {
            try {
                int no = Integer.parseInt(name);
                name = getProjectFromNo(pm, no);
            } catch (NumberFormatException e) {
                //OK
            }
        }
        project = pm.loadProjectProperties(name);
    }

    private String getProjectFromUserInput(PropertyManager pm) {
        System.err.println("Please select a project  ");
        List<String> projectList = pm.getProjectList();
        showProjects(projectList);
        int size = projectList.size();
        assert (size!=0);
        Integer option = 0;
        if(size>1){
            option = getUserInput();
        }
        return projectList.get(option);
    }

    private String getProjectFromNo(PropertyManager pm, int no){
        String name;
        List<String> projectList = pm.getProjectList();
        int size = projectList.size();
        assert (size!=0);
        if(size < no){
            return "NoProjectsExist";
        }
        return projectList.get(no);
    }

    private Integer getUserInput() {
        Scanner scan=new Scanner(System.in);
        int option = scan.nextInt();
        return option;
    }

    private void showProjects(List<String> list) {
        int i=0;
        for(String name : list){
            System.err.println(i+" : "+name);
            i++;
        }
    }


    public Project getProject(){
        return project;
    }
    public String getProjectName(){
        return project.name;
    }
    public String getProjectAbb(){
        return project.abb;
    }



}
