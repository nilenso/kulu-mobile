package nilenso.com.kulu_mobile;

import android.provider.BaseColumns;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by who on 04/11/14.
 */

public class ExpenseEntry extends RealmObject {
     private String comments;
     private String invoice;
     private String expenseType;
     private Date createdAt;


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
