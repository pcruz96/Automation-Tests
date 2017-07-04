package com.automation.utils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import com.automation.utils.FileFinder;
 
public class FileFinderDemo {
    public static void main(String[] args) throws IOException {
        // create a FileFinder instance with a naming pattern
        FileFinder finder = new FileFinder("*.xml");
 
        // pass the initial directory and the finder to the file tree walker
        Files.walkFileTree(Paths.get("/Users/paulcruz/Downloads"), finder);
 
        // get the matched paths
        Collection<Path> matchedFiles = finder.getMatchedPaths();
 
        // print the matched paths
        for (Path path : matchedFiles) {
            System.out.println(path.getFileName());
        }
    }
}