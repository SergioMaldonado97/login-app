package com.sergio.login;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CfgConfigRepository extends JpaRepository<CfgConfig, String> {
    List<CfgConfig> findByGrupo(String grupo);
}
