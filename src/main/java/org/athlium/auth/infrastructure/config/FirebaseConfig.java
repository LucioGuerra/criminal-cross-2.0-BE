package org.athlium.auth.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Configuration class for Firebase Admin SDK initialization.
 * Loads credentials from file path specified in environment variable.
 */
@ApplicationScoped
@Startup
public class FirebaseConfig {

    private static final Logger LOG = Logger.getLogger(FirebaseConfig.class);

    @ConfigProperty(name = "firebase.credentials.path")
    Optional<String> credentialsPath;

    @ConfigProperty(name = "firebase.project-id")
    Optional<String> projectId;

    @ConfigProperty(name = "firebase.mock.enabled", defaultValue = "false")
    boolean mockEnabled;

    private FirebaseApp firebaseApp;

    @PostConstruct
    void init() {
        if (mockEnabled) {
            LOG.warn("Firebase mock mode is enabled. No real Firebase authentication will occur.");
            return;
        }

        if (credentialsPath.isEmpty() || credentialsPath.get().isBlank()) {
            LOG.warn("Firebase credentials path not configured. Set FIREBASE_CREDENTIALS_PATH environment variable.");
            return;
        }

        try {
            initializeFirebaseApp();
            LOG.info("Firebase Admin SDK initialized successfully");
        } catch (IOException e) {
            LOG.error("Failed to initialize Firebase Admin SDK", e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    private void initializeFirebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = new FileInputStream(credentialsPath.get());
            
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount));
            
            projectId.ifPresent(optionsBuilder::setProjectId);
            
            firebaseApp = FirebaseApp.initializeApp(optionsBuilder.build());
        } else {
            firebaseApp = FirebaseApp.getInstance();
        }
    }

    /**
     * Gets the FirebaseAuth instance.
     * This is not a CDI producer because FirebaseAuth is not proxyable.
     * Use this method directly or inject FirebaseConfig instead.
     */
    public FirebaseAuth getFirebaseAuth() {
        if (mockEnabled || firebaseApp == null) {
            return null;
        }
        return FirebaseAuth.getInstance(firebaseApp);
    }

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    public boolean isInitialized() {
        return firebaseApp != null || mockEnabled;
    }
}
