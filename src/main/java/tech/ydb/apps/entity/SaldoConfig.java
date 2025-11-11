package tech.ydb.apps.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

/**
 *
 * @author Aleksandr Gorshenin
 */
@Entity
@Immutable
@Table(name = "saldo_config")
public class SaldoConfig {
    @EmbeddedId
    public SaldoKey id;

    @Column
    public String processor;

    public SaldoKey getId() {
        return id;
    }

    public String getProcessor() {
        return processor;
    }
}
