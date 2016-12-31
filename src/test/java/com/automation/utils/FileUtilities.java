package com.automation.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

public class FileUtilities extends Log4J{

    public boolean isDownloadFileExists(String targetFilePath) {
        try{
           File file = new File(targetFilePath);
            if(!file.exists()) {
              System.out.println("Downloading failed");
            } else {
                if (file.length() < 0) {
                    logger.error("Error in downloading file.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return true;
    }

    public boolean deleteExistsFile(String targetFilePath) {
        try {
            File file = new File(targetFilePath);
            if (file.exists()) {
                file.delete();
                logger.info(file.getName() + " is deleted.");
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return true;
    }
    
    public void createFile(String newfile) {
		try {
			File file = new File(newfile);

			if (file.createNewFile()) {
				logger.info(file.getName() + " file is created.");
			} else {
				logger.info(file.getName() + " file already exists.");
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
    
    public static void copyFile( File from, File to ) throws IOException {
        Files.copy( from.toPath(), to.toPath() );
    }
    
    public String scanFiles(String folderPath, String searchString) throws FileNotFoundException, IOException {
        File folder = new File(folderPath);

        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (!file.isDirectory()) {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String content = "";
                    try {
                        StringBuilder sb = new StringBuilder();
                        String line = br.readLine();

                        while (line != null) {
                            sb.append(line);
                            sb.append(System.lineSeparator());
                            line = br.readLine();
                        }
                        content = sb.toString();


                    } finally {
                        br.close();
                    }
                    if (content.contains(searchString)) {                    	
                        return file.getName().toString();
                    }
                }
            }
        } else {
            System.out.println("Not a Directory!");
        }
        return null;
    }
    
    public void deleteFile(String folderPath, String filenameContains) throws FileNotFoundException, IOException {
        File folder = new File(folderPath);

        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (!file.isDirectory()) {
                    if (file.getName().contains(filenameContains)) {                    	
                        file.delete();
                    }
                }
            }
        } else {
            System.out.println("Not a Directory!");
        }
    }
    
    public File getFileName(String directoryName, String filenameContains){
        File directory = new File(directoryName);
        //get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList){
        	String filename = file.getName();
            if (file.isFile() && filename.contains(filenameContains)){
                return file;
            }
        }
        return null;
    }
    
	public boolean stringInFile(String str, String file) throws FileNotFoundException {
		File f = new File(file);
		Scanner scanner = new Scanner(f);

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();

			if (line.contains(str)) {
				return true;
			}
		}
		return false;
	}
}
