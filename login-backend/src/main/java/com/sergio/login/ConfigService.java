package com.sergio.login;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ConfigService {

    private static final Logger log = LoggerFactory.getLogger(ConfigService.class);

    private final CfgConfigRepository repo;
    private final Map<String, CfgConfig> cache = new ConcurrentHashMap<>();

    public ConfigService(CfgConfigRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void cargar() {
        cache.clear();
        repo.findAll().forEach(c -> cache.put(c.getVar(), c));
        log.info("Configuración BRM_CONFIG cargada: {} parámetros", cache.size());
    }

    public String getValor(String var) {
        CfgConfig c = cache.get(var);
        return c != null ? c.getValor() : null;
    }

    public String getValor(String var, String defaultValor) {
        CfgConfig c = cache.get(var);
        return c != null ? c.getValor() : defaultValor;
    }

    public Map<String, String> getGrupo(String grupo) {
        return cache.values().stream()
                .filter(c -> grupo.equals(c.getGrupo()))
                .collect(Collectors.toMap(CfgConfig::getVar, CfgConfig::getValor));
    }

    public List<CfgConfig> getTodos() {
        return List.copyOf(cache.values());
    }
}
