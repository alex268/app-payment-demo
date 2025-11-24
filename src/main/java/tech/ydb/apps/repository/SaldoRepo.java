package tech.ydb.apps.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.ydb.apps.entity.Saldo;
import tech.ydb.apps.entity.SaldoKey;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface SaldoRepo extends JpaRepository<Saldo, SaldoKey> {

}
