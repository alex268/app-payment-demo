package tech.ydb.apps.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Entity
@Table(name = "saldo_update")
public class SaldoUpdate implements Persistable<SaldoUpdateKey> {
    @EmbeddedId
    private SaldoUpdateKey id;

    @Column
    private BigDecimal amount;

    @Transient
    private final boolean isNew;

    public SaldoUpdate() {
        this.isNew = false;
    }

    public SaldoUpdate(SaldoKey key, Transaction tx, BigDecimal amount) {
        this.id = new SaldoUpdateKey(key, tx.getId().getTxId());
        this.amount = amount;
        this.isNew = true;
    }

    @Override
    public SaldoUpdateKey getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
