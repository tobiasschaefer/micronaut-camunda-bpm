package info.novatec.micronaut.camunda.bpm.feature;

import io.micronaut.inject.ast.ClassElement;
import io.micronaut.inject.visitor.TypeElementVisitor;
import io.micronaut.inject.visitor.VisitorContext;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class ResourceScanVisitor implements TypeElementVisitor<ResourceScan, Object> {

    private static final String RESOURCES_DIR = "src/main/resources";

    private static ArrayList<String> resourceFiles = new ArrayList<>();

    private static ArrayList<String> subDirs = new ArrayList<>();

    @Override
    public void visitClass(ClassElement element, VisitorContext context) {
        //TODO: Versuchen @ResourceScan in Factory zu annotieren  --> Prio 1
        //TODO: Gradle Demon Problem

        Optional<Path> projectDir = context.getProjectDir();

        //get all subdirectories from resources
        determineSubdirectories(Paths.get(projectDir.get().toString(), RESOURCES_DIR).toFile());

        //Get all models in "resource" folder
        findResourceFiles(Paths.get(projectDir.get().toString(), RESOURCES_DIR).toFile());

        //Get all models in subdirectories of "resource" folder
        for(String path : subDirs){
            findResourceFiles(Paths.get(path).toFile());
        }

        //Converting to String[] to be able to pass it to the annotation
        String[] resourceArray = new String[resourceFiles.size()];
        resourceArray = resourceFiles.toArray(resourceArray);

        // make resourceArray effectivly final
        String[] finalResourceArray = resourceArray;

        //pass modelnames to annotation
        element.annotate(ResourceScan.class, resourceScanAnnotationValueBuilder -> resourceScanAnnotationValueBuilder.member("models", finalResourceArray));

    }

    private ArrayList<String> findResourceFiles(File folder){
        StringBuilder prefix = new StringBuilder();

        //pattern vlt. noch falsch für linux?
        String pattern = "(\\w)+\\.(bpmn|cmmn|dmn)";

        if (folder.exists()) {
            File[] files = folder.listFiles();

            for( File file : files){
                if (file.getName().matches(pattern)) {
                    if(getParentName(file).matches("resources")){ //if models saved in resources --> add modelname to resourceFiles
                        resourceFiles.add(file.getName());
                    }else{ //if not, get the parent directory / directories and prepend them
                        StringBuilder subdirprefix = generatePrefix(file, prefix);

                        //remove "/" from prefix
                        subdirprefix.deleteCharAt(0);

                        //format: exampledir1/eampledir2/helloworld.bpmn
                        resourceFiles.add(subdirprefix + "/" + file.getName());
                    }

                }

            }
        }
        return resourceFiles;
    }

    //checks if parent of file is "resources", if not add prefix
    private StringBuilder generatePrefix(File file, StringBuilder prefix) {
        File parent = file.getParentFile();

        if(!(parent.getName().equals("resources"))) {
            prefix.insert(0, "/" + parent.getName());
        }

        if (!(parent.getName().equals("resources"))) {
            generatePrefix(parent, prefix);
        }

        return prefix;


    }

    private String getParentName(File file){
        return file.getParentFile().getName();
    }


    //scanning all files and determine if directory or not, if so --> check if this directory has other directories (recusive)
    private void determineSubdirectories(File folder){
        File[] files = folder.listFiles();

        for(File file : files){
            if (file.isDirectory()){
                subDirs.add(file.getPath());
                determineSubdirectories(file);
            }
        }
    }



}



