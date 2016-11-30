package sample;
import com.ib.client.Contract;
import javafx.beans.property.*;

/**
 * Created by David on 10/29/2016.
 */
public class IBPosition {
    private Contract contract;
    private IntegerProperty conID;
    private IntegerProperty position;
    private DoubleProperty avgCost;
    private DoubleProperty mktPrice;
    private DoubleProperty mktValue;
    private StringProperty symbol;
    private StringProperty exchange;
    private StringProperty type;


    //constructor
    public IBPosition(){
        contract = null;
        position = null;
        avgCost = null;
    }
    public IBPosition(Contract contract, Integer position, Double avgCost, Double mktPrice, Double mktValue){
        this.contract = contract;
        this.conID = new SimpleIntegerProperty(contract.m_conId);
        this.position = new SimpleIntegerProperty(position);
        this.avgCost = new SimpleDoubleProperty(avgCost);
        this.mktPrice = new SimpleDoubleProperty(mktPrice);
        this.mktValue = new SimpleDoubleProperty(mktValue);
        this.symbol = new SimpleStringProperty(contract.m_localSymbol);
        this.exchange = new SimpleStringProperty(contract.m_primaryExch);
        this.type = new SimpleStringProperty(contract.m_secType);
    }
    public IntegerProperty conIDProperty(){
        return this.conID;
    }
    public IntegerProperty positionProperty(){
        return this.position;
    }
    public DoubleProperty avgCostProperty(){
        return this.avgCost;
    }
    public DoubleProperty mktPriceProperty(){
        return this.mktPrice;
    }
    public DoubleProperty mktValueProperty(){
        return this.mktValue;
    }
    public StringProperty symbolProperty(){
        return this.symbol;
    }
    public StringProperty exchangeProperty(){
        return this.exchange;
    }
    public StringProperty typeProperty(){
        return this.type;
    }
}