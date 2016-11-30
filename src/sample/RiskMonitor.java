package sample;

import javafx.application.Platform;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

/**
 * Created by David on 11/1/2016.
 */
public class RiskMonitor implements Runnable{
    Controller controllerOb;
    //DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    String accountMonitoring;
    LinkedList<LocalDateTime> NAVhistoryTime;
    LinkedList<String> NAVhistoryValue;
    String aumFirst;
    String aumLast;
    LocalDateTime timeFirst, timeLast;
    LocalDateTime lastWarningTime ;
    LocalDateTime lastRiskControlTime ;
    Integer monitorPeriod; //larger than 1 days
    Integer updateInterval; //in ms

    public RiskMonitor(Controller controllerOb) {
        this.controllerOb= controllerOb; //reference for main
        accountMonitoring = controllerOb.getMonitorAccount();
        lastWarningTime = LocalDateTime.now().minusMinutes(5);
        lastRiskControlTime = LocalDateTime.now().minusMinutes(5);
        NAVhistoryTime = new LinkedList();
        NAVhistoryValue = new LinkedList();
        String a = controllerOb.getDrawdownPeriod();
        monitorPeriod = Integer.parseInt(controllerOb.getDrawdownPeriod());  //larger than 1 days
        updateInterval = Integer.parseInt(controllerOb.getUpdateInterval()); //in ms
    }
    public void run() {
        Platform.runLater(()->{
            controllerOb.updateLog("StartRiskMonitor");
        });
        while(!Thread.currentThread().isInterrupted()){
            if(accountMonitoring != controllerOb.getMonitorAccount()){
                NAVhistoryTime = new LinkedList(); // if account changed will need to reload the whole linked list of AUM
                NAVhistoryValue = new LinkedList();
                accountMonitoring = controllerOb.getMonitorAccount();
            }
            if(controllerOb.ibwrapper.accountList.get(accountMonitoring)==null){
                System.out.println("account not ready yet ");
            }else{
                NAVhistoryTime.add(LocalDateTime.now());
                NAVhistoryValue.add(controllerOb.ibwrapper.accountList.get(accountMonitoring).getAUM());
                timeFirst = NAVhistoryTime.getFirst();timeLast = NAVhistoryTime.getLast();
                aumFirst = NAVhistoryValue.getFirst();aumLast = NAVhistoryValue.getLast();
                while(ChronoUnit.MINUTES.between(timeFirst, timeLast)>=monitorPeriod){
                    timeFirst = NAVhistoryTime.pop();
                    aumFirst = NAVhistoryValue.pop();
                }

                if(Double.parseDouble(aumFirst) !=0){
                    if( (Double.parseDouble(aumLast)/Double.parseDouble(aumFirst)-1)<= -(controllerOb.getHardThreshold())/100){
                        System.out.println("*******************!hardThreshold reached!*********************");
                        if(ChronoUnit.MINUTES.between(lastRiskControlTime ,LocalDateTime.now())>=5){
                            lastRiskControlTime = LocalDateTime.now();
                            String msgContent = (accountMonitoring + "-" + ChronoUnit.MINUTES.between(timeFirst, timeLast) + " mins - " +
                                    NAVhistoryTime.getFirst().toString() + " : " + NAVhistoryValue.getFirst() + " || " +
                                    NAVhistoryTime.getLast().toString() + " : " + NAVhistoryValue.getLast());
                            String msgTitle = ("HardThreshold reached " + Double.parseDouble(aumLast)+ "/"+Double.parseDouble(aumFirst) + " -1 <= -" + controllerOb.getHardThreshold()/100);
                            Platform.runLater(()->{
                                controllerOb.sendMail(msgTitle, msgContent);
                                controllerOb.killProgram();
                                controllerOb.updateLog("email sent, terminating program ; " + controllerOb.getProgramToBeKilled());
                            });

                        }


                    }else if((Double.parseDouble(aumLast)/Double.parseDouble(aumFirst)-1)<= -(controllerOb.getWarningThreshold())/100){
                        System.out.println("*********warningThreshold reached**********");
                        if(ChronoUnit.MINUTES.between(lastWarningTime ,LocalDateTime.now())>=5) {
                            lastWarningTime = LocalDateTime.now();
                            String msgContent = (accountMonitoring + "-" + ChronoUnit.MINUTES.between(timeFirst, timeLast) + " mins - " +
                                    NAVhistoryTime.getFirst().toString() + " : " + NAVhistoryValue.getFirst() + " || " +
                                    NAVhistoryTime.getLast().toString() + " : " + NAVhistoryValue.getLast());
                            String msgTitle = ("warningThreshold reached " + Double.parseDouble(aumLast) + "/" + Double.parseDouble(aumFirst) + " -1 <= -" + controllerOb.getWarningThreshold() / 100);
                            Platform.runLater(() -> {
                                controllerOb.sendMail(msgTitle, msgContent);
                            });
                        }
                    }
                }
                Platform.runLater(() -> {
                controllerOb.updateLog(accountMonitoring +"-" + ChronoUnit.MINUTES.between(timeFirst, timeLast)+ " mins - " +
                        NAVhistoryTime.getFirst().toString() + " : "+NAVhistoryValue.getFirst() + " || " +
                        NAVhistoryTime.getLast().toString()+ " : " + NAVhistoryValue.getLast());
                });
            }
            try {
                Thread.sleep(updateInterval);
            } catch (InterruptedException ex) {
                Platform.runLater(()->{
                    controllerOb.updateLog("StoppingRiskMonitor");
                });
                Thread.currentThread().interrupt();
            }
        }
    }
}
