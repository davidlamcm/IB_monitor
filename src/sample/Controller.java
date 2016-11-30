package sample;

import com.ib.client.EClientSocket;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Controller implements Initializable {
    String lastAccountUpdate ="";
    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    IBWrapper ibwrapper ;
    EClientSocket ibClient ;
    RiskMonitorProperties properties; // for storing setting
    Thread riskMonitorThread;
    ObservableList<String> accountList =  FXCollections.observableArrayList();
    Logger logger = Logger.getLogger("IBRiskMonitorLog");
    FileHandler fh;
    String path = System.getProperty("user.dir");

    public static String accountSelected;
    @FXML
    private TextField loginPort;
    @FXML
    private TextField loginHost;
    @FXML
    private TextField loginClientID;
    @FXML
    private TextArea log_ib;
    @FXML
    private Button login_ib;
    @FXML
    private Button logout_ib;
    @FXML
    private Button startRiskMonitor_ib;
    @FXML
    private Button stopRiskMonitor_ib;
    @FXML
    private ComboBox<String> account_ib;
    @FXML
    private Label aum_ib;
    @FXML
    private Label long_ib;
    @FXML
    private Label short_ib;
    @FXML
    private TableView<IBPosition> posTable;
    @FXML
    private TableColumn<IBPosition, Integer> posConID_ib;
    @FXML
    private TableColumn<IBPosition, String> posSymbol_ib;
    @FXML
    private TableColumn<IBPosition, String> posExch_ib;
    @FXML
    private TableColumn<IBPosition, String> posType_ib;
    @FXML
    private TableColumn<IBPosition, Integer> posPosition_ib;
    @FXML
    private TableColumn<IBPosition, Double> posMarketValue_ib;
    @FXML
    private TextField logFileDir_ib;
    @FXML
    private TextField emailList_ib;
    @FXML
    private TextField warningThreshold_ib;
    @FXML
    private TextField hardThreshold_ib;
    @FXML
    private ComboBox<String> monitorAccount_ib;
    @FXML
    private TextField programToBeKilled_ib;
    @FXML
    private TextField emailAccount_ib;
    @FXML
    private PasswordField emailPassword_ib;
    @FXML
    private Button testKill_ib;

    @FXML
    private TextField updateInterval_ib;
    @FXML
    private TextField drawdownPeriod_ib;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        properties = new RiskMonitorProperties();
        try {
            emailList_ib.setText(properties.getProperty("mailList",path));
            logFileDir_ib.setText(properties.getProperty("logFileDir",path));
            warningThreshold_ib.setText(properties.getProperty("warningThreshold",path));
            hardThreshold_ib.setText(properties.getProperty("hardThreshold",path));
            monitorAccount_ib.setValue(properties.getProperty("monitoringAccount",path));
            programToBeKilled_ib.setText(properties.getProperty("programToBeKilled",path));
            account_ib.setItems(accountList);
            emailAccount_ib.setText(properties.getProperty("emailAccount",path));
            emailPassword_ib.setText(properties.getProperty("emailPassword",path));
            monitorAccount_ib.setItems(accountList);
            updateInterval_ib.setText(properties.getProperty("updateInterval",path));
            drawdownPeriod_ib.setText(properties.getProperty("drawdownPeriod",path));

        } catch (Exception e) {
            updateLog("unable to retrieve setting");
        }
        try {
            // set up logger to log all record in updatelog
            String logpath;
            Calendar cal = Calendar.getInstance();
            DateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String timeStamp = df.format(cal.getTime());
            if(logFileDir_ib.getText() ==null || logFileDir_ib.getText().equals("")){


                new File(path+"\\log").mkdirs(); //check if directory exist and create one if not
                logpath = path + "\\log\\RiskMonitor"+timeStamp+".log";
                logFileDir_ib.setText(path + "\\log");
            }else{
                logpath = logFileDir_ib.getText() + "\\RiskMonitor"+timeStamp+".log";
                new File(logFileDir_ib.getText()).mkdirs(); //check if directory exist and create one if not
            }
            fh = new FileHandler(logpath );
            logger.addHandler(fh);
            SimpleFormatter logFormatter = new SimpleFormatter();
            fh.setFormatter(logFormatter);
            updateLog("saving log to:  "+ logpath);
        }catch(Exception e){
            System.out.println(e );
            updateLog("log file is not working, please check if the path if specified correctly and run again");
        }
    }
    @FXML
    void login_ib(ActionEvent event) {
        Calendar cal = Calendar.getInstance();
        updateLog("trying to login");
        ibClient.eConnect(loginHost.getText(), Integer.parseInt(loginPort.getText()), Integer.parseInt(loginClientID.getText()));
        updateLog("connected: " + ibClient.isConnected());
        ibClient.reqAccountSummary(0, "All", "NetLiquidation"); //for updating account list
        if(ibClient.isConnected()){
            //add shutdownhook to logout when terminate
           // java.lang.Runtime.addShutdownHook(riskMonitorThread);


        }
}

    @FXML
    void logout_ib(ActionEvent event) {
        updateLog("trying to logout");
        ibClient.eDisconnect();
        updateLog("connected: " + ibClient.isConnected());
    }

    @FXML
    void stop() {
        updateLog("trying to logout");
        ibClient.eDisconnect();
        updateLog("connected: " + ibClient.isConnected());
    }


    @FXML
    public void updateLog(String str){
        Calendar cal = Calendar.getInstance();
        String msg = dateFormat.format(cal.getTime()) + " - " + str;
        log_ib.appendText(msg+ "\n");
        logger.info(msg);
    }

    @FXML
    void updateAccountList(String account) {
        accountList.add(account);
        //account_ib.getItems().setAll(ibwrapper.accountList.keySet());
        //monitorAccount_ib.getItems().setAll(ibwrapper.accountList.keySet());
    }
    @FXML
    void registerPortfolioUpdate(ActionEvent event) { //show the list of account
        System.out.println("registerPortfolioUpdate");
        updateLog("registerPortfolioUpdate");
        accountSelected = account_ib.getValue();
        if(accountSelected != null){
            aum_ib.setText(ibwrapper.accountList.get(accountSelected).getAUM());
            ibClient.reqAccountUpdates(true,accountSelected);
        }
        if(accountSelected !=null) {

            posTable.setItems(ibwrapper.accountList.get(accountSelected).positionListForTableView);
            posConID_ib.setCellValueFactory(cellData -> cellData.getValue().conIDProperty().asObject());
            posSymbol_ib.setCellValueFactory(cellData -> cellData.getValue().symbolProperty());
            posExch_ib.setCellValueFactory(cellData -> cellData.getValue().exchangeProperty());
            posType_ib.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
            posPosition_ib.setCellValueFactory(cellData -> cellData.getValue().positionProperty().asObject());
            posMarketValue_ib.setCellValueFactory(cellData -> cellData.getValue().mktValueProperty().asObject());
        }
    }


    public void setIBWrapper(IBWrapper ibwrapper){ //called by main to get reference
        this.ibwrapper = ibwrapper;
    }
    public void setEClientSocket(EClientSocket ibClient){ //called by main to get reference
        this.ibClient = ibClient;
    }
    @FXML
    void updateAccount(String accountName){
        if(accountName.equals(accountSelected) ){
        aum_ib.setText(ibwrapper.accountList.get(accountSelected).getAUM());
        long_ib.setText(ibwrapper.accountList.get(accountSelected).getLong());
        short_ib.setText(ibwrapper.accountList.get(accountSelected).getShort());
        }
    }
    @FXML
    void saveSetting(){
        HashMap<String, String> propertySet = new HashMap<>();
        propertySet.put("mailList", emailList_ib.getText());
        propertySet.put("logFileDir", logFileDir_ib.getText());
        propertySet.put("monitoringAccount", monitorAccount_ib.getValue());
        propertySet.put("warningThreshold", warningThreshold_ib.getText());
        propertySet.put("hardThreshold", hardThreshold_ib.getText());
        propertySet.put("emailAccount", emailAccount_ib.getText());
        propertySet.put("emailPassword", emailPassword_ib.getText());
        propertySet.put("updateInterval", updateInterval_ib.getText());
        propertySet.put("drawdownPeriod", drawdownPeriod_ib.getText());

        try{
            properties.setProperty(propertySet,path);
        } catch (Exception e) {
            updateLog("unable to save setting");
        }
    }
    @FXML
    String getMonitorAccount(){
        return monitorAccount_ib.getValue();
    }
    @FXML
    String getUpdateInterval(){
        return updateInterval_ib.getText();
    }
    @FXML
    String getProgramToBeKilled(){ return programToBeKilled_ib.getText();}
    @FXML
    String getDrawdownPeriod(){
        return drawdownPeriod_ib.getText();}
    @FXML
    String getEmailPassword_ib(){
        System.out.println(emailPassword_ib.getText());
        return emailPassword_ib.getText();
    }
    @FXML
    String getEmailAccount_ib(){
        System.out.println(emailPassword_ib.getText());
        return emailAccount_ib.getText();
    }


    @FXML
    Double getWarningThreshold(){
        return Double.parseDouble(warningThreshold_ib.getText());
    }
    @FXML
    Double getHardThreshold(){
        return Double.parseDouble(hardThreshold_ib.getText());
    }



    @FXML
    void startRiskMinotor(){
        if(riskMonitorThread !=null){
            riskMonitorThread.interrupt();
        } //terminate previous thread
        riskMonitorThread = new Thread(new RiskMonitor(this));
        riskMonitorThread.start();
    }
    @FXML
    void stopRiskMinotor(){
        riskMonitorThread.interrupt();
    }
    @FXML
    void killProgram(){
        try{
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec("taskkill /F /IM "+ programToBeKilled_ib.getText());
        }catch(Exception e){
            updateLog("****Problem in killing the process****");
        }

    }
    @FXML
    void testSendMail(){
        sendMail("Testing: Pinpoint IB Risk Monitor Warning","***System Generated, IB Risk Monitor Warning***");
    }



    @FXML
    void sendMail(String title, String content){
        updateLog("Trying to send email");
        // Recipient's email ID needs to be mentioned.
        String to =emailList_ib.getText();

        // Sender's email ID needs to be mentioned
        String from = emailAccount_ib.getText();
        // Assuming you are sending email from localhost
        String host = "smtpwcom.263xmail.com";
        // Get system properties
        Properties properties = System.getProperties();
        // Setup mail server
        properties.setProperty("mail.smtp.host", host);
        // port
        properties.setProperty("mail.smtp.socketFactory.port", "465");
        //SSL
        properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.port", "465");


        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties,
            new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailAccount_ib.getText(),emailPassword_ib.getText());
            }
        });

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);
            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));
            // Set To: header field of the header.
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            // Set Subject: header field
            message.setSubject(title);
            // Now set the actual message
            message.setText(content);
            // Send message
            Transport.send(message);
            updateLog("Sent message successfully.");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }

    }
}
