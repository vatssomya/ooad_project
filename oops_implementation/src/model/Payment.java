// model/Payment.java
package model;
public class Payment {
    private String id;
    private double amount;
    private String method;
    private String status;
    private String txnRef;
    public Payment(String id,double amount,String method,String status,String txnRef){
        this.id=id; this.amount=amount; this.method=method; this.status=status; this.txnRef=txnRef;
    }
    public String getStatus(){return status;}
    public String getTxnRef(){return txnRef;}
}
