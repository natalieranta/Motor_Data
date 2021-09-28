 /*  This code reads text from a csv file, in the form of a string, then converts it
 *  into a 2d array of type double. The file contains data on 7 motors including their currents
 * (amps) over a time period (seconds). A text file is written for average currents over time periods
 * where the current is greater than 1.0, and any current exceeding 8.0 is indicated in the text file.*/


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class MotorCurrentReader {

    final static double MAXCURRENT = 8.0; //maximum current for motors
    final static double IDLECURRENT = 1.0; //idle current -- indicates no motor activity
    final static int ROWS = 1000; //number of rows in csv file (constant)
    final static int COLS = 8;    //number of columns in csv file (constant)

    public static double[][] divideString(String inputFile) {
        String[] array1D = new String[ROWS];
        String[][] array2D = new String[ROWS][COLS];
        Path file = Paths.get(inputFile);
        if (!file.toFile().exists()) {
            System.err.println( inputFile + "does not exist!!!");
            return null; //if the file does not exist, returns null value and exits
        }
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            for (int i = 0; i < ROWS; i++) {
                array1D[i] = reader.readLine();
            }
            for (int j = 0; j < ROWS; j++) {
                array2D[j] = array1D[j].split(","); //splits 1D array into 2D at each comma
            }
        }
        catch (IOException err) {
            System.err.println(err.getMessage());
            return null;
        }
        double[][] doubleArray = stringToDouble(array2D); //sends 2d string array to function which converts strings into doubles.
        return doubleArray;
    }// this method reads a csv file and divides it into a 2d array where a new line is a row delimiter and each comma is a column delimiter.

    public static double[][] stringToDouble(String[][] array2D){
        double[][] doubleArray = new double[ROWS][COLS];
        for(int i=0; i < ROWS; i++) {
            for(int j=0; j < COLS; j++) {
                double value = Double.parseDouble(array2D[i][j]);
                doubleArray[i][j] = value;
            }
        }
        return doubleArray;
    } //function converts strings to doubles to more easily analyze data

    public static String outputStringData(double[][] data, int motorNum, String outputStr) {
        int seconds, endTime;
        double current, currentAvg;
        int startTime = 0;
        int i = 0;
        double currentTot = 0;
        int initialOutputLength = outputStr.length();
        while (i < ROWS) {
            current = data[i][motorNum]; //analyzing motor i from 1 through 7
            seconds = (int)data[i][0]; //time data is in first column in file
            if (current > IDLECURRENT) {
                if (startTime == 0) {
                    startTime = seconds; //starting time when a pulse is detected
                    if (current > MAXCURRENT) {
                        outputStr += "***Current Exceeded*** \r\n"; //current > 8
                    }
                }
                currentTot += current; //increment total current values in pulse
            }
            else {
                if (startTime != 0) {
                    endTime = (seconds - 1); //set end time when back to idle current
                    int timeTot = endTime - startTime + 1; //find total time of pulse
                    currentAvg = currentTot / timeTot;
                    currentAvg = (double)Math.round(1000*currentAvg)/1000;
                    outputStr += "\t" + startTime + ",\t\t" + endTime + ",\t\t" + currentAvg + "\r\n"; //find average current over interval and append data to string
                }
                currentTot = 0; //reset current values for next pulse
                startTime = 0; //reset start time values for next pulse
            }
            i++;
        }
        if (initialOutputLength == outputStr.length())
            outputStr += "Not Used\n";//append "Not Used" if no data has been added for a motor -- indicates that it never reached a pulse above idle
        return outputStr;
    }

    public static void saveData(String fileName, String outputData){
        Path file = Paths.get(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            writer.write(outputData);
        }
        catch (IOException err) {
            System.err.println(err.getMessage());
        }
    }//try catch block to save collected data into a text file

    public static void main (String[] args) {
        String dataReport = "Data Report:\r\n\n";
        double[][] totData = divideString("Logger.csv");
        for (int motor = 1 ; motor < COLS; motor++) { //send each motor through Motor OutputString data to analyze current and start and stop times of each pulse.
            String headerString = "Motor: " + motor +"\nstart (sec), finish(sec), current(amps) \r\n";
            dataReport += outputStringData(totData, motor, headerString) + "\r\n";
        }
        saveData("Report.txt", dataReport); //save collected data in text file.
        System.out.println("Data has been saved in txt file.");
    }

}