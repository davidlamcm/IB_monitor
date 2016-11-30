package sample;

import java.io.*;
import java.util.Map;
import java.util.Properties;
/**
 * Created by David on 11/1/2016.
 */
public class RiskMonitorProperties {
    InputStream inputStream;
    String result;

    public String getProperty(String propertyName, String filePath) throws IOException {
        try {
            Properties prop = new Properties();
            String propFileName = filePath + "\\config\\config.properties";
            inputStream = new FileInputStream(propFileName);
            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");


            }
            result = prop.getProperty(propertyName);

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            inputStream.close();
        }
        return result;
    }
    public String setProperty(Map<String, String> propertySet, String filePath) throws IOException {
        try {
            Properties prop = new Properties();
            for (Map.Entry<String, String> entry : propertySet.entrySet())
            {
                if( entry.getValue()!=null) {
                    prop.setProperty(entry.getKey(), entry.getValue());
                }else{
                    prop.setProperty(entry.getKey(), "");
                }
            }
            new File(filePath+"\\config").mkdirs(); //check if directory exist and create one if not
            File f = new File(filePath +"\\config\\config.properties");
            OutputStream out = new FileOutputStream( f );
            prop.store(out,"Here store all the settings for risk monitor");
            out.close();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
        return result;
    }
}
