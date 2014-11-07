package nilenso.com.kulu_mobile;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;

public class ExpenseEntry extends RealmObject {
     private String comments;
     private String expenseType;
     private Date createdAt;

    @Index
    private String invoice;


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
}
