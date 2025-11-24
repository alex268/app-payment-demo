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
@Table(name = "transaction_archive")
public class TransactionArchive implements Serializable, Persistable<TransactionArchiveKey> {
    private static final long serialVersionUID = -8027632897709155651L;

    @EmbeddedId
    private TransactionArchiveKey id;

    @Column(name = "acc_a")
    private Long accountA;

    @Column(name = "acc_part_a")
    private Integer accPartA;

    @Column(name = "acc_b")
    private Long accountB;

    @Column(name = "acc_part_b")
    private Integer accPartB;

    @Column
    private Instant inputTs;

    @Column
    private Instant acceptedTs;

    @Column
    private BigDecimal amount;

    @Transient
    private final boolean isNew;

    public TransactionArchive() {
        this.isNew = false;
    }

    public TransactionArchive(Transaction tx) {
        this.id = new TransactionArchiveKey(tx.getProcessedTs(), tx.getId().getTxId());
        this.accountA = tx.getAccountA();
        this.accPartA = tx.getAccountPartA();
        this.accountB = tx.getAccountB();
        this.accPartB = tx.getAccountPartB();

        this.amount = tx.getAmount();

        this.inputTs = tx.getInputTs();
        this.acceptedTs = tx.getAcceptedTs();
        this.isNew = true;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public TransactionArchiveKey getId() {
        return id;
    }

    public Long getAccountA() {
        return accountA;
    }

    public Long getAccountB() {
        return accountB;
    }

    public Integer getAccountPartA() {
        return accPartA;
    }

    public Integer getAccountPartB() {
        return accPartB;
    }

    public Instant getInputTs() {
        return inputTs;
    }

    public Instant getAcceptedTs() {
        return acceptedTs;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
