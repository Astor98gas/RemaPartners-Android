package com.arsansys.remapartners.data.model.entities

class UserEntity {

    internal val id: String? = null

    internal var username: String? = null

    internal var email: String? = null

    internal var password: String? = null

    internal val active = true

    internal var rol: RolEntity = RolEntity()

    internal var googleTokens: String? = null

}