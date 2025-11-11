package tech.ydb.apps.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Embeddable
public class SaldoKey implements Serializable {
    private static final long serialVersionUID = -1994364579710732615L;

    private long accountId;
    private int partNum;

    public SaldoKey() { }

    public SaldoKey(long accountId, int partNum) {
        this.accountId = accountId;
        this.partNum = partNum;
    }

    public long getAccountId() {
        return accountId;
    }

    public int getPartNum() {
        return partNum;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, partNum);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SaldoKey)) {
            return false;
        }
        SaldoKey other = (SaldoKey) o;
        return accountId == other.accountId && partNum == other.partNum;
    }

}
