package nilenso.com.kulu_mobile2;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;

public class ExpenseEntry extends RealmObject {
    private String comments;
    private String expenseType;
    private String invoicePath;
    private boolean deleted;
    private String invoice;
    private String merchantName;
    private int amount;
    private String currency;
    private Date expenseDate;
    private Date createdAt;

    @Index
    private String id;


    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getInvoice() {
        return invoice;
    }

    public void setInvoice(String invoice) {
        this.invoice = invoice;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getInvoicePath() {
        return invoicePath;
    }

    public void setInvoicePath(String invoicePath) {
        this.invoicePath = invoicePath;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public Date getExpenseDate() { return expenseDate; }

    public void setExpenseDate(Date expenseDate) { this.expenseDate = expenseDate; }

    public String getMerchantName() { return merchantName; }

    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public int getAmount() { return amount; }

    public void setAmount(int amount) { this.amount = amount; }

    public String getCurrency() { return currency; }

    public void setCurrency(String currency) { this.currency = currency; }

}
