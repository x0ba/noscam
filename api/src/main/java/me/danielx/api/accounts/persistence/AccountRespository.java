package me.danielx.api.accounts.persistence;

import me.danielx.api.accounts.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRespository extends JpaRepository<Account, Long> {
}
