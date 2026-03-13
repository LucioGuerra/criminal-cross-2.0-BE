package org.athlium.users.infrastructure.repository;

import org.athlium.users.domain.model.PackageStatus;
import org.athlium.users.domain.model.UserWithPackageStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserQueryRepositoryImplUnitTest {

    @Test
    void shouldMapLocalDatePeriodEndFromNativeRow() throws Exception {
        UserQueryRepositoryImpl repository = new UserQueryRepositoryImpl();
        Method mapRow = UserQueryRepositoryImpl.class.getDeclaredMethod("mapRow", Object[].class);
        mapRow.setAccessible(true);

        Object[] row = new Object[] {
                1L,
                "Ana",
                "Lopez",
                "ana@example.com",
                true,
                "ACTIVE",
                LocalDate.of(2026, 3, 10),
                7
        };

        UserWithPackageStatus user = (UserWithPackageStatus) mapRow.invoke(repository, new Object[] { row });

        assertEquals(1L, user.getId());
        assertEquals(PackageStatus.ACTIVE, user.getPackageStatus());
        assertEquals(LocalDate.of(2026, 3, 10), user.getPeriodEnd());
        assertEquals(7, user.getDaysRemaining());
    }
}
