package tech.ydb.apps.repository;

import org.springframework.data.repository.CrudRepository;

import tech.ydb.apps.entity.AccountConfig;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface AccountConfigRepo extends CrudRepository<AccountConfig, Long> {

}
