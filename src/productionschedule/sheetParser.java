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
**************************************************************************
 */
package productionschedule;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
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
    
    
    public static void parse(Workbook w, OutputStream out, String encoding, boolean hide, String fileName) throws IOException, ParseException {
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
                    if(row[0].getContents().indexOf(jobNum) == 0){

                        packages.put(row[0], row[1]);
                    }
                }
                sheets.add(packages);
                
            }

        for (int i = 0; i < sheets.size(); i++) {
            System.out.println("For loop");
            Collection c = sheets.get(i).values();
            for(Object o :c){
                System.out.println("For Each Loop");
                System.out.println(o.toString());
            }
            
        }
    }
}
