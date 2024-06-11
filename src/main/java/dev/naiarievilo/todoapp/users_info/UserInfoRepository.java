package dev.naiarievilo.todoapp.users_info;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface UserInfoRepository extends BaseJpaRepository<UserInfo, Long> {

}
