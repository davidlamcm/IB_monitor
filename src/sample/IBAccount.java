package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import static java.lang.StrictMath.round;

/**
 * Created by David on 10/29/2016.
 */
public class IBAccount {

    private String account;
    private Map<Integer, IBPosition> positionList = new HashMap<Integer, IBPosition>();
    public ObservableList<IBPosition> positionListForTableView = FXCollections.observableArrayList();

    private String AUM;
    public IBAccount(String account){
        this.account =account;
    }

    public void updatePosition(IBPosition position){

            this.positionList.put(position.conIDProperty().getValue(), position);
            this.positionListForTableView.setAll(positionList.values());

    }
    public Map<Integer, IBPosition> getPosition(){
        return this.positionList;
    }
    public void updateAUM(String AUM){
        this.AUM= AUM;
    }

    public String getAUM(){
        return this.AUM;
    }
    public String getLong(){
        double longUSD = 0;
        for(IBPosition pos : this.positionList.values()){
            if(pos.mktValueProperty().getValue()>=0 ){
                longUSD += pos.mktValueProperty().getValue();
            }
        }
        DecimalFormat df = new DecimalFormat("#.#");
        return  String.format("%.2f", longUSD);
    }
    public String getShort(){
        double shortUSD = 0;
        for(IBPosition pos : this.positionList.values()){
            if(pos.mktValueProperty().getValue()<=0 ){
                shortUSD += pos.mktValueProperty().getValue();
            }
        }
        return String.format("%.2f", shortUSD);
    }
}
