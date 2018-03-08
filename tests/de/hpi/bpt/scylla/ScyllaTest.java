package de.hpi.bpt.scylla;

import org.junit.BeforeClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.jupiter.params.ParameterizedTest;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(Parameterized.class)
class ScyllaTest {

    private static Map<String,String> outputPathes = new HashMap();

    private void testSetUp(String folderName, String globalConfigFileName, String bpmnFileName, String simulationFileName, String pathToSavedFile) {
        String outputFolder = Scylla.mainTestAPI(folderName, globalConfigFileName, bpmnFileName, simulationFileName);
        outputPathes.put(pathToSavedFile, outputFolder);
    }

    private String getOutputFolder (String pathToSavedFile, String folder, String globalConfigName, String bpmnFileName, String simulationFileName){
        if (!outputPathes.containsKey(pathToSavedFile)){
            testSetUp(folder, globalConfigName+".xml", bpmnFileName+".bpmn", simulationFileName+".xml", pathToSavedFile);
        }
        return outputPathes.get(pathToSavedFile);
    }

    private String fullPathToSavedFile(String pathToSavedFile){
        return "tests"+Scylla.FILEDELIM+pathToSavedFile+Scylla.FILEDELIM;
    }

    //Does have to run with all pug ins activated
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv")
    void xesTest(String pathToSavedFile, String folder, String globalConfigName, String bpmnFileName, String simulationFileName) {
        pathToSavedFile = fullPathToSavedFile(pathToSavedFile);
        String outputFolder = getOutputFolder(pathToSavedFile, folder, globalConfigName, bpmnFileName, simulationFileName);
        try {
            String savedContent = new String(Files.readAllBytes(Paths.get(pathToSavedFile + bpmnFileName +".xes")));
            String contentToCheck = new String(Files.readAllBytes(Paths.get(outputFolder + Scylla.FILEDELIM + bpmnFileName +".xes")));
            Assertions.assertEquals(savedContent, contentToCheck);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    //Does have to run with all pug ins activated
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv")
    void resourceutilizationTest(String pathToSavedFile, String folder, String globalConfigName, String bpmnFileName, String simulationFileName) {
        pathToSavedFile = fullPathToSavedFile(pathToSavedFile);
        String outputFolder = getOutputFolder(pathToSavedFile, folder, globalConfigName, bpmnFileName, simulationFileName);
        try {
            String savedContent = new String(Files.readAllBytes(Paths.get(pathToSavedFile + globalConfigName +"_resourceutilization.txt")));
            String contentToCheck = new String(Files.readAllBytes(Paths.get(outputFolder + Scylla.FILEDELIM + globalConfigName +"_resourceutilization.txt")));
            Assertions.assertEquals(savedContent, contentToCheck);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    //Does have to run with all pug ins activated
    @ParameterizedTest
    @CsvFileSource(resources = "/test-data.csv")
    void batchregionstatsTest(String pathToSavedFile, String folder, String globalConfigName, String bpmnFileName, String simulationFileName) {
        pathToSavedFile = fullPathToSavedFile(pathToSavedFile);
        String outputFolder = getOutputFolder(pathToSavedFile, folder, globalConfigName, bpmnFileName, simulationFileName);
        try {
            String savedContent = new String(Files.readAllBytes(Paths.get(pathToSavedFile + globalConfigName +"_batchregionstats.txt")));
            String contentToCheck = new String(Files.readAllBytes(Paths.get(outputFolder + Scylla.FILEDELIM + globalConfigName +"_batchregionstats.txt")));
            Assertions.assertEquals(savedContent, contentToCheck);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}