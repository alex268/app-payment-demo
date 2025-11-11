package tech.ydb.apps.entity;

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
@Table(name = "account_config")
public class AccountConfig {
    @Id
    public Long accountId;

    @Column
    public Integer weight;

    public Long getAccountId() {
        return accountId;
    }

    public Integer getWeight() {
        return weight;
    }
}
