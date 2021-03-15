package ru.skillbranch.kotlinexample

import androidx.annotation.VisibleForTesting
import java.lang.IllegalArgumentException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

class User private constructor(
    private val firstName: String,
    private val lastName: String?,
    email: String? = null,
    rawPhone: String? = null,
    meta: Map<String, Any>? = null
) {
    val userInfo: String
    private val fullName: String
        get() = "${firstName.capitalize()} ${lastName?.capitalize() ?: ""}"
//        get() = listOfNotNull(firstName, lastName)
//            .joinToString { " " }
//            .capitalize()
    private val initials: String
        get() = "${firstName.first().toUpperCase()} ${lastName?.first()?.toUpperCase() ?: ""}"
//        get() = listOfNotNull(firstName, lastName)
//            .map{ it.first().toUpperCase() }
//            .joinToString { " " }
    private var phone: String? = null
        set(value) {
            field = value?.replace("[^+\\d]".toRegex(), "")
        }
    private var _login: String? = null
    var login: String
        set(value) {
            _login = value.toLowerCase()
        }
        get() = _login!!
    private var salt: String = ByteArray(16).also { SecureRandom().nextBytes(it)}.toString()
    private lateinit var passwordHash: String
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var accessCode: String? = null

    // for Email
    constructor(
        firstName: String,
        lastName: String?,
        email: String,
        password: String
    ): this(firstName, lastName, email = email, meta= mapOf("auth" to "password")) {
        println("Secondary mail constructor")
        passwordHash = encrypt(password)
    }

    // for phone
    constructor(
        firstName: String,
        lastName: String?,
        rawPhone: String
    ) : this(firstName, lastName, rawPhone = rawPhone, meta = mapOf("auth" to "sms")) {
        println("Secondary phone constructor ")
        val code: String = requestAccessCode()
        sendUserAccessCodeToUser(rawPhone, code)
    }

    constructor(
        firstName: String,
        lastName: String?,
        email: String?,
        salt: String,
        passwordHash: String,
        rawPhone: String?
    ) : this(firstName, lastName, email, rawPhone, meta = mapOf("src" to "csv")) {
        println("Secondary csv constructor")
        this.salt = salt
        this.passwordHash = passwordHash
    }


    init {
        println("First init block, primary constructor was called")

        check(!firstName.isBlank()) { "FirstName must be not blank" }
        check(email.isNullOrBlank() || rawPhone.isNullOrBlank()) { "Email or phone ust be not blank " }

        rawPhone?.run{
            if (isNotEmpty()) {
                phone = this
                login = phone!!
            }
        }
        email?.run {
            login = this
        }

        userInfo = """
             firstName: $firstName
             lastName: $lastName
             login: $login
             fullName: $fullName
             initials: $initials
             email: $email
             phone: $phone
             meta: $meta
             """.trimIndent()

        println(userInfo)
    }

    fun checkPassword(password: String) = encrypt(password) == passwordHash

    fun changePassword(oldPassword: String, newPassword: String) {
        if (checkPassword(oldPassword)) passwordHash = encrypt(newPassword)
        else IllegalArgumentException("The entered passwordd does not match the current password")
    }

    private fun encrypt(password: String): String = salt.plus(password).md5()

    private fun generateAccessCode(): String {
        val possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return StringBuilder().apply {
            repeat(6) {
                (possible.indices).random().also { index ->
                    append(possible[index])
                }
            }
        }.toString()
    }

    fun requestAccessCode(): String {
        return generateAccessCode().also {
            passwordHash = encrypt(it)
            accessCode = it
        }
    }

    private fun sendUserAccessCodeToUser(phone: String?, code: String) {
        println("Sending access code: $code on $phone")
    }

    private fun String.md5(): String {
        val md: MessageDigest = MessageDigest.getInstance("MD5")
        val digest: ByteArray = md.digest(toByteArray())
        val hexString: String = BigInteger(1, digest).toString(16)
        return hexString.padStart(32, '0')
    }

    companion object Factory {
        fun makeUser(
            fullName: String,
            email: String? = null,
            password: String? = null,
            phone: String? = null,
            passwordHash: String? = null,
            salt: String? = null
        ) : User {
            val (firstName, lastName) = fullName.fullNameToPair()
            return when {
                passwordHash != null && salt != null -> User(
                    firstName,
                    lastName,
                    email,
                    salt,
                    passwordHash,
                    phone
                )
                !phone.isNullOrBlank() -> User(firstName, lastName, phone)
                !email.isNullOrBlank() && !password.isNullOrBlank() -> User(firstName, lastName, email, password)
                else -> throw IllegalArgumentException("Email or phone must be not null or blank")

            }
        }

        fun String.fullNameToPair(): Pair<String, String?> {
            return this.split(" ")
                .filter { it.isNotBlank() }
                .run {
                    when (size) {
                        1 -> first() to null
                        2 -> first() to last()
                        else -> throw IllegalArgumentException("FullName must contain only first name " +
                                "and last name, current split result ${this@fullNameToPair}")
                    }
                }
        }
    }
}