package dz.cabinet.domain

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class UserAccessTest {

    @Test
    fun `admin account has all edit and user management rights`() {
        val admin = UserAccountSummary(
            username = "admin",
            role = UserRole.ADMIN,
            permissions = EditPermissions.admin()
        )
        assertTrue(admin.isAdmin)
        assertTrue(admin.permissions.canEditPatients)
        assertTrue(admin.permissions.canEditTreatments)
        assertTrue(admin.permissions.canRecordPayments)
        assertTrue(admin.permissions.canManageUsers)
    }

    @Test
    fun `practitioner account cannot manage users`() {
        val practitioner = UserAccountSummary(
            username = "dr.bouzid",
            role = UserRole.PRACTITIONER,
            permissions = EditPermissions.practitioner()
        )
        assertFalse(practitioner.isAdmin)
        assertTrue(practitioner.permissions.canEditPatients)
        assertFalse(practitioner.permissions.canManageUsers)
    }
}
