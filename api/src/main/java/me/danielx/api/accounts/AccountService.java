package me.danielx.api.accounts;

import me.danielx.api.accounts.dto.AccountListResponse;
import me.danielx.api.users.User;
import me.danielx.api.users.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {
    private final AccountRespository accountRespository;
    private final UserRepository userRepository;

    public AccountService(AccountRespository accountRespository, UserRepository userRepository) {
        this.accountRespository = accountRespository;
        this.userRepository = userRepository;
    }

    public Page<AccountListResponse> getAccountsByUser(UUID publicUserId, Pageable pageable) {
        User user = userRepository.findByPublicId(publicUserId).orElseThrow(() -> new RuntimeException("User not found"));
        return accountRespository.findAllByUserId(user.getId(), pageable).map(AccountService::toAccountListResponse);
    }

    private static AccountListResponse toAccountListResponse(Account account) {
        return new AccountListResponse(account.getPublicId(), account.getBank(), account.getAccountName(), account.getBalance());
    }
}
