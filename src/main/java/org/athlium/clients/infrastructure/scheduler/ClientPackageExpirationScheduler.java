package org.athlium.clients.infrastructure.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.athlium.clients.application.usecase.ExpireExpiredClientPackagesUseCase;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ClientPackageExpirationScheduler {

    private static final Logger LOG = Logger.getLogger(ClientPackageExpirationScheduler.class);

    @Inject
    ExpireExpiredClientPackagesUseCase expireExpiredClientPackagesUseCase;

    @Scheduled(cron = "0 15 3 * * ?", timeZone = "UTC")
    void expirePackages() {
        long expired = expireExpiredClientPackagesUseCase.execute();
        LOG.infof("Client package expiration completed: expired=%d", expired);
    }
}
