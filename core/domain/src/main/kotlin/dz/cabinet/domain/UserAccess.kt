package dz.cabinet.domain

enum class UserRole {
    ADMIN,
    PRACTITIONER
}

data class EditPermissions(
    val canEditPatients: Boolean,
    val canEditTreatments: Boolean,
    val canRecordPayments: Boolean,
    val canManageUsers: Boolean
) {
    companion object {
        fun admin(): EditPermissions = EditPermissions(
            canEditPatients = true,
            canEditTreatments = true,
            canRecordPayments = true,
            canManageUsers = true
        )

        fun practitioner(): EditPermissions = EditPermissions(
            canEditPatients = true,
            canEditTreatments = true,
            canRecordPayments = true,
            canManageUsers = false
        )

        fun readOnly(): EditPermissions = EditPermissions(
            canEditPatients = false,
            canEditTreatments = false,
            canRecordPayments = false,
            canManageUsers = false
        )
    }
}

data class UserAccountSummary(
    val username: String,
    val role: UserRole,
    val permissions: EditPermissions
) {
    val isAdmin: Boolean
        get() = role == UserRole.ADMIN && permissions.canManageUsers
}
