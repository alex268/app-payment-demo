package tech.ydb.apps.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tech.ydb.apps.entity.AccountConfig;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface AccountConfigRepo extends JpaRepository<AccountConfig, Long> {

}
