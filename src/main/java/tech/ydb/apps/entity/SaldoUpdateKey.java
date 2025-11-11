package tech.ydb.apps.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

import jakarta.persistence.Embeddable;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Embeddable
public class SaldoUpdateKey implements Serializable {
    private static final long serialVersionUID = 5308476873455433397L;

    private SaldoKey key;
    private Instant createdTs;
    private String txId;

    public SaldoUpdateKey() { }

    public SaldoUpdateKey(SaldoKey key, String txId) {
        this.key = key;
        this.createdTs = Instant.now();
        this.txId = txId;
    }

    public SaldoKey getSaldoKey() {
        return key;
    }

    public String getTxId() {
        return txId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, createdTs, txId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SaldoUpdateKey)) {
            return false;
        }
        SaldoUpdateKey other = (SaldoUpdateKey) o;
        return Objects.equals(key, other.key)
                && Objects.equals(createdTs, other.createdTs)
                && Objects.equals(txId, other.txId);
    }

}
