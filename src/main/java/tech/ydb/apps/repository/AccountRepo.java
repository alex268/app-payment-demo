package tech.ydb.apps.repository;

import java.util.Optional;

import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;

import tech.ydb.apps.entity.Account;

/**
 *
 * @author Aleksandr Gorshenin
 */
public interface AccountRepo extends CrudRepository<Account, Long> {

    @Override
    @QueryHints({
        @QueryHint(name=HibernateHints.HINT_COMMENT, value="use_index:<index-name>:<table-name>(<colums>)")
    })
    public Optional<Account> findById(Long id);



}
