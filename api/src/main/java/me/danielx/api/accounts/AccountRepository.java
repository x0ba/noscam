package me.danielx.api.accounts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {
  Page<Account> findAllByUserId(Long userId, Pageable pageable);

  Optional<Account> findByPublicId(UUID accountId);
}
