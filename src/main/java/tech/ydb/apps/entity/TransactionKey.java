package tech.ydb.apps.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Embeddable
public class TransactionKey implements Serializable {
    private static final long serialVersionUID = 1635407474820269097L;

    @Column(name="acc_a")
    private long accountA;
    @Column(name="acc_part_a")
    private int accountPartA;

    private String txId;

    public TransactionKey() { }

    public TransactionKey(SaldoKey accA, String txId) {
        this.accountA = accA.getAccountId();
        this.accountPartA = accA.getPartNum();
        this.txId = txId;
    }

    public long getAccountA() {
        return accountA;
    }

    public int getAccountPartA() {
        return accountPartA;
    }

    public String getTxId() {
        return txId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountA, accountPartA, txId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TransactionKey)) {
            return false;
        }
        TransactionKey other = (TransactionKey) o;
        return accountA == other.accountA && accountPartA == other.accountPartA && Objects.equals(txId, other.txId);
    }
}
