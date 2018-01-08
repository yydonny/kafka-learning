package yangyd.kf.commons;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;

public class TransactionRecord {

  @JsonSerialize(using = Instants.Serializer.class)
  @JsonDeserialize(using = Instants.Deserializer.class)
  private Instant acceptTime;
  private String customerID;
  private String sourceAccount;
  private String destAccount;
  private BigDecimal amount;

  public static TransactionRecord random() {
    return new TransactionRecord("test1", "src-acc", "dst-acc");
  }

  public TransactionRecord() {

  }

  public TransactionRecord(String customerID, String sourceAccount, String destAccount) {
    this.customerID = customerID;
    this.sourceAccount = sourceAccount;
    this.destAccount = destAccount;
    acceptTime = Instant.now();
    amount = BigDecimal.valueOf(new Random().nextInt(10000) / 100.0);
  }

  public Instant getAcceptTime() {
    return acceptTime;
  }

  public void setAcceptTime(Instant acceptTime) {
    this.acceptTime = acceptTime;
  }

  public String getCustomerID() {
    return customerID;
  }

  public void setCustomerID(String customerID) {
    this.customerID = customerID;
  }

  public String getSourceAccount() {
    return sourceAccount;
  }

  public void setSourceAccount(String sourceAccount) {
    this.sourceAccount = sourceAccount;
  }

  public String getDestAccount() {
    return destAccount;
  }

  public void setDestAccount(String destAccount) {
    this.destAccount = destAccount;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }
}
