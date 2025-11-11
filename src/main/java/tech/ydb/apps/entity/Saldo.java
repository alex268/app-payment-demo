package tech.ydb.apps.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

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
@Table(name = "saldo")
public class Saldo implements Serializable, Persistable<SaldoKey> {
    private static final long serialVersionUID = 5262098464543726511L;

    @EmbeddedId
    public SaldoKey id;

    @Column
    public BigDecimal amount;

    @Column
    public Instant updatedTs;

    @Transient
    private final boolean isNew;

    public Saldo() {
        this.isNew = false;
    }

    public Saldo(SaldoKey id, BigDecimal amount) {
        this.id = id;
        this.amount = amount;
        this.updatedTs = Instant.now();
        this.isNew = true;
    }

    @Override
    public SaldoKey getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getUpdatedTs() {
        return updatedTs;
    }

    public void updateSaldo(BigDecimal amount) {
        this.amount = amount;
        this.updatedTs = Instant.now();
    }
}
