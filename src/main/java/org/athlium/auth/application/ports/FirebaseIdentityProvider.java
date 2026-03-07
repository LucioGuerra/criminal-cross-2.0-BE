package org.athlium.auth.application.ports;

import org.athlium.auth.domain.model.FirebaseSessionTokens;

public interface FirebaseIdentityProvider {

    FirebaseSessionTokens register(String email, String password, String displayName);

    FirebaseSessionTokens login(String email, String password);

    FirebaseSessionTokens refresh(String refreshToken);
}
