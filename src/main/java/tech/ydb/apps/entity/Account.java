package tech.ydb.apps.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Entity
@Immutable
@Table(name = "account")
public class Account implements Serializable {
    private static final long serialVersionUID = -3950165879129931545L;

    @Id
    public Long accountId;

    @Column
    public String bic;

    public Long getAccountId() {
        return accountId;
    }

    public String getBic() {
        return bic;
    }
}
