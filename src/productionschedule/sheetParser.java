/**
 * *******************************************************************
 *
 * Copyright (C) 2002 Andrew Khan
 * 
* This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
* This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
* You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * *************************************************************************
 */
package productionschedule;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

/**
 * Simple demo class which uses the api to present the contents of an excel 97
 * spreadsheet as comma separated values, using a workbook and output stream of
 * your choice
 */
public class sheetParser {

    /**
     * Constructor
     *
     * @param w The workbook to interrogate
     * @param out The output stream to which the CSV values are written
     * @param encoding The encoding used by the output stream. Null or
     * unrecognized values cause the encoding to default to UTF8
     * @param hide Suppresses hidden cells
     * @exception java.io.IOException
     */
    public static ArrayList<Map> sheets = new ArrayList();
    public static ArrayList<ArrayList> rows = new ArrayList();
    public static ArrayList<String> cells = new ArrayList();
    public static String jobNum;
    public static String client;
    public static String jobName;
    public static Date mailDate;
    public static int sheetsToUse = 1;  //number to tell how many sheets to use

    public static Job parse(Workbook w, OutputStream out, String encoding, boolean hide, String fileName) throws IOException, ParseException, ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException {
        DateFormat df = new SimpleDateFormat("MMddyy");
        System.out.println("File Name: " + fileName);
        String[] path = fileName.split("\\\\");
        String[] jobInfo = path[2].split("_");
        jobNum = jobInfo[0];
        client = jobInfo[1];
        jobName = jobInfo[2];
        mailDate = df.parse(jobInfo[3]);
        System.out.println(mailDate);


        if (encoding == null || !encoding.equals("UnicodeBig")) {
            encoding = "UTF8";
        }
        for (int sheet = 0; sheet < w.getNumberOfSheets(); sheet++) {
            rows = new ArrayList();
            Sheet s = w.getSheet(sheet);
            Cell[] row = null;
            Map packages = new HashMap();
            for (int i = 0; i < s.getRows(); i++) {

                row = s.getRow(i);
                String[] val = new String[row.length];
                for (int j = 0; j < row.length; j++) {
                    val[j] = row[j].getContents();
                }
                if (row[0].getContents().indexOf(jobNum) == 0) {

                    packages.put(val[0], val[1]);
                }
            }
            sheets.add(packages);
        }
        for (int i = 0; i < sheets.size(); i++) {
            List<String> keys = new ArrayList<String>(sheets.get(i).keySet());
            for (String key : keys) {
                System.out.println(key + ": " + sheets.get(i).get(key).toString());
            }

        }
        //return sheets;
        ArrayList<Package> packages = new ArrayList<Package>();
        for (int i = 0; i < sheetsToUse; i++) {
            List<String> keys = new ArrayList<String>(sheets.get(i).keySet());
            for (String key : keys) {
                packages.add(createPackage(key, sheets.get(i).get(key).toString()));
            }
        }
        Job j = createJob(packages);
        return j;
    }
    
    public static Package createPackage(String name, String size){
        int sizeNum = 0;
        if(!size.isEmpty()){
            sizeNum = Integer.parseInt(size.replace(",", ""));
        }
        Package pack = new Package(name, mailDate, "inQueue", sizeNum, 1, 0.0, "None", 000000);
        
        
        return pack;
    }
    public static Job createJob(ArrayList<Package> p) throws ClassNotFoundException, SQLException, IllegalArgumentException, IllegalAccessException{
        String user = System.getProperty("user.name");;
        Job jobber = new Job(jobNum, client, jobName, "Approved", user, p);
        return jobber;
    }
}
