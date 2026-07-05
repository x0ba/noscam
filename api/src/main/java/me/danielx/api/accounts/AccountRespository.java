package me.danielx.api.accounts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRespository extends JpaRepository<Account, Long> {
  Page<Account> findAllByUserId(Long userId, Pageable pageable);
}
