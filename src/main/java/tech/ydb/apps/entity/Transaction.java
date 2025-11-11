package tech.ydb.apps.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

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
@Table(name = "transaction")
public class Transaction implements Serializable, Persistable<TransactionKey> {
    private static final long serialVersionUID = 5756781726661470066L;

    @EmbeddedId
    private TransactionKey id;

    @Column(name = "acc_b")
    private Long accountB;

    @Column(name = "acc_part_b")
    private Integer accPartB;

    @Column
    private Instant inputTs;

    @Column
    private Instant acceptedTs;

    @Column
    private Instant processedTs;

    @Column
    private BigDecimal amount;

    @Transient
    private final boolean isNew;

    public Transaction() {
        this.isNew = false;
    }

    public Transaction(SaldoKey a, SaldoKey b, BigDecimal amount, Instant input, Instant accepted) {
        this.id = new TransactionKey(a, UUID.randomUUID().toString());
        this.accountB = b.getAccountId();
        this.accPartB = b.getPartNum();

        this.amount = amount;

        this.inputTs = input;
        this.acceptedTs = accepted;
        this.processedTs = Instant.now();
        this.isNew = true;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public TransactionKey getId() {
        return id;
    }

    public Long getAccountA() {
        return id.getAccountA();
    }

    public Long getAccountB() {
        return accountB;
    }

    public Integer getAccountPartA() {
        return id.getAccountPartA();
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

    public Instant getProcessedTs() {
        return processedTs;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
