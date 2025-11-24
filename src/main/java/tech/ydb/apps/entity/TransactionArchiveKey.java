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
public class TransactionArchiveKey implements Serializable {
    private static final long serialVersionUID = 7087835038666094308L;

    private Instant processedTs;
    private String txId;

    public TransactionArchiveKey() { }

    public TransactionArchiveKey(Instant processedTs, String txId) {
        this.processedTs = processedTs;
        this.txId = txId;
    }

    public Instant getProcessedTs() {
        return processedTs;
    }

    public String getTxId() {
        return txId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(processedTs, txId);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TransactionArchiveKey)) {
            return false;
        }
        TransactionArchiveKey other = (TransactionArchiveKey) o;
        return Objects.equals(processedTs, other.processedTs) && Objects.equals(txId, other.txId);
    }
}
