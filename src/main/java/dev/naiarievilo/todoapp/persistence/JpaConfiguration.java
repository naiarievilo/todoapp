package dev.naiarievilo.todoapp.persistence;

import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(value = {"io.hypersistence.utils.spring.repository", "dev.naiarievilo.todoapp"},
    repositoryBaseClass = BaseJpaRepositoryImpl.class)
public class JpaConfiguration {

}
