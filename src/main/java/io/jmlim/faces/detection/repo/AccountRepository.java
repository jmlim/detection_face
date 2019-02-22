package io.jmlim.faces.detection.repo;

import io.jmlim.faces.detection.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * @param email
     * @return
     */
    Account findByEmail(String email);

    Account findByFaceId(String faceId);
}
